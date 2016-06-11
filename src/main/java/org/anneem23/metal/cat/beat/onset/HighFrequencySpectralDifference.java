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
public class HighFrequencySpectralDifference extends AbstractOnsetDetectionFunction {

    private final float[] _prevPhase;
    private final float[] _prevPhase2;

    public HighFrequencySpectralDifference(int frameSize, int hopSize) {
        super(frameSize, hopSize);
        _prevPhase = new float[frameSize/2];
        _prevPhase2 = new float[frameSize/2];

        for (int i = 0; i < frameSize/2; i++) {
            _prevPhase[i]=0.0f;
            _prevPhase2[i]=0.0f;
        }
    }

    public double onsetDetection(AudioEvent audioEvent) {
        be.tarsos.dsp.util.fft.FFT _fft = new be.tarsos.dsp.util.fft.FFT(_frameSize/2, new HammingWindow());
        double sum;
        double mag_diff;

        // perform the FFT
        float[] audioBuffer = audioEvent.getFloatBuffer().clone();
        // perform the FFT
        _fft.powerPhaseFFT(audioBuffer, _magnitude, _phase);

        sum = 0; // initialise sum to zero

        // compute phase values from fft output and sum deviations
        for (int i = 0;i < _frameSize/2;i++)
        {

            // calculate difference
            mag_diff = _magnitude[i] - _prevMagnitude[i];

            if (mag_diff < 0)
            {
                mag_diff = -mag_diff;
            }

            sum = sum + (mag_diff*((double) (i+1)));

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
