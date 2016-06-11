package org.anneem23.metal.cat.beat.onset;

import be.tarsos.dsp.AudioEvent;

public class SpectralDifferenceHWR extends AbstractOnsetDetectionFunction {


    public SpectralDifferenceHWR(int frameSize, int hopSize) {
        super(frameSize, hopSize);
    }

    public double onsetDetection(AudioEvent audioEvent) {
        double diff;
        double sum = 0;	// initialise sum to zero



        for (int i = 0;i < _frameSize/2;i++) {
            // calculate difference
            diff = _magnitude[i] - _prevMagnitude[i];

            // only add up positive differences
            if (diff > 0)
            {
                // add difference to sum
                sum = sum+diff;
            }

            // store magnitude spectrum bin for next detection onset sample calculation
            _prevMagnitude[i] = -_magnitude[i];
        }

        return sum;
    }

    @Override
    public double onsetDetection(double[] audioBuffer) {
        return 0;
    }
}
