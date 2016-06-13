package org.anneem23.metal.cat.tools;

import org.anneem23.metal.cat.audio.Shared;
import org.anneem23.metal.cat.audio.TargetDataLineProcessor;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import static javax.sound.sampled.AudioSystem.getMixer;

/**
 * AudioInputPrinter Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #AudioInputPrinter(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class AudioInputPrinter {


    public static void main(String[] args) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(48000, 16, 1, true, false);

        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info: mixerInfos){
            Mixer m = getMixer(info);
            Line.Info[] lineInfos = m.getSourceLineInfo();
            for (Line.Info lineInfo:lineInfos){
                System.out.println (info.getName());
                System.out.println ("\t---"+lineInfo);
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

        final Vector<Mixer.Info> infoVector = Shared.getMixerInfo(false, true);
        for (int i = 0; i < infoVector.size(); i++) {
            Mixer.Info info = infoVector.get(i);
            System.out.println(i + ": " + info.getName());
            System.out.println(info.getDescription());
            System.out.println(info.getVendor());
        }

        System.out.print("");

        try (Mixer mixer = getMixer(infoVector.get(scan.nextInt()))) {

            try {
                if (mixer.isLineSupported(targetInfo)) {
                    TargetDataLine targetLine = (TargetDataLine) mixer.getLine(targetInfo);
                    TargetDataLineProcessor processor = new TargetDataLineProcessor(targetLine);
                    processor.openStream();


                    int numBytesRead;
                    byte[] targetData = new byte[Shared.FRAME_SIZE];

                    System.out.println("Start audio ...");
                    // start recording

                    do {
                        targetData = processor.readBytes(targetData.length);
                        System.out.println(Arrays.toString(targetData));
                    } while (processor.bytesAvailable());

                } else {
                    System.out.println("Line is not supported. Mixer needs one of ");
                    for (Line.Info li : mixer.getTargetLineInfo())
                        System.out.println(li);

                    System.out.println(mixer.getLineInfo());

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