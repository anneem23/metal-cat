package org.anneem23.metal.cat;

import org.anneem23.metal.cat.body.Arm;
import org.anneem23.metal.cat.body.Brain;
import org.anneem23.metal.cat.body.Ear;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * MetalCat.
 * <p>
 * <P>Sleeps until it hears music. Then wakes up and moves its arm to the beat.
 * <p>
 *
 * @author anneem23
 * @version 2.0
 */
public class MetalCat {

    private final Arm arm;
    private final Brain brain;
    private final Ear ear;


    public MetalCat(TargetDataLine line) {
        System.out.println("Creating metal cat:");
        this.arm = new Arm();
        System.out.println("\tArm added!");
        this.ear = new Ear(line);
        System.out.println("\tEars added!");
        this.brain = new Brain(this.ear.getDispatcher());
        System.out.println("\tBrain added!");
        this.brain.addMetalListener(arm);
    }

    public void waitForMusic() throws LineUnavailableException {
        sleep();
    }

    private void sleep() throws LineUnavailableException {
        this.ear.listen();
    }


}
