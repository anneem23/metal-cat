package org.anneem23.metal.cat.body;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.onsets.OnsetHandler;
import org.anneem23.metal.cat.algorithm.BTrack;
import org.anneem23.metal.cat.input.Shared;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Brain of MetalCat.
 *
 * @author anneem23
 * @version 2.0
 */
public class Brain implements OnsetHandler, AudioProcessor {


    private final BTrack bTrack;
    private CopyOnWriteArrayList<MetalCallback> _metalListeners = new CopyOnWriteArrayList<>();

    private boolean initialized;

    public Brain(AudioDispatcher dispatcher) {
        dispatcher.addAudioProcessor(this);

        bTrack = new BTrack(Shared.BUFFER_SIZE, Shared.OVERLAP);
        bTrack.setHandler(this);
    }


    @Override
    public void handleOnset(double time, double bpm) {
        System.out.println("beat detected at time=["+time+"] with bpm=["+bpm+"]");
        if (!initialized) {
            initialized = true;
            for (MetalCallback metalCallback : _metalListeners) {
                try {
                    metalCallback.setDancing(Boolean.TRUE);
                    metalCallback.updateBpm(bpm);
                    metalCallback.dance();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {

            for (MetalCallback metalCallback : _metalListeners) {
                metalCallback.updateBpm(bpm);
            }
        }
    }


    public void addMetalListener(MetalCallback metalCat) {
        _metalListeners.add(metalCat);
    }


    public boolean process(AudioEvent audioEvent) {
        double odfSample = bTrack.getSample(audioEvent);
        bTrack.findOnsets(odfSample, audioEvent.getTimeStamp());
        //System.out.println("Incoming audio event ts=["+audioEvent.getTimeStamp()+"] extracted sample=[" + odfSample+"]");

        return true;
    }

    public void processingFinished() {
        for (MetalCallback metalCallback : _metalListeners) {
            metalCallback.setDancing(Boolean.FALSE);
        }
        initialized = false;
    }
}
