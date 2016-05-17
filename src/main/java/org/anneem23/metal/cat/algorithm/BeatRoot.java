package org.anneem23.metal.cat.algorithm;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.OnsetHandler;
import org.anneem23.metal.cat.input.Shared;
import org.anneem23.metal.cat.mind.Brain;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BeatRoot Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #BeatRoot(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class BeatRoot implements BeatMatchingAlgorithm, OnsetHandler {


    private static final float sampleRate = 44100;
    /*private static final int bufferSize = 1024;
    private static final int overlap = 512;*/

    private static final int bufferSize = 1024;
    private static final int overlap = 512;

    private final BTrack _onsetDetector;
    private final TargetDataLine _line;
    private final AudioFormat _format;
    private final Queue<Double> _beats = new LinkedList<Double>();
    private int _bpm;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final AudioDispatcher _dispatcher;

    private final Brain _brain;

    public BeatRoot(int idx, Brain brain) throws LineUnavailableException, IOException {
        _format = new AudioFormat(sampleRate, 16, 1, true, false);

        //AudioInput audioInput = new AudioInput(this);
        Vector<Mixer.Info> mixerInfo = Shared.getMixerInfo(false, true);
        _line = (TargetDataLine) AudioSystem.getMixer(mixerInfo.get(idx)).getLine(new DataLine.Info(TargetDataLine.class, _format));

        JVMAudioInputStream audioStream = new JVMAudioInputStream(new AudioInputStream(_line));
        // create a new dispatcher
        _dispatcher = new AudioDispatcher(audioStream, bufferSize, overlap);
        _onsetDetector = new BTrack(_dispatcher, bufferSize, overlap);
        _onsetDetector.setHandler(this);
        _brain = brain;

        run();
    }

    public void run() throws LineUnavailableException {
        _line.open(_format, bufferSize);
        _line.start();
/*        JVMAudioInputStream audioStream = new JVMAudioInputStream(new AudioInputStream(_line));
        // create a new dispatcher
        final AudioDispatcher dispatcher = new AudioDispatcher(audioStream, bufferSize, overlap);*/

        executorService.execute(new Runnable() {
            public void run() {
                _dispatcher.run();
            }
        });
    }

    public Double matchBeat(URI url) throws IOException {
        return (double) _bpm;
    }

    public void handleOnset(double v, double v1) {
        System.out.println("handleOnset(double v=["+v+"], double v1=["+v1+"])");
        try {
            _brain.dance(_bpm, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
