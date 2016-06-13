package org.anneem23.metal.cat.audio;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AudioDispatcher feeds listeners of type {@link } with audio samples.
 * <p>
 *
 * @author anneem23
 */
public class AudioDispatcher implements Runnable {

    private final CopyOnWriteArrayList<AudioSampleListener> _listeners = new CopyOnWriteArrayList();
    private final AudioProcessor _audioProcessor;

    public AudioDispatcher(AudioProcessor audioProcessor) {
        _audioProcessor = audioProcessor;
    }


    public void register(AudioSampleListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void run() {
        final AudioSampleConverter sampleConverter = new AudioSampleConverter(_audioProcessor.getFormat());

        try {
            _audioProcessor.openStream();
            while (_audioProcessor.bytesAvailable()) {
                byte[] audioData = _audioProcessor.readBytes(Shared.FRAME_SIZE);

                dispatch(sampleConverter.convert(audioData));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatch(double[] audioSamples) {
        for (AudioSampleListener listener : _listeners)
            listener.updateSamples(audioSamples);
    }
}
