package org.anneem23.metal.cat.algorithm.function;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;

/**
 * SpectralDifference Model Object.
 *
 * @author anneem23
 * @version 2.0
 */
abstract class AbstractOnsetDetectionFunction implements OnsetDetectionFunction {

    protected final float[] _power;
    protected final float[] _phase;



    protected final int _frameSize;						/**< audio framesize */
    private final int _hopSize;						/**< audio hopsize */
    protected final FFT _fft;
    protected float[] _prevPower;

    public AbstractOnsetDetectionFunction(int frameSize, int hopSize) {
        _fft = new FFT(frameSize, new HammingWindow());
        _hopSize = hopSize; // set hopsize
        _frameSize = frameSize; // set framesize


        _power = new float[frameSize/2];
        _phase = new float[frameSize/2];
        _prevPower = new float[frameSize/2];

        initializeArraysWithZeroValues();
    }

    private void initializeArraysWithZeroValues() {
        for (int i = 0;i < _frameSize/2;i++)
        {
            _power[i] = 0.0f;
            _prevPower[i] = 0.0f;
            _phase[i] = 0.0f;
        }
    }



}
