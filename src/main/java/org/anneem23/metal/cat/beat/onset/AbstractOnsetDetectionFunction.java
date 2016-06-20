package org.anneem23.metal.cat.beat.onset;

import be.tarsos.dsp.util.fft.HammingWindow;
import org.anneem23.metal.cat.beat.fft.Complex;
import org.anneem23.metal.cat.beat.fft.FFT;

/**
 * SpectralDifference Model Object.
 *
 * @author anneem23
 * @version 2.0
 */
abstract class AbstractOnsetDetectionFunction implements OnsetDetectionFunction {

    final float[] _magnitude;
    final float[] _phase;



    final int _frameSize;					/**< audio framesize */
    private final int _hopSize;						/**< audio hopsize */
    final float[] _prevMagnitude;
    private final HammingWindow window;
    private final double[] _frame;

    AbstractOnsetDetectionFunction(int frameSize, int hopSize) {
        _hopSize = hopSize; // set hopsize
        _frameSize = frameSize; // set framesize
        window = new HammingWindow();

        _frame = new double[_frameSize];
        _magnitude = new float[frameSize];
        _phase = new float[frameSize];
        _prevMagnitude = new float[frameSize];

        initializeArraysWithZeroValues();
    }

    private void initializeArraysWithZeroValues() {
        for (int i = 0;i < _frameSize;i++)
        {
            _magnitude[i] = 0.0f;
            _prevMagnitude[i] = 0.0f;
            _phase[i] = 0.0f;
            _frame[i] = 0.0;
        }
    }

    Complex[] performFFT() {
        int fsize2 = (_frameSize/2);
        float[] buffer = new float[fsize2];
        for (int i = 0; i < fsize2; i++)
            buffer[i] = (float) _frame[i];

        window.apply(buffer);
        float[] windowf = window.generateCurve(_frameSize);

        Complex[] complexIn = new Complex[_frameSize];
        // window _frame and copy to complex array, swapping the first and second half of the signal
        for (int i = 0;i < fsize2;i++)
        {
            complexIn[i] = new Complex(_frame[i+fsize2] * windowf[i+fsize2], 0.0);
            complexIn[i+fsize2] = new Complex(_frame[i] * windowf[i], 0.0);
        }

        // perform the fft
        return FFT.fft(complexIn);
    }

    public double calculateOnsetDetectionFunctionSample(double[] buffer) {
        // shift audio samples back in _frame by hop size
        System.arraycopy(_frame, 0 + _hopSize, _frame, 0, _frameSize - _hopSize);

        // add new samples to _frame from input buffer
        int j = 0;
        for (int i = (_frameSize-_hopSize); i < _frameSize; i++)
        {
            _frame[i] = buffer[j];
            j++;
        }

        return onsetDetection(_frame);
    }

    abstract double onsetDetection(double[] frame);
}
