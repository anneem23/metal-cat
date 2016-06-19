package org.anneem23.metal.cat.body;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class BrainTest {

    private Brain _brain;

    @Before
    public void init() throws IOException {
        _brain = new Brain();
    }

    @Test
    public void canMoveMoveableOnSampleUpdate() {
        whenUpdatingSamples(new double[]{});
    }

    private void whenUpdatingSamples(double[] samples) {
        _brain.updateSamples(samples);
    }

}