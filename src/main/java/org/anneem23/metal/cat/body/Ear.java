package org.anneem23.metal.cat.body;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import org.anneem23.metal.cat.audio.AudioInputStreamProcessor;
import org.anneem23.metal.cat.audio.AudioSampleConverter;
import org.anneem23.metal.cat.audio.Shared;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
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
    private AudioSampleConverter audioSampleConverter;
    private double[] _audioData;
    private AudioInputStreamProcessor streamProcessor;

    public Ear(TargetDataLine line) {
        _line = line;

        JVMAudioInputStream audioStream = new JVMAudioInputStream(new AudioInputStream(_line));
        // create a new dispatcher
        _dispatcher = new AudioDispatcher(audioStream, Shared.FRAME_SIZE, Shared.HOPSIZE);
        executorService = Executors.newFixedThreadPool(10);
    }

    public Ear(String filename) throws IOException, UnsupportedAudioFileException {
        executorService = Executors.newFixedThreadPool(10);
        _dispatcher = null;
        _line = null;
        File audioFile = new File(filename);
        streamProcessor = new AudioInputStreamProcessor(audioFile);
        audioSampleConverter = new AudioSampleConverter(audioFormat);
    }


    public AudioDispatcher getDispatcher() {
        return _dispatcher;
    }

    /*public void listen() throws LineUnavailableException {
        ////System.out.println("Metal cat starts to listen for music!");
        if (_line != null) {
            _line.open(AUDIO_FORMAT, Shared.FRAME_SIZE);
            _line.start();
        }

        executorService.execute(new Runnable() {
            public void run() {
                _dispatcher.run();
            }
        });
    }*/

    public void listen() throws IOException, UnsupportedAudioFileException {
        _audioData = audioSampleConverter.convert(streamProcessor.readBytes());
    }

    public double[] getResult() {
        return _audioData;
    }
}
