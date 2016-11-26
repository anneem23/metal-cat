package org.anneem23.metal.cat;


import org.anneem23.btrack.audio.AudioDispatcher;
import org.anneem23.btrack.audio.TargetDataLineProcessor;
import org.anneem23.metal.cat.body.Arm;
import org.anneem23.metal.cat.body.Brain;
import org.anneem23.metal.cat.body.Ear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * MetalCat.
 * <p>
 * <p>Sleeps until it hears music with more than 80 BPM.
 * <p>Then wakes up and moves its arm to the beat.
 *
 * @author anneem23
 */
class MetalCat {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetalCat.class);

    private final Arm arm;
    private final Brain brain;
    private final Ear ear;


    public MetalCat(TargetDataLine line) throws IOException {
        LOGGER.info("Creating metal cat:");
        this.arm = new Arm();
        LOGGER.info("\tArm added!");
        this.ear = new Ear(new AudioDispatcher(new TargetDataLineProcessor(line)));
        LOGGER.info("\tEars added!");
        this.brain = new Brain();
        this.ear.getDispatcher().register(this.brain);
        LOGGER.info("\tBrain added!");
        this.brain.addMetalListener(arm);
    }

    public int bpm() {
        return brain.currentTempo();
    }


    public MetalCat(String fileName) throws IOException, UnsupportedAudioFileException {
        LOGGER.info("Creating metal cat:");
        this.arm = null;
        LOGGER.info("\tArm added!");
        this.ear = new Ear(fileName);
        LOGGER.info("\tEar added!");
        this.brain = new Brain();
        this.ear.getDispatcher().register(this.brain);
        LOGGER.info("\tBrain added!");
    }

    public void waitForMusic() throws IOException, UnsupportedAudioFileException {
        sleep();
    }

    private void sleep() throws IOException, UnsupportedAudioFileException {
        this.ear.listen();
    }

}
