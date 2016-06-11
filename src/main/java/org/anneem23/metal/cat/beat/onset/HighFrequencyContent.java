package org.anneem23.metal.cat.beat.onset;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.util.fft.HammingWindow;

/**
 * ComplexSpectralDifferenceHWR Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #ComplexSpectralDifferenceHWR(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class HighFrequencyContent extends AbstractOnsetDetectionFunction {


    public HighFrequencyContent(int frameSize, int hopSize) {
        super(frameSize, hopSize);
    }

    public double onsetDetection(AudioEvent audioEvent) {
        be.tarsos.dsp.util.fft.FFT _fft = new be.tarsos.dsp.util.fft.FFT(_frameSize/2, new HammingWindow());
        double sum;

        // perform the FFT
        float[] audioBuffer = audioEvent.getFloatBuffer().clone();
        // perform the FFT
        _fft.powerPhaseFFT(audioBuffer, _magnitude, _phase);

        sum = 0; // initialise sum to zero

        // compute phase values from fft output and sum deviations
        for (int i = 0; i < _frameSize / 3; i++) {

            sum = sum + (_magnitude[i] * ((double) (i + 1)));

            // store values for next calculation
            _prevMagnitude[i] = _magnitude[i];
        }

        return sum;
    }

    @Override
    public double onsetDetection(double[] audioBuffer) {
        return 0;
    }
}