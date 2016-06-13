package org.anneem23.metal.cat.body;

import org.anneem23.metal.cat.audio.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ear uses {@link AudioDispatcher} to process different kinds
 * of audio input
 * <p>
 *
 * @author anneem23
 */
public class Ear {

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(Shared.SAMPLE_RATE, 16, 1, true, false);

    private final ExecutorService _executorService;

    private final AudioDispatcher _dispatcher;

    public Ear(AudioDispatcher dispatcher) {
        _dispatcher = dispatcher;
        _executorService = Executors.newFixedThreadPool(10);
    }

    public Ear(String filename) throws IOException, UnsupportedAudioFileException {
        _executorService = Executors.newFixedThreadPool(10);
        AudioProcessor streamProcessor = new AudioInputStreamProcessor(new File(filename));
        _dispatcher = new AudioDispatcher(streamProcessor);
    }


    public AudioDispatcher getDispatcher() {
        return _dispatcher;
    }


    public void listen() throws IOException, UnsupportedAudioFileException {
        _executorService.execute(new Runnable() {
            public void run() {
                _dispatcher.run();
            }
        });
    }

}
