package org.anneem23.metal.cat;

import javax.sound.sampled.*;
import java.util.Scanner;
import java.util.Vector;

import static org.anneem23.metal.cat.input.Shared.SAMPLE_RATE;
import static org.anneem23.metal.cat.input.Shared.getMixerInfo;

/**
 * <p>
 * <P>Metal Cat Demo Client
 * <p>
 *
 * @author anneem23
 * @version 2.0
 */
public class MetalCatDemo {

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    private static final DataLine.Info DATA_LINE_INFO = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);

    public static void main(String[] args) throws LineUnavailableException {
        final Scanner scan=new Scanner(System.in);
        System.out.println("Select a microphone from the list below: ");

        final MetalCat metalCat;

        final Vector<Mixer.Info> infoVector = getMixerInfo(false, true);
        for (int i = 0; i < infoVector.size(); i++) {
            Mixer.Info info = infoVector.get(i);
            System.out.println(i + ": " + info.getName());
        }
        System.out.print("");

            Mixer mixer = AudioSystem.getMixer(getMixerInfo(false, true).get(scan.nextInt()));
            TargetDataLine line = (TargetDataLine) mixer.getLine(DATA_LINE_INFO);
            System.out.println("Initializing metal cat ...");
            metalCat = new MetalCat(line);
            metalCat.waitForMusic();


    }


}
