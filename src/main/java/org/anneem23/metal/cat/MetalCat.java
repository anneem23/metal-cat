package org.anneem23.metal.cat;

import org.anneem23.metal.cat.algorithm.BeatRoot;
import org.anneem23.metal.cat.body.Arm;
import org.anneem23.metal.cat.body.MetalCallback;
import org.anneem23.metal.cat.mind.Brain;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.URI;

/**
 * MetalCat.
 * <p>
 * <P>Sleeps until it hears music. Then wakes up and moves its arm to the beat.
 * <p>
 *
 * @author anneem23
 * @version 2.0
 */
public class MetalCat implements MetalCallback {

    private final Arm arm;
    private final Brain brain;
    //private final Ear ear;

    public MetalCat(int idx) throws IOException, LineUnavailableException {
        this.arm = new Arm();
        this.brain = new Brain(idx, this.arm);
        //this.ear = new Ear(this);
    }

    public void waitForMusic() {
        sleep();
    }

    private void sleep() {
        //this.ear.listen();
    }

    public void dance(URI url) throws InterruptedException {
        System.out.println("Metal Cat starts to dance!");
        Double bpm = this.brain.compute(url);
        if (bpm >= 0)
            this.brain.dance(bpm, 120000);
    }


}
