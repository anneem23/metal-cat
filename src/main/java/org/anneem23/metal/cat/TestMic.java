package org.anneem23.metal.cat;

import org.anneem23.metal.cat.body.Ear;
import org.anneem23.metal.cat.input.Shared;

import javax.sound.sampled.*;

import java.util.Scanner;
import java.util.Vector;

import static javax.sound.sampled.AudioSystem.getLine;
import static javax.sound.sampled.AudioSystem.getMixer;

public class TestMic {

    public static void main(String[] args) throws LineUnavailableException {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info: mixerInfos){
            Mixer m = getMixer(info);
            Line.Info[] lineInfos = m.getSourceLineInfo();
            for (Line.Info lineInfo:lineInfos){
                System.out.println (info.getName()+"---"+lineInfo);
                Line line = m.getLine(lineInfo);
                System.out.println("\t-----"+line);
            }
            lineInfos = m.getTargetLineInfo();
            for (Line.Info lineInfo:lineInfos){
                System.out.println (m+"---"+lineInfo);
                Line line = m.getLine(lineInfo);
                System.out.println("\t-----"+line);

            }

        }

        final Scanner scan=new Scanner(System.in);
        System.out.println("Select a microphone from the list below: ");

        final Vector<Mixer.Info> infoVector = Shared.getMixerInfo(false, true);
        for (int i = 0; i < infoVector.size(); i++) {
            Mixer.Info info = infoVector.get(i);
            System.out.println(i + ": " + info.getName());
        }
        System.out.print("");

        try (Mixer mixer = getMixer(infoVector.get(scan.nextInt()))) {

            AudioFormat format = new AudioFormat(44100, 16, 2, true, true);

            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);

            try {
                TargetDataLine targetLine = (TargetDataLine) mixer.getLine(targetInfo);
                targetLine.open(format);
                targetLine.start();

                SourceDataLine sourceLine = (SourceDataLine) getLine(sourceInfo);
                sourceLine.open(format);
                sourceLine.start();

                int numBytesRead;
                byte[] targetData = new byte[targetLine.getBufferSize() / 5];

                while (true) {
                    numBytesRead = targetLine.read(targetData, 0, targetData.length);

                    if (numBytesRead == -1)	break;

                    sourceLine.write(targetData, 0, numBytesRead);
                }
            }
            catch (Exception e) {
                System.err.println(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}