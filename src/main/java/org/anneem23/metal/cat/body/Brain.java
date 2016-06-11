package org.anneem23.metal.cat.body;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.onsets.OnsetHandler;
import org.anneem23.metal.cat.beat.BeatTracker;
import org.anneem23.metal.cat.beat.onset.ComplexSpectralDifference;
import org.anneem23.metal.cat.audio.Shared;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Brain of MetalCat.
 *
 * @author anneem23
 * @version 2.0
 */
public class Brain implements OnsetHandler, AudioProcessor {


    private final BeatTracker beatTracker;
    private CopyOnWriteArrayList<Moveable> _metalListeners = new CopyOnWriteArrayList<>();

    private boolean initialized;
    private long counter;

    public Brain() throws IOException {
        beatTracker = new BeatTracker(Shared.HOPSIZE, new ComplexSpectralDifference(Shared.FRAME_SIZE, Shared.HOPSIZE), Shared.SAMPLE_RATE);
        beatTracker.setHandler(this);
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

    public double[] trackBeats(double[] audioData) {
        double[] buffer = new double[Shared.HOPSIZE];	// buffer to hold one Shared.HOPSIZE worth of audio samples

        // get number of audio frames, given the hop size and signal length
        double numframes = (int) Math.floor(((double) audioData.length) / ((double) Shared.HOPSIZE));

        double[] beats = new double[5000];
        int beatnum = 0;

        ///////////////////////////////////////////
        //////// Begin Processing Loop ////////////

        for (int i=0;i < numframes;i++)
        {
            // add new samples to frame
            for (int n = 0; n < Shared.HOPSIZE; n++)
            {
                buffer[n] = audioData[(i*Shared.HOPSIZE)+n];
                //System.out.println (buffer[n])  ;
            }

            this.beatTracker.processAudioFrame(buffer);
            if (this.beatTracker.isBeatDueInFrame())
            {
                beats[beatnum] = this.beatTracker.getBeatTimeInSeconds(i,Shared.HOPSIZE,44100);
                System.out.println (beatnum + ". beat at " + beats[beatnum] + " secs");
                beatnum = beatnum + 1;
            }
        }


        ////////// END PROCESS ///////////////////

        double[] beats_out = new double[beatnum];          // create output array

        // copy beats into output array
        for (int i = 0;i < beatnum;i++)
        {
            beats_out[i] = beats[i];
        }



        ////////// CREATE ARRAY AND RETURN IT ///////////////////

        return beats_out;
    }
}
