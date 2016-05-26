package org.anneem23.metal.cat.algorithm.onset;

import be.tarsos.dsp.AudioEvent;

/**
 * OnsetDetectionFunction Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #OnsetDetectionFunction(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public interface OnsetDetectionFunction {
    double onsetDetection(AudioEvent audioEvent);
}
