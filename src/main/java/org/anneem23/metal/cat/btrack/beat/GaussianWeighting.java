package org.anneem23.metal.cat.btrack.beat;

/**
 * Gaussian Weighting functions used by {@link org.anneem23.metal.cat.btrack.beat.EllisDynamicProgrammingBeatPrediction}.
 *
 * @author anneem23
 */
public class GaussianWeighting {

    private GaussianWeighting() {
    }

    /**
     * Gaussian weighting centred on the most likely beat location
     * (nextPredictionCounter + beatPeriod/2)
     *
     * @param v = 1, . . . , beatPeriod specifies the future one-beat window
     * @param beatPeriod the time (in DF samples) between beats
     * @return weighting (centred on the most likely beat location)
     */
    public static double gaussianWeighting(double v, double beatPeriod) {
        return Math.exp((-1 * Math.pow(v - (beatPeriod / 2), 2)) / (2 * Math.pow(beatPeriod / 2, 2)));
    }

    /**
     *  To give most preference to the information exactly {@param beatPeriod} samples into the past,
     *  we multiply C∗ by a log-Gaussian transition weighting
     *
     * @param tightness tightness of the transition weighting (defaults to 5.0)
     * @param v search interval for the most likely beat
     * @param beatPeriod the time (in DF samples) between beats
     * @return weighting factor
     */
    public static double logGaussianTransitionWeighting(double tightness, double v, double beatPeriod) {
        return Math.exp((-1 * Math.pow(tightness * Math.log(-v/ beatPeriod),2)) / 2);
    }
}
