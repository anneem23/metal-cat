package org.anneem23.metal.cat.beat;

import be.tarsos.dsp.onsets.OnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PrintOnsetHandler;
import be.tarsos.dsp.resample.Resampler;
import org.anneem23.metal.cat.audio.Shared;
import org.anneem23.metal.cat.beat.onset.OnsetDetectionFunction;

import java.io.IOException;

/**
 * BeatTracker is a java implementation of Adam Starks
 * BTrack algorithm.
 * <p>
 * It does live beat tracking and is based on a
 * combination of two algorithms:
 * <p>
 * - Tempo estimation is based on the Davies and Plumbley method
 * - Beat prediction is based on Ellis' dynamic programming beat
 *
 */
public class BeatTracker implements OnsetDetector, BeatTrackingAlgorithm {

    private OnsetHandler onsetHandler;
    private final OnsetDetectionFunction onsetDetectionFunction;

    private int m0;
    private final double tightness;
    /** adds balance between existing and past data in cumulative score (default: 0.9) */
    private final double[] acf;
    private final int hopSize;

    private final int onsetDFBufferSize;
    private final float[] onsetDF;

    private double estimatedTempo;
    private final double[][] tempoTransitionMatrix = new double[41][41]; /**<  tempo transition matrix */
    private final double[] tempoObservationVector = new double[41];

    private final float[] cumulativeScore;

    private float beatPeriod;                                     /** the time (in DF samples) between two beats    */
    private boolean beatDueInFrame;
    private int beatCounter = -1;

    private final double[] combFilterBankOutput;
    private final float[] weightingVector = new float[128];

    private final double[] deltas = new double[41];                       /**<  to hold final tempo candidate array */
    private final double[] previousDeltas = new double[41];                   /**<  previous delta */
    private final double[] previousDeltasFixed;
    private double[] resampledOnsetDetectionFunctionData;

    public BeatTracker(int hopSize, OnsetDetectionFunction onsetDetector, float sampleRate) throws IOException {
        onsetDetectionFunction = onsetDetector;
        onsetDFBufferSize = (512*512)/hopSize;
        onsetDF = new float[onsetDFBufferSize];
        resampledOnsetDetectionFunctionData = new double[512];
        cumulativeScore = new float[onsetDFBufferSize];
        // initialize beat period with 120 bpm
        beatPeriod = Math.round(60/((((double) hopSize)/ sampleRate)* 120));
        // initialize estimated tempo with 120 bpm
        estimatedTempo = 120.0;
        this.hopSize = hopSize;
        onsetHandler = new PrintOnsetHandler();
        m0 = 10;
        acf = new double[hopSize];
        tightness = 5;
        combFilterBankOutput = new double[128];
        previousDeltasFixed = new double[41];


        initializeArrays();

        final double rayparam = 43;
        // create rayleigh weighting vector
        for (int n = 0;n < 128;n++)
        {
            weightingVector[n] = (float) (((double) n / Math.pow(rayparam,2)) * Math.exp((-1*Math.pow((double)-n,2)) / (2*Math.pow(rayparam,2))));
        }

        // initialise prev_delta
        for (int i = 0;i < 41;i++)
        {
            previousDeltas[i] = 1;
        }

        // create tempo transition matrix
        double x;

        for (int i = 0;i < 41;i++)
        {
            for (int j = 0;j < 41;j++)
            {
                x = (double) j+1;
                double tMu = (double) i + 1;
                double mSig = (double) 41 / 8;
                tempoTransitionMatrix[i][j] = (1 / (mSig * Math.sqrt(2*Math.PI))) * Math.exp( (-1*Math.pow(x- tMu,2)) / (2*Math.pow(mSig,2)) );
            }
        }


    }

    private void initializeArrays() {
        // initialise df_buffer to zeros
        for (int i = 0; i < onsetDFBufferSize; i++)
        {
            onsetDF[i] = 0;
            cumulativeScore[i] = 0;


            if ((i %  Math.round(beatPeriod)) == 0) {
                onsetDF[i] = 1;
            }

        }

    }



    @Override
    public void processAudioFrame(double[] audioBuffer) {
        double odfSample = onsetDetectionFunction.calculateOnsetDetectionFunctionSample(audioBuffer);
        processOnsetDetectionFunctionSample(odfSample);
    }

    private void processOnsetDetectionFunctionSample(double onsetDetectionFunctionSample) {
        // to ensure that the onset detection onset sample is positive
        double odfSample = Math.abs(onsetDetectionFunctionSample);

        // add a tiny constant to the sample to stop it from ever going
        // to zero. this is to avoid problems further down the line
        odfSample = odfSample + 0.0001;

        m0--;
        beatCounter--;
        beatDueInFrame = false;

        // move all samples back one step
        for (int i = 0; i < (onsetDFBufferSize -1); i++)
        {
            onsetDF[i] = onsetDF[i+1];
        }

        // add new sample at the end
        onsetDF[onsetDFBufferSize -1] = (float) odfSample;
        // update cumulative score
        updateCumulativeScore(odfSample);

        // if we are halfway between beats
        if (m0 == 0)
        {
            predictBeat();
        }

        // if we are at a beat
        if (beatCounter == 0)
        {
            beatDueInFrame = true;	// indicate a beat should be output

            resampleOnsetDetectionFunction();
            // recalculate the tempo
            calculateTempo();
        }
    }


    private void resampleOnsetDetectionFunction() {
        float[] output = new float[512];
        float[] input = new float[onsetDFBufferSize];

        for (int i = 0; i < onsetDFBufferSize; i++)
        {
            input[i] = onsetDF[i];
        }

        Resampler resampler = new Resampler(true, 1, 200);

        resampler.process(1,input, 0, onsetDFBufferSize, true, output,  0, 512);


        for ( int i = 0; i < output.length; i++) {
            resampledOnsetDetectionFunctionData[i] = output[i];
        }
    }

    public void findOnsets(double onsetDetectionFunctionSample, long frame){
        // to ensure that the onset detection onset sample is positive
        double odfSample = Math.abs(onsetDetectionFunctionSample);

        // add a tiny constant to the sample to stop it from ever going
        // to zero. this is to avoid problems further down the line
        odfSample = odfSample + 0.0001;

        m0--;
        beatCounter--;
        beatDueInFrame = false;

        // move all samples back one step
        for (int i = 0; i < (onsetDFBufferSize -1); i++)
        {
            onsetDF[i] = onsetDF[i+1];
        }

        // add new sample at the end
        onsetDF[onsetDFBufferSize -1] = (float) odfSample;

        // update cumulative score
        updateCumulativeScore(odfSample);

        // if we are halfway between beats
        if (m0 == 0)
        {
            predictBeat();
        }

        // if we are at a beat
        if (beatCounter == 0)
        {
            beatDueInFrame = true;	// indicate a beat should be output
            onsetHandler.handleOnset(getBeatTimeInSeconds(frame, hopSize, (int) Shared.SAMPLE_RATE), estimatedTempo);
            // recalculate the tempo
            calculateTempo();
        }
    }

    /**
     * Recursive function building the weighted sum of the current
     * ODF sample and the cumulative score from the past at the most
     * likely position
     *
     * @param odfSample
     */
    private void updateCumulativeScore(double odfSample) {
        float max;
        float wcumscore;

        // two beat periods in the past
        final int start = onsetDFBufferSize - Math.round(2 * beatPeriod);
        // half a beat period in the past
        final int end = onsetDFBufferSize - Math.round(beatPeriod / 2);
        // interval in the past that is going to be searched
        final int winsize = end-start+1;
        final float[] window = new float[winsize];
        // most likely beat over the interval
        // allowed are vals in range of -|2*beatperiod| and -|beatperiod/2|
        double mostLikelyBeat = -2 * beatPeriod;

        // create window
        for (int i = 0;i < winsize;i++)
        {
            // to ensure that data exactly beatPeriod samples in the past is preferred over other data,
            // a weighting factor (log Gaussian transition weighting) is introducedy
            window[i] = (float) Math.exp((-1*Math.pow(tightness * Math.log(-mostLikelyBeat/ beatPeriod), 2)) / 2);

            mostLikelyBeat = mostLikelyBeat+1;
        }

        /* find the most likely beat position in the past */
        max = 0;
        int n = 0;
        for (int i=start;i <= end;i++) {
            // calculate new cumulative score value (max) from cumulative score and weighting factor
            wcumscore = cumulativeScore[i] * window[n];

            if (wcumscore > max) {
                // replace max if new score bigger
                max = wcumscore;
            }
            n++;
        }

        // shift cumulative score back one
        for (int i = 0; i < (onsetDFBufferSize -1); i++) {
            cumulativeScore[i] = cumulativeScore[i+1];
        }

        /* set new score and apply weighting   */
        /* tightness of transition weighting window (default: 5)*/
        final float alpha = 0.9f;
        // add the new score at the end
        cumulativeScore[onsetDFBufferSize -1] = (float) (((1 - alpha) * odfSample) + (alpha * max));

    }

    /**
     * Regular tempo updates
     */
    private void calculateTempo() {
        // adaptive threshold on input
        adaptiveThreshold(resampledOnsetDetectionFunctionData,512);

        // calculate auto-correlation onset of detection onset
        calculateBalancedACF(resampledOnsetDetectionFunctionData);

        // calculate output of comb filterbank
        calculateOutputOfCombFilterBank();

        // adaptive threshold on rcf
        adaptiveThreshold(combFilterBankOutput,128);


        int index;
        int index2;
        // calculate tempo observation vector from beat period observation vector
        for (int i = 0;i < 41;i++)
        {
            double tempoToLagFactor = 60. * (Shared.SAMPLE_RATE / (float) Shared.HOPSIZE);
            index = (int) Math.round(tempoToLagFactor / ((double) ((2*i)+80)));
            index2 = (int) Math.round(tempoToLagFactor / ((double) ((4*i)+160)));

            tempoObservationVector[i] = combFilterBankOutput[index-1] + combFilterBankOutput[index2-1];
        }


        double maxval;
        double maxind;
        double curval;

        // if tempo is fixed then always use a fixed set of tempi as the previous observation probability onset
        boolean tempoFixed = false;
        if (tempoFixed)
        {
            for (int k = 0;k < 41;k++)
            {
                previousDeltas[k] = previousDeltasFixed[k];
            }
        }

        for (int j=0;j < 41;j++)
        {
            maxval = -1;
            for (int i = 0;i < 41;i++)
            {
                curval = previousDeltas[i]* tempoTransitionMatrix[i][j];

                if (curval > maxval)
                {
                    maxval = curval;
                }
            }

            deltas[j] = maxval* tempoObservationVector[j];
        }


        normaliseArray(deltas,41);

        maxind = -1;
        maxval = -1;

        for (int j=0;j < 41;j++)
        {
            if (deltas[j] > maxval)
            {
                maxval = deltas[j];
                maxind = j;
            }

            previousDeltas[j] = deltas[j];
        }

        beatPeriod = Math.round((60.0*Shared.SAMPLE_RATE)/(((2*maxind)+80)*((double) hopSize)));

        if (beatPeriod > 0)
        {
            estimatedTempo = 60.0/((((double) hopSize) / Shared.SAMPLE_RATE)* beatPeriod);
        }
    }

    private void normaliseArray(double[] array, int length) {
        double sum = 0;

        for (int i = 0;i < length;i++)
        {
            if (array[i] > 0)
            {
                sum = sum + array[i];
            }
        }

        if (sum > 0)
        {
            for (int i = 0;i < length;i++)
            {
                array[i] = array[i] / sum;
            }
        }

    }

    private void calculateOutputOfCombFilterBank() {
        int numelem = 4;

        for (int i = 0;i < 128;i++) {
            combFilterBankOutput[i] = 0;
        }


        for (int i = 2;i <= 127;i++) // max beat period
        {
            for (int a = 1;a <= numelem;a++) // number of comb elements
            {
                for (int b = 1-a;b <= a-1;b++) // general state using normalisation of comb elements
                {
                    // calculate value for comb filter row
                    combFilterBankOutput[i-1] = combFilterBankOutput[i-1] + (acf[(a*i+b)-1]* weightingVector[i-1])/(2*a-1);
                }
            }
        }
    }

    private void calculateBalancedACF(double[] onsetDF) {
        int l;
        int n;
        double sum;
        double tmp;


        // for l lags from 0-511
        for (l = 0;l < 512;l++)
        {
            sum = 0;

            // for n samples from 0 - (512-lag)
            for (n = 0;n < (512-l);n++)
            {
                tmp = onsetDF[n] * onsetDF[n+l];	// multiply current sample n by sample (n+l)
                sum = sum + tmp;	// add to sum
            }

            acf[l] = sum / (512-l);		// weight by number of mults and add to acf buffer
        }
    }

    private void adaptiveThreshold(double[] onsetDF, int dfSize) {
        int i;
        int k;
        int t;

        double[] thresholds = new double[dfSize];

        int post = 7;
        int pre = 8;

        t = Math.min(dfSize,post);	// what is smaller, post or df size. This is to avoid accessing outside of arrays

        // find threshold for first 't' samples, where a full average cannot be computed yet
        for (i = 0;i <= t;i++)
        {
            k = Math.min(i+pre,dfSize);
            thresholds[i] = calculateMeanOfArray(onsetDF,1,k);
        }
        // find threshold for bulk of samples across a moving average from [i-pre,i+post]
        for (i = t+1;i < dfSize-post;i++)
        {
            // use Mean from apache commons-math?
            thresholds[i] = calculateMeanOfArray(onsetDF,i-pre,i+post);
        }
        // for last few samples calculate threshold, again, not enough samples to do as above
        for (i = dfSize-post;i < dfSize;i++)
        {
            k = Math.max(i-post,1);
            thresholds[i] = calculateMeanOfArray(onsetDF,k,dfSize);
        }

        // subtract the threshold from the detection onset and check that it is not less than 0
        for (i = 0;i < dfSize;i++)
        {
            onsetDF[i] = onsetDF[i] - thresholds[i];
            if (onsetDF[i] < 0)
            {
                onsetDF[i] = 0;
            }
        }
    }

    private double calculateMeanOfArray(double[] array, int startIndex, int endIndex) {
        int i;
        double sum = 0;

        int length = endIndex - startIndex;

        // find sum
        for (i = startIndex;i < endIndex;i++)
        {
            sum = sum + array[i];
        }

        if (length > 0)
        {
            return sum / length;	// average and return
        }
        else
        {
            return 0;
        }

    }

    private void predictBeat() {
        int windowSize = (int) beatPeriod;
        double[] futureCumulativeScore = new double[onsetDFBufferSize + windowSize];
        double[] futureWindow = new double[windowSize];
        // copy cumscore to first part of fcumscore
        for (int i = 0; i < onsetDFBufferSize; i++)
        {
            futureCumulativeScore[i] = cumulativeScore[i];
        }

        // create future window
        double v = 1;
        for (int i = 0;i < windowSize;i++)
        {
            futureWindow[i] = Math.exp((-1*Math.pow(v - (beatPeriod /2),2))   /  (2*Math.pow(beatPeriod /2 ,2)));
            v++;
        }

        // create past window
        v = -2* beatPeriod;
        int start = onsetDFBufferSize - Math.round(2* beatPeriod);
        int end = onsetDFBufferSize - Math.round(beatPeriod /2);
        int pastwinsize = end-start+1;
        double[] pastWindow = new double[pastwinsize];

        for (int i = 0;i < pastwinsize;i++)
        {
            pastWindow[i] = Math.exp((-1*Math.pow(tightness *Math.log(-v/ beatPeriod),2))/2);
            v = v+1;
        }



        // calculate future cumulative score
        double max;
        int n;
        double wcumscore;
        for (int i = onsetDFBufferSize; i < (onsetDFBufferSize +windowSize); i++)
        {
            start = i - Math.round(2* beatPeriod);
            end = i - Math.round(beatPeriod /2);

            max = 0;
            n = 0;
            for (int k=start;k <= end;k++)
            {
                wcumscore = futureCumulativeScore[k]*pastWindow[n];

                if (wcumscore > max)
                {
                    max = wcumscore;
                }
                n++;
            }

            futureCumulativeScore[i] = max;
        }


        // predict beat
        max = 0;
        n = 0;

        for (int i = onsetDFBufferSize; i < (onsetDFBufferSize +windowSize); i++)
        {
            wcumscore = futureCumulativeScore[i]*futureWindow[n];

            if (wcumscore > max)
            {
                max = wcumscore;
                beatCounter = n;
            }

            n++;
        }
        // set next prediction time
        m0 = beatCounter +Math.round(beatPeriod /2.0f);

    }

    @Override
    public boolean isBeatDueInFrame() {
        return beatDueInFrame;
    }



    @Override
    public void setHandler(OnsetHandler handler) {
        onsetHandler = handler;
    }

    public double getBeatTimeInSeconds(long frameNumber,int hopSize, int samplingFrequency) {
        return ((double) hopSize / (double) samplingFrequency) * (double) frameNumber;
    }

}