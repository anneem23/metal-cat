package org.anneem23.metal.cat.btrack.onset;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.util.fft.HammingWindow;

/**
 * SpectralDifference Model Object.
 *
 * @author anneem23
 * @version 2.0
 */
class SpectralDifference extends AbstractOnsetDetectionFunction {


    public SpectralDifference(int frameSize, int hopSize) {
        super(frameSize, hopSize);
    }


    public double onsetDetection(AudioEvent audioEvent) {
        be.tarsos.dsp.util.fft.FFT _fft = new be.tarsos.dsp.util.fft.FFT(_frameSize/2, new HammingWindow());
        float[] audioBuffer = audioEvent.getFloatBuffer().clone();
        _fft.powerPhaseFFT(audioBuffer, _magnitude, _phase);

        double diff;
        double odfSample = 0;	// initialise sum to zero

        for (int i = 0;i < _frameSize/2;i++)
        {
            // calculate difference
            diff = _magnitude[i] - _prevMagnitude[i];

            // only add up positive differences
            if (diff < 0)
            {
                // add difference to sum
                diff = diff * -1;
            }
            // add difference to sum
            odfSample = odfSample+diff;

            // store magnitude spectrum bin for next detection onset sample calculation
            _prevMagnitude[i] = _magnitude[i];
        }

        return odfSample;
    }

    @Override
    public double onsetDetection(double[] audioBuffer) {
        return 0;
    }


}
