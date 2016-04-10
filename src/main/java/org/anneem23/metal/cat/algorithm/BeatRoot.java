package org.anneem23.metal.cat.algorithm;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import org.anneem23.metal.cat.input.AudioInput;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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


    private static final float sampleRate = 48000;
    private static final int bufferSize = 1024 * 4;
    private static final int overlap = 768 * 4 ;

    private final ComplexOnsetDetector _onsetDetector;
    private final TargetDataLine _line;
    private final AudioFormat _format;
    private final Queue<Double> _beats = new LinkedList<Double>();
    private int _bpm;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);



    public BeatRoot(int idx) throws LineUnavailableException, IOException {
        _format = new AudioFormat(sampleRate, 16, 1, true, false);

        _onsetDetector = new ComplexOnsetDetector(bufferSize);
        AudioInput audioInput = new AudioInput(this);
        _onsetDetector.setHandler(audioInput);

        _line = (TargetDataLine) AudioSystem.getMixer(audioInput.getInfo().get(idx)).getLine(new DataLine.Info(TargetDataLine.class, _format));

        run();
    }

    public void run() throws LineUnavailableException {
        _line.open(_format, bufferSize);
        _line.start();

        JVMAudioInputStream audioStream = new JVMAudioInputStream(new AudioInputStream(_line));
        // create a new dispatcher
        final AudioDispatcher dispatcher = new AudioDispatcher(audioStream, bufferSize, overlap);
        // add a processor, handle percussion event.
        dispatcher.addAudioProcessor(_onsetDetector);

        executorService.execute(new Runnable() {
            public void run() {
                dispatcher.run();
            }
        });
    }

    public Double matchBeat(URI url) throws IOException {
        return (double) _bpm;
    }

    public void handleOnset(double v, double v1) {
        if (!_beats.contains(v)) {
            _beats.offer(v);
            //System.out.println(v);

            if (v >= 10) {
                int beatsOfLastTenSeconds = 0;

                Double[] arrBeats = _beats.toArray(new Double[_beats.size()]);

                double limit = arrBeats[arrBeats.length - 1] - 10;
                for (int i = (arrBeats.length-1); i >= 0; i--) {
                    if (arrBeats[i] < limit) {
                        break;
                    }
                    beatsOfLastTenSeconds++;
                }
                _bpm = beatsOfLastTenSeconds * 6;
            }

        }
    }
}
