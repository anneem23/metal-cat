package org.anneem23.metal.cat.algorithm.onset;

import be.tarsos.dsp.AudioEvent;

public class ComplexSpectralDifference extends AbstractOnsetDetectionFunction {

    private float[] _prevPhase;
    private float[] _prevPhase2;

    public ComplexSpectralDifference(int frameSize, int hopSize) {
        super(frameSize, hopSize);
        _prevPhase = new float[frameSize/2];
        _prevPhase2 = new float[frameSize/2];

        for (int i = 0; i < frameSize/2; i++) {
            _prevPhase[i]=0.0f;
            _prevPhase2[i]=0.0f;
        }
    }

    public double onsetDetection(AudioEvent audioEvent) {
        double phaseDeviation;
        double sum;
        double csd;

        float[] audioBuffer = audioEvent.getFloatBuffer().clone();
        /*int fsize2 = (_frameSize/2);

        // window frame and copy to complex array, swapping the first and second half of the signal
        for (int i = 0;i < fsize2;i++)
        {
            audioBuffer[i] = audioBuffer[i+fsize2] * window[i+fsize2];
            audioBuffer[i+fsize2] = audioBuffer[i] * window[i];
        }*/
        // perform the FFT
        _fft.powerPhaseFFT(audioBuffer, _power, _phase);

        sum = 0; // initialise sum to zero

        // compute phase values from fft output and sum deviations
        for (int i = 0;i < _frameSize/2;i++)
        {

            // phase deviation
            phaseDeviation = _phase[i] - (2*_prevPhase[i]) + _prevPhase2[i];

            // calculate complex spectral difference for the current spectral bin
            //csd = Math.sqrt(Math.pow(_power[i], 2) + Math.pow(_prevPower[i], 2) - 2 * _power[i] * _prevPower[i] * Math.cos(phaseDeviation));
            //FIXME: csd=NaN - added math.abs but that might be wrong
            csd = Math.sqrt(Math.abs(Math.pow(_power[i], 2) + Math.pow(_prevPower[i], 2) - 2 * _power[i] * _prevPower[i] * Math.cos(phaseDeviation)));

            // add to sum
            sum = sum + csd;

            // store values for next calculation
            _prevPhase2[i] = _prevPhase[i];
            _prevPhase[i] = _phase[i];
            _prevPower[i] = _power[i];
        }

        return sum;
    }
}
