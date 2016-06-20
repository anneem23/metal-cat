package org.anneem23.metal.cat.body;

import org.anneem23.metal.cat.audio.AudioDispatcher;
import org.anneem23.metal.cat.audio.AudioInputStreamProcessor;
import org.anneem23.metal.cat.audio.AudioProcessor;

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

    private final ExecutorService executorService;

    private final AudioDispatcher audioDispatcher;

    public Ear(AudioDispatcher dispatcher) {
        audioDispatcher = dispatcher;
        executorService = Executors.newFixedThreadPool(10);
    }

    public Ear(String filename) throws IOException, UnsupportedAudioFileException {
        executorService = Executors.newFixedThreadPool(10);
        AudioProcessor streamProcessor = new AudioInputStreamProcessor(new File(filename));
        audioDispatcher = new AudioDispatcher(streamProcessor);
    }


    public AudioDispatcher getDispatcher() {
        return audioDispatcher;
    }


    public void listen() throws IOException, UnsupportedAudioFileException {
        executorService.execute(audioDispatcher::run);
    }

}
