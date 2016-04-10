package org.anneem23.metal.cat;

import org.anneem23.metal.cat.input.AudioInput;
import org.anneem23.metal.cat.input.Shared;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

/**
 * <p>
 * <P>Metal Cat Demo Client
 * <p>
 *
 * @author anneem23
 * @version 2.0
 */
public class MetalCatDemo {

    public static void main(String[] args) throws InterruptedException {
        final Scanner scan=new Scanner(System.in);
        System.out.println("Select a microphone from the list below: ");

        final MetalCat metalCat;
        try {

            final Vector<Mixer.Info> infoVector = Shared.getMixerInfo(false, true);
            for (int i = 0; i < infoVector.size(); i++) {
                Mixer.Info info = infoVector.get(i);
                System.out.println(i + ": " + info.getName());
            }
            System.out.print("");

            metalCat = new MetalCat(scan.nextInt());
            metalCat.dance(null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }


}
