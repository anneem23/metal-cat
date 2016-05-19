package org.anneem23.metal.cat;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import org.anneem23.metal.cat.algorithm.BTrack;
import org.anneem23.metal.cat.input.Shared;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TestBTrack Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #TestBTrack(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class TestBTrack implements OnsetHandler {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final AudioDispatcher _dispatcher;
    private final BTrack _onsetHandler;
    private static TargetDataLine _line;

    private TestBTrack(String pathname) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        _dispatcher = AudioDispatcherFactory.fromFile(new File(pathname), 1024, 512);
        _onsetHandler = new BTrack(_dispatcher, 1024, 512);

        _onsetHandler.setHandler(this);

    }


    private TestBTrack(TargetDataLine line) throws LineUnavailableException {
        JVMAudioInputStream audioStream = new JVMAudioInputStream(new AudioInputStream(line));
        // create a new dispatcher
        _dispatcher = new AudioDispatcher(audioStream, 1024, 512);
        _onsetHandler = new BTrack(_dispatcher, 1024, 512);

        _onsetHandler.setHandler(this);

    }

    public static void main(String[] args) {

        final Scanner scan=new Scanner(System.in);


        System.out.println("Select a microphone from the list below: ");

        final Vector<Mixer.Info> infoVector = Shared.getMixerInfo(false, true);
        for (int i = 0; i < infoVector.size(); i++) {
            Mixer.Info info = infoVector.get(i);
            System.out.println(i + ": " + info.getName());
        }
        System.out.print("");

        TestBTrack bTrack;
        try (Mixer mixer = AudioSystem.getMixer(infoVector.get(scan.nextInt()))) {

            _line = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, new AudioFormat(44100, 16, 1, true, false)));
            bTrack = new TestBTrack(_line);
            bTrack.run();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void run() throws LineUnavailableException {
        _line.open(new AudioFormat(44100, 16, 1, true, false), 1024);
        _line.start();

        executorService.execute(new Runnable() {
            public void run() {
                _dispatcher.run();
            }
        });
    }

    public void handleOnset(double time, double salience) {
        System.out.println(time+": beat");
    }
}
