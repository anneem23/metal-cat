package org.anneem23.metal.cat.body;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.onsets.OnsetHandler;
import org.anneem23.metal.cat.audio.AudioSampleListener;
import org.anneem23.metal.cat.audio.Shared;
import org.anneem23.metal.cat.beat.BeatTrackingAlgorithm;
import org.anneem23.metal.cat.beat.BeatTracker;
import org.anneem23.metal.cat.beat.onset.ComplexSpectralDifference;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Brain of MetalCat.
 *
 * @author anneem23
 * @version 2.0
 */
public class Brain implements AudioSampleListener, AudioProcessor, OnsetHandler {


    private final BeatTrackingAlgorithm _beatTracker;
    private CopyOnWriteArrayList<Moveable> _metalListeners = new CopyOnWriteArrayList<>();

    private boolean initialized;
    private long counter;

    public Brain(BeatTrackingAlgorithm algorithm) {
        _beatTracker = algorithm;
    }

    public Brain() throws IOException {
        _beatTracker = new BeatTracker(Shared.HOPSIZE, new ComplexSpectralDifference(Shared.FRAME_SIZE, Shared.HOPSIZE), Shared.SAMPLE_RATE);
        ((BeatTracker) _beatTracker).setHandler(this);
    }


    @Override
    public void handleOnset(double time, double bpm) {
        ////System.out.println("beat detected at time=["+time+"] with bpm=["+bpm+"]");
        if (!initialized) {
            initialized = true;
            for (Moveable moveable : _metalListeners) {
                try {
                    moveable.setDancing(Boolean.TRUE);
                    moveable.updateBpm(bpm);
                    moveable.dance();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {

            for (Moveable moveable : _metalListeners) {
                moveable.updateBpm(bpm);
            }
        }
    }


    public void addMetalListener(Moveable metalCat) {
        _metalListeners.add(metalCat);
    }


    public boolean trackBeats(AudioEvent audioEvent) {
        /*double odfSample = bTrack.getSample(audioEvent);
        bTrack.findOnsets(odfSample, ++counter);
        ////System.out.println("Incoming audio event ts=["+audioEvent.getTimeStamp()+"] extracted sample=[" + odfSample+"]");
*/
        return true;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        return trackBeats(audioEvent);
    }

    public void processingFinished() {
        for (Moveable moveable : _metalListeners) {
            moveable.setDancing(Boolean.FALSE);
        }
        initialized = false;
    }

    @Override
    public void updateSamples(double[] audioData) {
        double[] buffer = new double[Shared.HOPSIZE];	// buffer to hold one Shared.HOPSIZE worth of audio samples

        // get number of audio frames, given the hop size and signal length
        double numFrames = (int) Math.floor(((double) audioData.length) / ((double) Shared.HOPSIZE));

        for (int i=0;i < numFrames;i++) {
            // add new samples to frame
            for (int n = 0; n < Shared.HOPSIZE; n++) {
                buffer[n] = audioData[(i*Shared.HOPSIZE)+n];
            }

            _beatTracker.processAudioFrame(buffer);
            if (_beatTracker.isBeatDueInFrame()) {
                System.out.println("beat found.");
                moveAll();
            }
        }

    }

    private void moveAll() {
        for (Moveable moveable : _metalListeners) {
            try {
                moveable.dance();
            } catch (InterruptedException e) {
                e.printStackTrace();
                //TODO log error/warning
            }
        }
    }
}
