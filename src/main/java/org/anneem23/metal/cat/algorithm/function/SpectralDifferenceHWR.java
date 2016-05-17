package org.anneem23.metal.cat.algorithm.function;

import be.tarsos.dsp.AudioEvent;

/**
 * SpectralDifferenceHWR Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #SpectralDifferenceHWR(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class SpectralDifferenceHWR extends AbstractOnsetDetectionFunction {


    public SpectralDifferenceHWR(int frameSize, int hopSize) {
        super(frameSize, hopSize);
    }

    public double onsetDetection(AudioEvent audioEvent) {
        double diff;
        double sum = 0;	// initialise sum to zero

        float[] audioBuffer = audioEvent.getFloatBuffer().clone();
        _fft.powerPhaseFFT(audioBuffer, _power, _phase);



        for (int i = 0;i < _frameSize/2;i++)
        {
            // calculate difference
            diff = _power[i] - _prevPower[i];

            // only add up positive differences
            if (diff > 0)
            {
                // add difference to sum
                sum = sum+diff;
            }



            // store magnitude spectrum bin for next detection function sample calculation
            _prevPower[i] = -_power[i];
        }

        return sum;
    }
}
