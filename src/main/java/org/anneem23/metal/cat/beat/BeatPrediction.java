package org.anneem23.metal.cat.beat;


import static org.anneem23.metal.cat.beat.weighting.GaussianWeighting.*;

/**
 * Beat prediction is based on Ellis' dynamic programming beat
 *
 * <p>The underlying model for beat tracking assumes that the sequence
 * of beats, beatPeriod, will correspond to a set of approximately periodic
 * peaks in the onset detection function.
 *
 * <p>At the core of this method is the generation of a recursive cumulative
 * score function, C*(m) whose value at m is defined as the weighted sum of
 * the current DF value Γ(m) and the value of C at the most likely previous
 * beat location, C*(m) = (1 − α)Γ(m) + α max(W1(v)C(m + v)).
 *
 * @author anneem23
 */
class BeatPrediction {

    /**
     * the time (in DF samples) between two beats
     */
    private int beatCounter;
    private final float[] cumulativeScore;
    /**
     * indicates when the next point to predict the next beat is
     */
    private int nextPredictionCounter;
    private final double tightness;

    public BeatPrediction(int onsetDFBufferSize) {
        this.tightness = (double) 5;
        this.nextPredictionCounter = 10;
        this.beatCounter = -1;
        this.cumulativeScore = new float[onsetDFBufferSize];
        // set all cumulative scores to 0.0
        for (int i = 0; i < onsetDFBufferSize; i++){
            cumulativeScore[i] = 0;
        }
    }

    /**
     * updates the cumulative score with the new odf sample and
     * runs beat detection
     * @param odfSample onset detection function sample
     * @param beatPeriod time (in DF samples) between two beats
     */
    public void processSample(double odfSample, float beatPeriod) {
        // update cumulative score
        updateCumulativeScore(odfSample, beatPeriod);

        // if we are halfway between beats
        if (nextPredictionCounter == 0) {
            predictBeat(beatPeriod);
        }

    }

    /**
     * Is beat predicted for current frame
     *
     * @return true if beat in current frame else false
     */
    public boolean beatDetected() {
        return beatCounter == 0;
    }

    /**
     * Recursive function building the weighted sum of the current
     * ODF sample and the cumulative score from the past at the most
     * likely position
     *
     * @param odfSample onset detection function sample
     * @param beatPeriod the time (in DF samples) between beats
     */
    private void updateCumulativeScore(double odfSample, float beatPeriod) {
        this.nextPredictionCounter--;
        this.beatCounter--;

        // two beat periods in the past
        final int start = this.cumulativeScore.length - Math.round(2 * beatPeriod);
        // half a beat period in the past
        final int end = this.cumulativeScore.length - Math.round(beatPeriod / 2);
        // most likely beat over the interval in range of -|2*beatperiod| and -|beatperiod/2|
        double mostLikelyBeat = -2 * beatPeriod;
        // find the most likely beat position in the past
        float max = maximumCumulativeScore(cumulativeScore, start, end, mostLikelyBeat, beatPeriod);
        // shift cumulative score back one
        System.arraycopy(cumulativeScore, 1, cumulativeScore, 0, this.cumulativeScore.length - 1);
        // tightness of transition weighting window (default: 5)
        final float alpha = 0.9f;
        // set new score and apply weighting
        cumulativeScore[this.cumulativeScore.length -1] = (float) (((1 - alpha) * odfSample) + (alpha * max));
        
    }

    /**
     * predict when the next beat happens during the next beat period (one-beat-window)
     * the prediction is based on the (historic) cumulative scores collected over the
     * last two beat periods and a gaussian weighting factor centered around the
     * most likely future beat location
     *
     * @param beatPeriod the time (in DF samples) between beats
     */
    private void predictBeat(float beatPeriod) {
        final int windowSize = (int) beatPeriod;
        final float[] futureCumulativeScore = new float[this.cumulativeScore.length + windowSize];
        final double[] futureWindow = new double[windowSize];

        // copy cumulative score to first part of future cumulative score
        System.arraycopy(cumulativeScore, 0, futureCumulativeScore, 0, this.cumulativeScore.length);
        // create future window
        double mostLikelyFutureBeat = 1;

        for (int i = 0;i < windowSize;i++,mostLikelyFutureBeat++) {
            // Gaussian weighting centred on the most likely beat location
            futureWindow[i] = gaussianWeighting(mostLikelyFutureBeat, beatPeriod);
        }
        // create past window
        double mostLikelyPreviousBeat = -2* beatPeriod;
        // calculate future cumulative score
        double windowCumulativeScore;
        for (int i = this.cumulativeScore.length; i < (this.cumulativeScore.length + windowSize); i++) {
            final int start = i - Math.round(2* beatPeriod);
            final int end = i - Math.round(beatPeriod /2);

            futureCumulativeScore[i] = maximumCumulativeScore(futureCumulativeScore, start, end, mostLikelyPreviousBeat, beatPeriod);
        }
        // predict beat
        double max = 0;
        int n = 0;

        for (int i = this.cumulativeScore.length; i < (this.cumulativeScore.length + windowSize); i++) {
            windowCumulativeScore = futureCumulativeScore[i]*futureWindow[n];
            if (windowCumulativeScore > max) {
                max = windowCumulativeScore;
                beatCounter = n;
            }
            n++;
        }
        // set next prediction time
        nextPredictionCounter = beatCounter + Math.round(beatPeriod /2.0f);
    }



    /**
     * applies {@see logGaussianTransitionWeighting} to every value in
     * scores and returns the maximum cumulative score found
     *
     * @param scores array of floats in which to find maximum cumulative score
     * @param start start index position in scores
     * @param end end index position in scores
     * @param mostLikelyBeat most likely beat position
     * @param beatPeriod the time (in DF samples) between two beats
     * @return maximum cumulative score found in {@param scores}
     */
    private float maximumCumulativeScore(float[] scores, int start, int end, double mostLikelyBeat, float beatPeriod) {
        float windowCumulativeScore;
        float max = 0;
        double mlb = mostLikelyBeat;
        for (int i=start;i <= end;i++,mlb++) {
            float weightingFactor = (float) logGaussianTransitionWeighting(tightness, mlb, beatPeriod);
            // calculate new cumulative score value (max) from cumulative score and weighting factor
            windowCumulativeScore = scores[i] * weightingFactor;
            if (windowCumulativeScore > max) {
                // replace max if new score bigger
                max = windowCumulativeScore;
            }
        }
        return max;
    }

}
