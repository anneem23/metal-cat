package org.anneem23.metal.cat.btrack.onset;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.util.fft.HammingWindow;
import org.anneem23.metal.cat.btrack.fft.Complex;

import java.io.*;

public class ComplexSpectralDifference extends AbstractOnsetDetectionFunction {

    private final float[] _prevPhase;
    private final float[] _prevPhase2;

    private final File data = new File("/Users/anneem23/development/arbeit/team friday projekte/metal-cat/target/data.csv");

    public ComplexSpectralDifference(int frameSize, int hopSize) throws IOException {
        super(frameSize, hopSize);
        _prevPhase = new float[frameSize];
        _prevPhase2 = new float[frameSize];

        for (int i = 0; i < frameSize; i++) {
            _prevPhase[i]=0.0f;
            _prevPhase2[i]=0.0f;
        }
    }

    public double onsetDetection(AudioEvent audioEvent) {
        be.tarsos.dsp.util.fft.FFT _fft = new be.tarsos.dsp.util.fft.FFT(_frameSize/2, new HammingWindow());
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
        _fft.powerPhaseFFTBeatRootOnset(audioBuffer, _magnitude, _phase);

        try(FileWriter writer = new FileWriter(data, true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter out = new PrintWriter(bw))
        {
            for (int i = 0; i < _frameSize/2; i++) {
                    out.println(_magnitude[i] + ", " + _phase[i]);
                    //more code

            }
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        sum = 0; // initialise sum to zero

        // compute phase values from fft output and sum deviations
        for (int i = 0;i < _frameSize/2;i++)
        {

            // phase deviation
            phaseDeviation = _phase[i] - (2*_prevPhase[i]) + _prevPhase2[i];

            // calculate complex spectral difference for the current spectral bin
            //csd = Math.sqrt(Math.pow(_magnitude[i], 2) + Math.pow(_prevMagnitude[i], 2) - 2 * _magnitude[i] * _prevMagnitude[i] * Math.cos(phaseDeviation));
            //FIXME: csd=NaN - added math.abs but that might be wrong
            csd = Math.sqrt(Math.abs(Math.pow(_magnitude[i], 2) + Math.pow(_prevMagnitude[i], 2) - 2 * _magnitude[i] * _prevMagnitude[i] * Math.cos(phaseDeviation)));

            // add to sum
            sum = sum + csd;

            // store values for next calculation
            _prevPhase2[i] = _prevPhase[i];
            _prevPhase[i] = _phase[i];
            _prevMagnitude[i] = _magnitude[i];
        }

        return sum;
    }

    public double onsetDetection(double[] audioBuffer) {
        float phaseDeviation;
        double sum = 0;
        double csd;

        // perform the FFT
        Complex[] complexOut = performFFT();
// compute phase values from fft output and sum deviations
        for (int i = 0;i < _frameSize;i++)
        {
            // calculate phase value
            _phase[i] = (float) complexOut[i].phase();
            //System.out.println("phase: " + _phase[i]);
            // calculate magnitude value
            _magnitude[i] = (float) complexOut[i].abs();
            //System.out.println("magnitude: " + _magnitude[i]);

            // phase deviation
            phaseDeviation = _phase[i] - (2*_prevPhase[i]) + _prevPhase2[i];

            // calculate complex spectral difference for the current spectral bin
            csd = Math.sqrt(Math.pow(_magnitude[i], 2) + Math.pow(_prevMagnitude[i], 2) - 2 * _magnitude[i] * _prevMagnitude[i] * Math.cos(phaseDeviation));

            // add to sum
            sum = sum + csd;

            // store values for next calculation
            _prevPhase2[i] = _prevPhase[i];
            _prevPhase[i] = _phase[i];
            _prevMagnitude[i] = _magnitude[i];
        }

        return sum;
    }



}
