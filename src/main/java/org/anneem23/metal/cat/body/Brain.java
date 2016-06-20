package org.anneem23.metal.cat.body;

import org.anneem23.metal.cat.audio.AudioSampleListener;
import org.anneem23.metal.cat.audio.Shared;
import org.anneem23.metal.cat.beat.BeatTracker;
import org.anneem23.metal.cat.beat.BeatTrackingAlgorithm;
import org.anneem23.metal.cat.beat.onset.ComplexSpectralDifference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Brain of MetalCat.
 *
 * @author anneem23
 * @version 2.0
 */
public class Brain implements AudioSampleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Brain.class);

    private final BeatTrackingAlgorithm beatTrackingAlgorithm;
    private final CopyOnWriteArrayList<Moveable> metalListeners = new CopyOnWriteArrayList<>();

    private Brain(BeatTrackingAlgorithm algorithm) {
        beatTrackingAlgorithm = algorithm;
    }

    public Brain() throws IOException {
        this(new BeatTracker(Shared.HOPSIZE, new ComplexSpectralDifference(Shared.FRAME_SIZE, Shared.HOPSIZE), Shared.SAMPLE_RATE));
    }


    public void addMetalListener(Moveable metalCat) {
        metalListeners.add(metalCat);
    }

    @Override
    public void updateSamples(double[] audioData) {
        double[] buffer = new double[Shared.HOPSIZE];	// buffer to hold one Shared.HOPSIZE worth of audio samples

        // get number of audio frames, given the hop size and signal length
        double numFrames = (int) Math.floor(((double) audioData.length) / ((double) Shared.HOPSIZE));

        for (int i=0;i < numFrames;i++) {
            // add new samples to frame
            System.arraycopy(audioData, (i * Shared.HOPSIZE) + 0, buffer, 0, Shared.HOPSIZE);

            beatTrackingAlgorithm.processAudioFrame(buffer);
            if (beatTrackingAlgorithm.isBeatDueInFrame()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("beat found.");
                }
                moveAll();
            }
        }

    }

    private void moveAll() {
        for (Moveable moveable : metalListeners) {
            try {
                moveable.dance();
            } catch (InterruptedException e) {
                LOGGER.error("MetalCat can't move!", e);
            }
        }
    }
}
