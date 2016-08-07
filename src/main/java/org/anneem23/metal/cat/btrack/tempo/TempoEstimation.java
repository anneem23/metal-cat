package org.anneem23.metal.cat.btrack.tempo;

import org.anneem23.metal.cat.audio.Shared;

import java.util.Arrays;

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

    protected static final double MIN_BPM = 80;
    protected static final double MAX_BPM = 160;

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


    public TempoEstimation(int hopSize, float sampleRate) {
        this.hopSize = hopSize;
        // initialize beat period with 120 bpm
        this.beatPeriod = Math.round(60 / ((((double) hopSize) / sampleRate) * 120));

        // tempo
        this.tempoObservationVector = new double[41];
        // deltas
        this.deltas = new double[41];
        // weighting
        this.weightingVector = new float[128];

        // create rayleigh weighting vector
        final double rayparam = 43;
        for (int n = 0;n < 128;n++){
            this.weightingVector[n] = (float) weightingFactor(rayparam, n);
        }

        // We enforce some dependence on consecutive tempo estimates
        // by finding the current tempo tb based on the previous estimate
        // tb−1
        this.previousDeltas = new double[41];
        for (int i = 0;i < 41;i++) {
            this.previousDeltas[i] = 1;
        }


        // create tempo transition matrix tempoTransitionMatrix(ti, tj) where each column is a Gaussian
        // of fixed standard deviation σ = (tmax − tmin)/8 and ti, tj = 1, . . . ,(tmax − tmin).
        this.tempoTransitionMatrix = new double[41][41];
        for (int i = 0;i < 41;i++) {
            for (int j = 0;j < 41;j++) {
                final double x = (double) j + 1;
                final double tMu = (double) i + 1;
                // standard deviation σ = (tmax − tmin)/8
                double fixedStandardDerivation = (double) 41 / 8;
                tempoTransitionMatrix[i][j] = (1 / (fixedStandardDerivation * Math.sqrt(2*Math.PI))) * gaussian(x, tMu, fixedStandardDerivation);
            }
        }
    }

    public double tempo() {
        return estimatedTempo;
    }

    public float beatPeriod() {
        return beatPeriod;
    }

    /**
     * Tempo estimation (and hence beat period τb) is based on components from
     * the two state model of Davies and Plumbley.
     *
     * To minimise the common beat tracking error of switching between
     * metrical levels we restrict the range of tempi to one tempo octave from
     * 80 beats per minute (bpm) to 160 bpm.
     *
     * The method can be summarised in the following five steps:
     *     i) we extract a six second analysis frame (up to m0 from (4)) from
     *     the onset detection function Γ(m);
     *
     *     ii) we preserve the peaks in Γ(m) by
     *     applying an adaptive moving mean threshold to leave a modified detection
     *     function Γ( ˜ m);
     *
     *     iii) we take the auto-correlation function of Γ( ˜ m);
     *
     *     iv) we pass the auto-correlation function through a shift-invariant comb
     *     filterbank weighted by a tempo preference curve;
     *
     *     and v) we find the beat period as in the index of the maximum value of the
     *     comb filterbank output, R(l)
     *
     * @param data six second analysis frame (up to nextPrediction) from the onset
     *             detection funtion
     */
    public void calculateTempo(double[] data) {
//        System.out.println(Arrays.toString(data));
        // adaptive threshold on input (II)
        final double[] adaptiveThreshold = adaptiveThreshold(data, 512);

        // calculate auto-correlation onset of detection onset (III)
        final double[] balancedAutoCorrelationFunction = calculateBalancedAutoCorrelationFunction(adaptiveThreshold);

        // calculate output of comb filterbank (IV)
        double[] combFilterBankOutput = calculateOutputOfCombFilterBank(balancedAutoCorrelationFunction);

        // adaptive threshold on rcf
        combFilterBankOutput = adaptiveThreshold(combFilterBankOutput,128);

        // calculate tempo observation vector from beat period observation vector (V)
        // we track beats in the range of 80 - 160 bpm
        for (int i = 0;i < 41;i++) {
            // the temporal resolution of the onset detection function in seconds
            final double tempoToLagFactor = 60. * (Shared.SAMPLE_RATE / (float) Shared.HOP_SIZE);
            // lower bounds at 80 bpm
            final int index = (int) Math.round(tempoToLagFactor / ((2 * i) + MIN_BPM));
            // upper bounds at 160 bpm
            final int index2 = (int) Math.round(tempoToLagFactor / ((4 * i) + MAX_BPM));

            tempoObservationVector[i] = combFilterBankOutput[index-1] + combFilterBankOutput[index2-1];
        }

        // At each new iteration, we store the maximum value of the
        // product of each column of tempoTransitionMatrix with the
        // stored state probabilities previousDeltas from the
        // previous iteration
        for (int j=0;j < 41;j++) {
            double maxval = -1;
            for (int i = 0;i < 41;i++) {
                final double curval = previousDeltas[i] * tempoTransitionMatrix[i][j];
                if (curval > maxval) {
                    maxval = curval;
                }
            }
            // Update delta[i] to reflect the tempo range comb filter output
            // for the current beat frame tempoObservationVector[j] by taking
            // the element-wise product of the two signals
            deltas[j] = maxval * tempoObservationVector[j];
        }

        // To prevent deltas from growing exponentially or approaching zero at each
        // iteration we normalise it to sum to unity
        normaliseArray(deltas);


        // We then find the current tempo beatPeriod as the index of the maximum value
        // of deltas - maxIdx
        double maxIdx = -1;
        double maxVal = -1;

        for (int j=0;j < 41;j++) {
            if (deltas[j] > maxVal) {
                maxVal = deltas[j];
                maxIdx = j;
            }
            previousDeltas[j] = deltas[j];
        }

        //TODO why this? round((60.0*44100.0)/(((2*maxidx)+80)*((double) hopSize)));
        beatPeriod = Math.round((60.0 * Shared.SAMPLE_RATE) / (((2 * maxIdx) + MIN_BPM) * ((double) hopSize)));

        if (beatPeriod > 0) {
            // estimatedTempo = | 60.0 / (0.01161 * t) |
            estimatedTempo = 60.0 / ((((double) hopSize) / Shared.SAMPLE_RATE) * beatPeriod);
        }
    }


    /**
     * Auto-correlation function of modified detection function
     *
     * Adds balance between existing and past data in cumulative score (default: 0.9)
     *
     * @param onsetDF modified detection function
     * @return auto-correlation function of modified detection function
     */
    private double[] calculateBalancedAutoCorrelationFunction(double[] onsetDF) {
        //
        final double[] autoCorrelationFunction = new double[hopSize];

        // for l lags from 0-511
        for (int l = 0;l < 512;l++) {
            double sum = 0;
            // for n samples from 0 - (512-lag)
            for (int n = 0;n < (512-l);n++) {
                // multiply current sample n by sample (n+l) and add it to sum
                sum += (onsetDF[n] * onsetDF[n+l]);
            }
            // weight by number of mults and add to autoCorrelationFunction buffer
            autoCorrelationFunction[l] = sum / (512-l);
        }

        return autoCorrelationFunction;
    }

    /**
     * Preserve the peaks in onsetDF by applying an adaptive moving mean threshold to leave a modified detection
     * function modified detection function
     *
     * @param onsetDF onset detection function data
     * @param dfSize size of onsetDF
     * @return modified detection function of modified detection function??
     */
    private double[] adaptiveThreshold(double[] onsetDF, int dfSize) {
        final double[] thresholds = new double[dfSize];
        final double[] result = new double[dfSize];

        final int post = 7;
        final int pre = 8;
        // what is smaller, post or df size. This is to avoid accessing outside of arrays
        final int t = Math.min(dfSize, post);

        // find threshold for first 't' samples, where a full average cannot be computed yet
        for (int i = 0;i <= t;i++) {
            final int k = Math.min(i + pre, dfSize);
            thresholds[i] = calculateMeanOfArray(onsetDF,1,k);
        }
        // find threshold for bulk of samples across a moving average from [i-pre,i+post]
        for (int i = t+1;i < dfSize-post;i++) {
            // use Mean from apache commons-math?
            thresholds[i] = calculateMeanOfArray(onsetDF,i-pre,i+post);
        }
        // for last few samples calculate threshold, again, not enough samples to do as above
        for (int i = dfSize-post;i < dfSize;i++) {
            final int k = Math.max(i - post, 1);
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
     * Returns arithmetic mean of array, the sum of the values divided by the
     * number of items in the array
     *
     *  (array[startIndex]+array[startIndex+1]+...+array[endIndex]) / (endIndex - startIndex)
     *
     * @param array input values
     * @param startIndex start index
     * @param endIndex end index
     * @return arithmetic mean of all values from startIndex to endIndex
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
     * Normalize the array so that the minimum and maximum values are 0 and 1
     *
     * @param array the data to be normalized
     */
    private void normaliseArray(double[] array) {
        double sum = 0;
        for (final double value : array) {
            if (value > 0) {
                sum = sum + value;
            }
        }

        if (sum == 0) {
            return;
        }

        for (int i = 0;i < array.length;i++) {
            array[i] = array[i] / sum;
        }

    }

    /**
     * Calculate shift-invariant comb filter bank weighted by a tempo preference curve
     *
     * @param autoCorrelationFunction auto-correlation function of Γ( ˜ m)
     * @return shift-invariant, weighted comb filter bank
     */
    private double[] calculateOutputOfCombFilterBank(double[] autoCorrelationFunction) {
        final double[] combFilterBankOutput = new double[128];

        for (int i = 0;i < 128;i++) {
            combFilterBankOutput[i] = 0;
        }

        final int numElem = 4;
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
     * weighting factor utility method to create rayleigh weighting vector
     * see: Rayleigh mixture distribution
     *
     * @param rayParam
     * @param n
     * @return
     */
    private static double weightingFactor(double rayParam, double n) {
        //TODO (n / rayParam^2) * exp(( -1 * -n^2) / (2 * rayParam^2))
        //TODO (n / rayParam^2) * gaussian(n, rayParam)?
        return (n / Math.pow(rayParam, 2)) * Math.exp((-1 * Math.pow(-n, 2)) / (2 * Math.pow(rayParam, 2)));
    }

    //TODO is that really a gaussian function?
    private static double gaussian(double ti, double tj, double omega) {
        return Math.exp( (-1*Math.pow(ti- tj,2)) / (2*Math.pow(omega,2)));
    }
}
