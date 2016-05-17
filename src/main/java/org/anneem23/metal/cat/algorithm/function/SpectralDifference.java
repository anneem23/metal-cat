package org.anneem23.metal.cat.algorithm.function;

import be.tarsos.dsp.AudioEvent;

/**
 * SpectralDifference Model Object.
 *
 * @author anneem23
 * @version 2.0
 */
public class SpectralDifference extends AbstractOnsetDetectionFunction {


    public SpectralDifference(int frameSize, int hopSize) {
        super(frameSize, hopSize);
    }


    public double onsetDetection(AudioEvent audioEvent) {
        float[] audioBuffer = audioEvent.getFloatBuffer().clone();
        _fft.powerPhaseFFT(audioBuffer, _power, _phase);

        double diff;
        double odfSample = 0;	// initialise sum to zero

        for (int i = 0;i < _frameSize/2;i++)
        {
            // calculate difference
            diff = _power[i] - _prevPower[i];

            // only add up positive differences
            if (diff < 0)
            {
                // add difference to sum
                diff = diff * -1;
            }
            // add difference to sum
            odfSample = odfSample+diff;

            // store magnitude spectrum bin for next detection function sample calculation
            _prevPower[i] = _power[i];
        }

        return odfSample;
    }




}
