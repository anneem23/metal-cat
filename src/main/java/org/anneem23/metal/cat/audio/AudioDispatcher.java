package org.anneem23.metal.cat.audio;

/**
 * AudioDispatcher Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #AudioDispatcher(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class AudioDispatcher implements Runnable {

    private final AudioSampleReader _audioProcessor;

    public AudioDispatcher(AudioSampleReader audioProcessor) {
        _audioProcessor = audioProcessor;
    }

    @Override
    public void run() {

    }
}
