package org.anneem23.metal.cat.btrack.tempo;

import org.anneem23.metal.cat.audio.Shared;

/**
 * Tempo estimation is based on the two state model of Davies and Plumbley
 * <p>
 * <p> The method can be summarised in the following five steps:
 *     i) we extract a six second analysis frame (up to m0 from (4)) from
 *     the onset detection function Γ(m);
 *
 *     ii) we preserve the peaks in Γ(m) by
 *     applying an adaptive moving mean threshold to leave a modified detection
 *     function Γ( ˜ m);
 *
 *     iii) we take the autocorrelation function of Γ( ˜ m);
 *
 *     iv) we pass the autocorrelation function through a shift-invariant comb
 *     filterbank weighted by a tempo preference curve;
 *
 *     and v) we find the beat period as in the index of the maximum value of the
 *     comb filterbank output, R(l)
 *
 * @author anneem23
 */
public class TempoEstimation {

    private static final int MIN_BPM = 80;
    private static final int MAX_BPM = 160;

    private final int hopSize;
    private double estimatedTempo;

    /**
     * tempo transition matrix
     */
    private final double[][] tempoTransitionMatrix;
    private final double[] tempoObservationVector;
    /**
     * the time (in DF samples) between two beats
     */
    private float beatPeriod;

    private final float[] weightingVector;
    /**
     * to hold final tempo candidate array
     */
    private final double[] deltas;
    /**
     * previous delta
     */
    private final double[] previousDeltas;


    public TempoEstimation(int hopSize) {
        this.hopSize = hopSize;

        // tempo
        this.tempoObservationVector = new double[41];
        // deltas
        this.deltas = new double[41];

        final double rayparam = 43;
        this.weightingVector = new float[128];
        // create rayleigh weighting vector
        for (int n = 0;n < 128;n++){
            this.weightingVector[n] = (float) weightingFactor(rayparam, n);
        }

        // initialise prev_delta
        this.previousDeltas = new double[41];
        for (int i = 0;i < 41;i++) {
            this.previousDeltas[i] = 1;
        }


        // create tempo transition matrix
        this.tempoTransitionMatrix = new double[41][41];
        for (int i = 0;i < 41;i++) {
            for (int j = 0;j < 41;j++) {
                double x = (double) j + 1;
                double tMu = (double) i + 1;
                double mSig = (double) 41 / 8;
                tempoTransitionMatrix[i][j] = (1 / (mSig * Math.sqrt(2*Math.PI))) * Math.exp( (-1*Math.pow(x- tMu,2)) / (2*Math.pow(mSig,2)) );
            }
        }
    }



    public double tempo() {
        return estimatedTempo;
    }

    /**
     * Tempo estimation (and hence beat period τb) is based on components from
     * the two state model of Davies and Plumbley.
     *
     * The method can be summarised in the following five steps:
     *     i) we extract a six second analysis frame (up to m0 from (4)) from
     *     the onset detection function Γ(m);
     *
     *     ii) we preserve the peaks in Γ(m) by
     *     applying an adaptive moving mean threshold to leave a modified detection
     *     function Γ( ˜ m);
     *
     *     iii) we take the autocorrelation function of Γ( ˜ m);
     *
     *     iv) we pass the autocorrelation function through a shift-invariant comb
     *     filterbank weighted by a tempo preference curve;
     *
     *     and v) we find the beat period as in the index of the maximum value of the
     *     comb filterbank output, R(l)
     *
     * @param data six second analysis frame (up to nextPrediction) from the onset
     *             detection funtion
     */
    public void calculateTempo(double[] data) {
        // adaptive threshold on input (II)
        double[] adaptiveThreshold = adaptiveThreshold(data, 512);

        // calculate auto-correlation onset of detection onset (III)
        double[] balancedACF = calculateBalancedACF(adaptiveThreshold);

        // calculate output of comb filterbank (IV)
        double[] combFilterBankOutput = calculateOutputOfCombFilterBank(balancedACF);

        // adaptive threshold on rcf
        combFilterBankOutput = adaptiveThreshold(combFilterBankOutput,128);

        // calculate tempo observation vector from beat period observation vector (V)
        // we track beats in the range of 80 - 160 bpm
        for (int i = 0;i < 41;i++) {
            double tempoToLagFactor = 60. * (Shared.SAMPLE_RATE / (float) Shared.HOPSIZE);
            int index = (int) Math.round(tempoToLagFactor / ((double) ((2*i)+ MIN_BPM)));
            int index2 = (int) Math.round(tempoToLagFactor / ((double) ((4*i)+ MAX_BPM)));

            tempoObservationVector[i] = combFilterBankOutput[index-1] + combFilterBankOutput[index2-1];
        }


        for (int j=0;j < 41;j++) {
            double maxval = -1;
            for (int i = 0;i < 41;i++) {
                double curval = previousDeltas[i]* tempoTransitionMatrix[i][j];
                if (curval > maxval) {
                    maxval = curval;
                }
            }
            deltas[j] = maxval* tempoObservationVector[j];
        }


        normaliseArray(deltas,41);

        double maxind = -1;
        double maxval = -1;

        for (int j=0;j < 41;j++)
        {
            if (deltas[j] > maxval)
            {
                maxval = deltas[j];
                maxind = j;
            }

            previousDeltas[j] = deltas[j];
        }

        beatPeriod = Math.round((60.0*Shared.SAMPLE_RATE)/(((2*maxind)+ MIN_BPM)*((double) hopSize)));

        if (beatPeriod > 0) {
            estimatedTempo = 60.0/((((double) hopSize) / Shared.SAMPLE_RATE)* beatPeriod);
        }
    }


    /**
     * Auto-correlation function of Γ( ˜ m)
     *
     * Adds balance between existing and past data in cumulative score (default: 0.9)
     *
     * @param onsetDF Γ( ˜ m)
     * @return auto-correlation function of Γ( ˜ m)
     */
    private double[] calculateBalancedACF(double[] onsetDF) {
        //
        double[] acf = new double[hopSize];

        // for l lags from 0-511
        for (int l = 0;l < 512;l++) {
            double sum = 0;
            // for n samples from 0 - (512-lag)
            for (int n = 0;n < (512-l);n++) {
                // multiply current sample n by sample (n+l) and add it to sum
                sum += (onsetDF[n] * onsetDF[n+l]);
            }
            // weight by number of mults and add to acf buffer
            acf[l] = sum / (512-l);
        }

        return acf;
    }

    /**
     * Preserve the peaks in onsetDF by applying an adaptive moving mean threshold to leave a modified detection
     * function Γ( ˜ m)
     *
     * @param onsetDF onset detection function data
     * @param dfSize size of onsetDF
     * @return modified detection function Γ( ˜ m)
     */
    private double[] adaptiveThreshold(double[] onsetDF, int dfSize) {
        double[] thresholds = new double[dfSize];
        double[] result = new double[dfSize];

        int post = 7;
        int pre = 8;
        // what is smaller, post or df size. This is to avoid accessing outside of arrays
        int t = Math.min(dfSize,post);

        // find threshold for first 't' samples, where a full average cannot be computed yet
        for (int i = 0;i <= t;i++) {
            int k = Math.min(i+pre,dfSize);
            thresholds[i] = calculateMeanOfArray(onsetDF,1,k);
        }
        // find threshold for bulk of samples across a moving average from [i-pre,i+post]
        for (int i = t+1;i < dfSize-post;i++) {
            // use Mean from apache commons-math?
            thresholds[i] = calculateMeanOfArray(onsetDF,i-pre,i+post);
        }
        // for last few samples calculate threshold, again, not enough samples to do as above
        for (int i = dfSize-post;i < dfSize;i++) {
            int k = Math.max(i-post,1);
            thresholds[i] = calculateMeanOfArray(onsetDF,k,dfSize);
        }

        // subtract the threshold from the detection onset and check that it is not less than 0
        for (int i = 0;i < dfSize;i++) {
            result[i] = onsetDF[i] - thresholds[i];
            if (onsetDF[i] < 0) {
                onsetDF[i] = 0;
            }
        }

        return result;
    }

    /**
     *
     * @param array
     * @param startIndex
     * @param endIndex
     * @return
     */
    private double calculateMeanOfArray(double[] array, int startIndex, int endIndex) {
        if (startIndex >= endIndex) {
            return 0;
        }

        double sum = 0;
        for (int i = startIndex; i < endIndex; i++) {
            sum = sum + array[i];
        }

        // return average
        return sum / (endIndex - startIndex);
    }

    /**
     *
     * @param array
     * @param length
     */
    private void normaliseArray(double[] array, int length) {
        double sum = 0;

        for (int i = 0;i < length;i++) {
            if (array[i] > 0) {
                sum = sum + array[i];
            }
        }

        if (sum > 0) {
            for (int i = 0;i < length;i++) {
                array[i] = array[i] / sum;
            }
        }

    }

    /**
     * Calculate shift-invariant comb filter bank weighted by a tempo preference curve
     *
     * @param autoCorrelationFunction auto-correlation function of Γ( ˜ m)
     * @return shift-invariant, weighted comb filter bank
     */
    private double[] calculateOutputOfCombFilterBank(double[] autoCorrelationFunction) {
        double[] combFilterBankOutput = new double[128];

        for (int i = 0;i < 128;i++) {
            combFilterBankOutput[i] = 0;
        }

        int numElem = 4;
        // max beat period
        for (int i = 2;i <= 127;i++) {
            // number of comb elements
            for (int a = 1;a <= numElem;a++) {
                // general state using normalisation of comb elements
                for (int b = 1-a;b <= a-1;b++) {
                    // calculate value for comb filter row
                    combFilterBankOutput[i-1] = combFilterBankOutput[i-1] + (autoCorrelationFunction[(a*i+b)-1]* weightingVector[i-1])/(2*a-1);
                }
            }
        }

        return combFilterBankOutput;
    }


    /**
     *
     * @param rayparam
     * @param n
     * @return
     */
    private static double weightingFactor(double rayparam, int n) {
        return ((double) n / Math.pow(rayparam,2)) * Math.exp((-1*Math.pow((double)-n,2)) / (2*Math.pow(rayparam,2)));
    }
}
