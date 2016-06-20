package org.anneem23.metal.cat.beat;

import be.tarsos.dsp.resample.Resampler;
import org.anneem23.metal.cat.audio.Shared;
import org.anneem23.metal.cat.beat.onset.OnsetDetectionFunction;

import java.io.IOException;

/**
 * BeatTracker is a java implementation of Adam Starks BTrack algorithm.
 * <p>
 * It does live beat tracking with a combination of two well-known algorithms for
 * beat prediction and tempo estimation.
 * <p>
 * <p>{@link TempoEstimation} is based on the Davies and Plumbley method
 * <p>{@link BeatPrediction} is based on Ellis' dynamic programming beat
 *
 */
public class BeatTracker implements BeatTrackingAlgorithm {

    private final OnsetDetectionFunction onsetDetectionFunction;
    private final BeatPrediction beatPrediction;

    /** adds balance between existing and past data in cumulative score (default: 0.9) */
    private final double[] acf;
    private final int hopSize;



    private double estimatedTempo;
    /**
     * tempo transition matrix
     */
    private final double[][] tempoTransitionMatrix = new double[41][41];
    private final double[] tempoObservationVector = new double[41];

    private boolean beatDueInFrame;

    /**
     * the time (in DF samples) between two beats
     */
    private float beatPeriod;
    private final int onsetDFBufferSize;
    private final float[] onsetDF;

    private final double[] combFilterBankOutput;
    private final float[] weightingVector = new float[128];
    /**
     * to hold final tempo candidate array
     */
    private final double[] deltas = new double[41];
    /**
     * previous delta
     */
    private final double[] previousDeltas = new double[41];
    private final double[] resampledOnsetDetectionFunctionData;


    public BeatTracker(int hopSize, OnsetDetectionFunction onsetDetector, float sampleRate) throws IOException {
        onsetDetectionFunction = onsetDetector;
        onsetDFBufferSize = (512*512)/hopSize;
        onsetDF = new float[onsetDFBufferSize];
        resampledOnsetDetectionFunctionData = new double[512];

        // initialize beat period with 120 bpm
        beatPeriod = Math.round(60/((((double) hopSize)/ sampleRate)* 120));
        // initialize estimated tempo with 120 bpm
        estimatedTempo = 120.0;
        this.hopSize = hopSize;

        acf = new double[hopSize];
        combFilterBankOutput = new double[128];

        beatPrediction = new BeatPrediction(onsetDFBufferSize);

        initializeArrays();
    }

    @Override
    public void processAudioFrame(double[] audioBuffer) {
        double odfSample = onsetDetectionFunction.calculateOnsetDetectionFunctionSample(audioBuffer);
        processOnsetDetectionFunctionSample(odfSample);
    }

    private void processOnsetDetectionFunctionSample(double onsetDetectionFunctionSample) {
        // to ensure that the onset detection onset sample is positive
        // add a tiny constant to the sample to stop it from ever going
        // to zero. this is to avoid problems further down the line
        double odfSample = Math.abs(onsetDetectionFunctionSample) + 0.0001;

        // move all samples back one step
        System.arraycopy(onsetDF, 1, onsetDF, 0, onsetDFBufferSize - 1);

        // add new sample at the end
        onsetDF[onsetDFBufferSize -1] = (float) odfSample;

        beatDueInFrame = false;
        // beat prediction
        beatPrediction.processSample(odfSample, beatPeriod);

        // if we are at a beat
        if (beatPrediction.beatDetected()) {
            // indicate a beat should be output
            beatDueInFrame = true;
            // resample odf samples 
            resampleOnsetDetectionFunction();
            // recalculate the tempo
            calculateTempo();
        }
    }


    private void resampleOnsetDetectionFunction() {
        float[] output = new float[512];
        float[] input = new float[onsetDFBufferSize];

        System.arraycopy(onsetDF, 0, input, 0, onsetDFBufferSize);

        Resampler resampler = new Resampler(true, 1, 200);
        resampler.process(1,input, 0, onsetDFBufferSize, true, output,  0, 512);

        for ( int i = 0; i < output.length; i++) {
            resampledOnsetDetectionFunctionData[i] = output[i];
        }
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


        for (int j=0;j < 41;j++) {
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

        if (beatPeriod > 0) {
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


    @Override
    public boolean isBeatDueInFrame() {
        return beatDueInFrame;
    }

    public double getBeatTimeInSeconds(long frameNumber,int hopSize, int samplingFrequency) {
        return ((double) hopSize / (double) samplingFrequency) * (double) frameNumber;
    }





    private void initializeArrays() {
        // initialise df_buffer to zeros
        for (int i = 0; i < onsetDFBufferSize; i++)
        {
            onsetDF[i] = 0;

            if ((i %  Math.round(beatPeriod)) == 0) {
                onsetDF[i] = 1;
            }

        }

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


}