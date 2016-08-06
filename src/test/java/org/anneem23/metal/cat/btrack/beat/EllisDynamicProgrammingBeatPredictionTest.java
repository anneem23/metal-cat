package org.anneem23.metal.cat.btrack.beat;

import org.anneem23.metal.cat.audio.Shared;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class EllisDynamicProgrammingBeatPredictionTest {

    private EllisDynamicProgrammingBeatPrediction beatPrediction = new EllisDynamicProgrammingBeatPrediction(Shared.FRAME_SIZE);

    private double[] sample;
    private long beatPeriod;

    @Test
    public void predictNoBeatForSilence() {
        givenZeroSample();
        givenDefaultBeatPeriod();
        whenProcessingSample();
        thenNoBeatIsDetcted();
    }


    @Test
    public void predictOneBeat() {
        givenSampleWithOneBeat();
        givenDefaultBeatPeriod();
        whenProcessingSample();
        thenNoBeatIsDetcted();
    }

    private void givenSampleWithOneBeat() {
        sample = new double[512];
        for (int i = 0; i < 10; i++)
            if (i % 5 == 0)
                sample[i] = 756.8;
            else
                sample[i] = 0;
    }

    private void thenNoBeatIsDetcted() {
        assertThat(beatPrediction.beatDetected(), is(Boolean.FALSE));
    }

    private void givenDefaultBeatPeriod() {
        beatPeriod = Math.round(60 / ((((double) Shared.HOP_SIZE) / Shared.SAMPLE_RATE) * 120));
    }

    private void givenZeroSample() {
        sample = new double[512];
        for (int i = 0; i < 10; i++)
            sample[i] = 0;
    }

    private void whenProcessingSample() {
        for (int i = 0; i < 10; i++)
            beatPrediction.processSample(sample[i], beatPeriod);
    }

}