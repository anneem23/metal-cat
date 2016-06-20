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
class ComplexSpectralDifferenceHWR extends AbstractOnsetDetectionFunction {

    private final float[] _prevPhase;
    private final float[] _prevPhase2;

    public ComplexSpectralDifferenceHWR(int frameSize, int hopSize) {
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
        double phaseDeviation;
        double sum;
        double magnitudeDifference;
        double csd;

        // perform the FFT
        float[] audioBuffer = audioEvent.getFloatBuffer().clone();
        // perform the FFT
        _fft.powerPhaseFFT(audioBuffer, _magnitude, _phase);

        sum = 0; // initialise sum to zero

        // compute _phase values from fft output and sum deviations
        for (int i = 0;i < _frameSize/2;i++)
        {

            // _phase deviation
            phaseDeviation = _phase[i] - (2*_prevPhase[i]) + _prevPhase2[i];

            // calculate magnitude difference (real part of Euclidean distance between complex frames)
            magnitudeDifference = _magnitude[i] - _prevMagnitude[i];

            // if we have a positive change in magnitude, then include in sum, otherwise ignore (half-wave rectification)
            if (magnitudeDifference > 0)
            {
                // calculate complex spectral difference for the current spectral bin
                csd = Math.sqrt(Math.pow(_magnitude[i], 2) + Math.pow(_prevMagnitude[i], 2) - 2 * _magnitude[i] * _prevMagnitude[i] * Math.cos(phaseDeviation));

                // add to sum
                sum = sum + csd;
            }

            // store values for next calculation
            _prevPhase2[i] = _prevPhase[i];
            _prevPhase[i] = _phase[i];
            _prevMagnitude[i] = _magnitude[i];
        }

        return sum;
    }

    @Override
    public double onsetDetection(double[] audioBuffer) {
        return 0;
    }
}
