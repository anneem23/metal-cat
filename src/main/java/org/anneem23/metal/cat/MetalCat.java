package org.anneem23.metal.cat;

import org.anneem23.metal.cat.audio.AudioDispatcher;
import org.anneem23.metal.cat.audio.TargetDataLineProcessor;
import org.anneem23.metal.cat.body.Arm;
import org.anneem23.metal.cat.body.Brain;
import org.anneem23.metal.cat.body.Ear;

import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * MetalCat.
 * <p>
 * <P>Sleeps until it hears music. Then wakes up and moves its arm to the beat.
 * <p>
 *
 * @author anneem23
 */
public class MetalCat {

    private final Arm arm;
    private final Brain brain;
    private final Ear ear;


    public MetalCat(TargetDataLine line) throws IOException {
        //System.out.println("Creating metal cat:");
        this.arm = new Arm();
        //System.out.println("\tArm added!");
        //Try out in case of problems: JVMAudioInputStream audioStream = new JVMAudioInputStream(new AudioInputStream(line));
        this.ear = new Ear(new AudioDispatcher(new TargetDataLineProcessor(line)));
        //System.out.println("\tEars added!");
        this.brain = new Brain();
        this.ear.getDispatcher().register(this.brain);
        //System.out.println("\tBrain added!");
        this.brain.addMetalListener(arm);
    }



    public MetalCat(String fileName) throws IOException, UnsupportedAudioFileException {
        //System.out.println("Creating metal cat:");
        this.arm = null;
        //System.out.println("\tArm added!");
        this.ear = new Ear(fileName);
        //System.out.println("\tEar added!");
        this.brain = new Brain();
        //System.out.println("\tBrain added!");
        //this.brain.addMetalListener(arm);
    }

    public void waitForMusic() throws IOException, UnsupportedAudioFileException {
        sleep();
    }

    private void sleep() throws IOException, UnsupportedAudioFileException {
        this.ear.listen();
    }

}
