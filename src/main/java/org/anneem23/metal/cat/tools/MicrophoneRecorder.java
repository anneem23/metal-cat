package org.anneem23.metal.cat.tools;

import org.anneem23.metal.cat.audio.Shared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static javax.sound.sampled.AudioSystem.getMixer;

class MicrophoneRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrophoneRecorder.class);
    // path of the wav file
    private static final File wavFile = new File("/tmp/recording.wav");

    // format of audio file

    private static final AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    public static void main(String[] args) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(48000, 16, 1, true, false);

        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info: mixerInfos){
            Mixer m = getMixer(info);
            Line.Info[] lineInfos = m.getSourceLineInfo();
            for (Line.Info lineInfo:lineInfos){
                //System.out.println (info.getName());
                //System.out.println ("\t---"+lineInfo);
                Line line = m.getLine(lineInfo);
                System.out.println("\t-----"+line);
                System.out.println("\t-----"+line.getLineInfo());
                System.out.println("\t-------"+ Arrays.toString(line.getControls()));
            }
            lineInfos = m.getTargetLineInfo();
            for (Line.Info lineInfo:lineInfos){
                System.out.println (info.getName());
                System.out.println ("\t---"+lineInfo);
                Line line = m.getLine(lineInfo);
                System.out.println("\t-----"+line);
                System.out.println("\t-----"+line.getLineInfo());
                System.out.println("\t-----"+ Arrays.toString(line.getControls()));

            }

        }

        final Scanner scan=new Scanner(System.in);
        System.out.println("Select a microphone from the list below: ");

        final List<Mixer.Info> infoVector = Shared.getMixerInfo(false, true);
        for (int i = 0; i < infoVector.size(); i++) {
            Mixer.Info info = infoVector.get(i);
            System.out.println(i + ": " + info.getName());
            System.out.println(info.getDescription());
            System.out.println(info.getVendor());
        }

        System.out.print("");

        try (Mixer mixer = getMixer(infoVector.get(scan.nextInt()))) {

                if (mixer.isLineSupported(targetInfo)) {
                    TargetDataLine targetLine = (TargetDataLine) mixer.getLine(targetInfo);
                    targetLine.open(format);
                    targetLine.start();

                    AudioInputStream ais = new AudioInputStream(targetLine);
                    System.out.println("Start recording...");
                    // start recording
                    AudioSystem.write(ais, fileType, wavFile);
                } else {
                    System.out.println("Line is not supported. Mixer needs one of ");
                    for (Line.Info li : mixer.getTargetLineInfo())
                        System.out.println(li);

                    System.out.println(mixer.getLineInfo());

                }


        } catch (Exception e) {
            LOGGER.error("Cannot open Mixer.", e);
        }


    }

}