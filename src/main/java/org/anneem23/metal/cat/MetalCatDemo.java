package org.anneem23.metal.cat;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static org.anneem23.metal.cat.audio.Shared.SAMPLE_RATE;
import static org.anneem23.metal.cat.audio.Shared.getMixerInfo;

/**
 * Metal Cat Demo Client
 * <p>
 *
 * @author anneem23
 */
public class MetalCatDemo {

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    private static final DataLine.Info DATA_LINE_INFO = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);

    private MetalCatDemo() {}


    public static void main(String[] args) throws LineUnavailableException {
        final Scanner scan=new Scanner(System.in);
        System.out.println("Select a microphone from the list below: ");

        final MetalCat metalCat;

        final List<Mixer.Info> infoVector = getMixerInfo(false, true);
        for (int i = 0; i < infoVector.size(); i++) {
            Mixer.Info info = infoVector.get(i);
            System.out.println(i + ": " + info.getName());
        }
        System.out.print("");

        TargetDataLine line;
        try (Mixer mixer = AudioSystem.getMixer(getMixerInfo(false, true).get(scan.nextInt()))) {
            line = (TargetDataLine) mixer.getLine(DATA_LINE_INFO);
            metalCat = new MetalCat(line);
            metalCat.waitForMusic();
        } catch (UnsupportedAudioFileException | IOException e) {
            System.err.println(e);
        }


    }


}
