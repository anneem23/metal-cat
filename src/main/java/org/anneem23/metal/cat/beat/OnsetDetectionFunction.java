package org.anneem23.metal.cat.beat;

/**
 * OnsetDetectionFunction Interface with api method defintion
 * to calculate the sample on a given audio buffer chunk.
 *
 * @author anneem23
 * @version 2.0
 */
public interface OnsetDetectionFunction {

    double calculateOnsetDetectionFunctionSample(double[] audioBuffer);
}
