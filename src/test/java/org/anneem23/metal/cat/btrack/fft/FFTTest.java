package org.anneem23.metal.cat.btrack.fft;

import org.junit.Test;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.Assert.assertThat;

public class FFTTest {


    @Test
    public void canDoFourierTransformation() {
        Complex[] input = {new Complex(0.0, 0.0)};

        Complex[] data = FFT.fft(input);
        assertThat(data, arrayWithSize(1));
        assertThat(data, arrayContaining(input));
    }
}