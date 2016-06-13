package org.anneem23.metal.cat.beat;

/**
 * AudioProcessingAlgorithm Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #AudioProcessingAlgorithm(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public interface BeatTrackingAlgorithm {
    void processAudioFrame(double[] audioBuffer);

    boolean isBeatDueInFrame();
}
