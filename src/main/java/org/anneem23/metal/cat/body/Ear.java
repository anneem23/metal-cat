package org.anneem23.metal.cat.body;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import org.anneem23.metal.cat.input.Shared;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ear Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 *
 * @author anneem23
 * @version 2.0
 */
public class Ear {

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(Shared.SAMPLE_RATE, 16, 1, true, false);


    private final ExecutorService executorService;

    private final TargetDataLine _line;
    private final AudioDispatcher _dispatcher;

    public Ear(TargetDataLine line) {
        _line = line;

        JVMAudioInputStream audioStream = new JVMAudioInputStream(new AudioInputStream(_line));
        // create a new dispatcher
        _dispatcher = new AudioDispatcher(audioStream, Shared.BUFFER_SIZE, Shared.OVERLAP);
        executorService = Executors.newFixedThreadPool(10);
    }

    public AudioDispatcher getDispatcher() {
        return _dispatcher;
    }

    public void listen() throws LineUnavailableException {
        System.out.println("Metal cat starts to listen for music!");
        _line.open(AUDIO_FORMAT, Shared.BUFFER_SIZE);
        _line.start();

        executorService.execute(new Runnable() {
            public void run() {
                _dispatcher.run();
            }
        });
    }
}
