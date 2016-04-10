package org.anneem23.metal.cat.input;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;

import javax.sound.sampled.*;
import javax.sound.sampled.Mixer.Info;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioInput extends BeatRootOnsetEventHandler {

    private final PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);
    private final Vector<Info> _sources;
    private Mixer _mixer;
    private BufferedWriter _file;
    private Queue<Double> beats = new LinkedList<Double>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    private OnsetHandler _onsetHandler;


    public AudioInput(OnsetHandler onsetHandler) throws IOException {
        _onsetHandler = onsetHandler;
        _sources = Shared.getMixerInfo(false, true);

        File f = new File("/tmp/data.dat");

        f.createNewFile();
        _file = new BufferedWriter(new FileWriter(f));
    }

    public Vector<Info> getInfo() {
        return _sources;
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        _propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        _propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    public void setAudioInput(Mixer mixer) {
        _propertyChangeSupport.firePropertyChange("mixer", _mixer, mixer);
        _mixer = mixer;
    }

    @Override
    public void handleOnset(double time, double salience) {
        super.handleOnset(time, salience);


        trackBeats(_onsetHandler);
    }

    public static void main(String[] args) {
        final Scanner scan=new Scanner(System.in);

        final float sampleRate = 44100;
        final int bufferSize = 1024 * 4;
        final int overlap = 768 * 4 ;


        final AudioInput input;
        try {
            input = new AudioInput(new OnsetHandler() {
                public void handleOnset(double v, double v1) {

                }
            });
        input.addPropertyChangeListener("mixer", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent arg0) {
                final AudioDispatcher dispatcher;
                Mixer mixer = (Mixer) arg0.getNewValue();
                try {
                    final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
                        false);
                    final DataLine.Info dataLineInfo = new DataLine.Info(
                        TargetDataLine.class, format);
                    TargetDataLine line;
                    line = (TargetDataLine) mixer.getLine(dataLineInfo);
                    final int numberOfSamples = bufferSize;
                    line.open(format, numberOfSamples);
                    line.start();
                    final AudioInputStream stream = new AudioInputStream(line);

                    JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
                    // create a new dispatcher
                    //int size = 512;
                    //int overlap = 256;
                    dispatcher = new AudioDispatcher(audioStream, bufferSize, overlap);
                    //dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(bufferSize, overlap);

                    ComplexOnsetDetector onsetDetector = new ComplexOnsetDetector(bufferSize);
                    BeatRootOnsetEventHandler handler = new BeatRootOnsetEventHandler();
                    onsetDetector.setHandler(input);
                    // add a processor, handle percussion event.
                    dispatcher.addAudioProcessor(onsetDetector);

                    executor.execute(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(60000);
                                dispatcher.stop();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    dispatcher.run();

//                    handler.trackBeats(input);


                } catch (LineUnavailableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });


        System.out.println("Select a microphone from the list below: ");
        final Vector<Info> infoVector = input.getInfo();
        for (int i = 0; i < infoVector.size(); i++) {
            Info info = infoVector.get(i);
            System.out.println(i + ": " + info.getName());
        }
        System.out.print("");
        input.setAudioInput(AudioSystem.getMixer(infoVector.get(scan.nextInt())));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
