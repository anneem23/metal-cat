package org.anneem23.metal.cat.audio;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AudioDispatcher feeds listeners of type {@link } with audio samples.
 * <p>
 *
 * @author anneem23
 * @version 2.0
 */
public class AudioDispatcher implements Runnable {

    private final CopyOnWriteArrayList<AudioSampleListener> _listeners = new CopyOnWriteArrayList();
    private final TargetDataLine _line;

    public AudioDispatcher(TargetDataLine targetDataLine) {
        _line = targetDataLine;
    }


    public void register(AudioSampleListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void run() {
        final AudioProcessor audioProcessor = new TargetDataLineProcessor(_line);
        final AudioSampleConverter sampleConverter = new AudioSampleConverter(audioProcessor.getFormat());

        try {
            _line.start();
            _line.open(audioProcessor.getFormat());

            while (audioProcessor.bytesAvailable()) {
                byte[] audioData = audioProcessor.readBytes(Shared.FRAME_SIZE);

                dispatch(sampleConverter.convert(audioData));
            }
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        } finally {
            _line.close();
        }
    }

    private void dispatch(double[] audioSamples) {
        for (AudioSampleListener listener : _listeners)
            listener.updateSamples(audioSamples);
    }
}
