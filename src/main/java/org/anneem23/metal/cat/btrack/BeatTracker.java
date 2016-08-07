package org.anneem23.metal.cat.btrack;

import be.tarsos.dsp.resample.Resampler;
import org.anneem23.metal.cat.btrack.onset.OnsetDetectionFunction;
import org.anneem23.metal.cat.btrack.beat.EllisDynamicProgrammingBeatPrediction;
import org.anneem23.metal.cat.btrack.tempo.TempoEstimation;

import java.io.IOException;

/**
 * BeatTracker is a java implementation of Adam Starks BTrack algorithm.
 * <p>
 * It does live beat tracking with a combination of two well-known algorithms for
 * beat prediction and tempo estimation.
 * <p>
 * <p>{@link TempoEstimation} is based on the Davies and Plumbley method
 * <p>{@link EllisDynamicProgrammingBeatPrediction} is based on Ellis' dynamic programming beat
 *
 */
public class BeatTracker implements BeatTrackingAlgorithm {

    private final OnsetDetectionFunction onsetDetectionFunction;
    private final EllisDynamicProgrammingBeatPrediction beatPrediction;
    private final TempoEstimation tempoEstimation;

    private final int onsetDFBufferSize;
    private final float[] onsetDF;
    private boolean beatDueInFrame;


    public BeatTracker(int hopSize, OnsetDetectionFunction onsetDetector, float sampleRate) throws IOException {
        onsetDetectionFunction = onsetDetector;
        onsetDFBufferSize = (512*512)/hopSize;
        onsetDF = new float[onsetDFBufferSize];


        beatPrediction = new EllisDynamicProgrammingBeatPrediction(onsetDFBufferSize);
        tempoEstimation = new TempoEstimation(hopSize, sampleRate);

        initializeArrays();
    }

    @Override
    public void processAudioFrame(double[] audioBuffer) {
        double odfSample = onsetDetectionFunction.calculateOnsetDetectionFunctionSample(audioBuffer);
        processOnsetDetectionFunctionSample(odfSample);
    }

    /**
     * Our underlying model for beat tracking assumes that the sequence
     * of beats, will correspond to a set of approximately periodic
     * peaks in the onset detection function.
     *
     * To be able to track beats in music that varies in speed we need
     * to regularly update the tempo estimate used by the beat tracking
     * stage.
     *
     * In line with the beat prediction methodology, the tempo is
     * re-estimated once each new predicted beat has elapsed.
     *
     * @param onsetDetectionFunctionSample the odf sample
     */
    private void processOnsetDetectionFunctionSample(double onsetDetectionFunctionSample) {
        // to ensure that the onset detection onset sample is positive
        // add a tiny constant to the sample to stop it from ever going
        // to zero. this is to avoid problems further down the line
        double odfSample = Math.abs(onsetDetectionFunctionSample) + 0.0001;

        // move all samples back one step
        System.arraycopy(onsetDF, 1, onsetDF, 0, onsetDFBufferSize - 1);

        // add new sample at the end
        onsetDF[onsetDFBufferSize -1] = (float) odfSample;

        beatDueInFrame = false;
        // beat prediction
        beatPrediction.processSample(odfSample, tempoEstimation.beatPeriod());

        // if we are at a beat
        if (beatPrediction.beatDetected()) {
            // indicate a beat should be output
            beatDueInFrame = true;
            // resample odf samples
            double[] resampledData = resampleOnsetDetectionFunction();

            // To be able to track beats in music that varies in speed we need
            // to regularly update the tempo estimate used by the beat tracking
            // stage.
            // In line with the beat prediction methodology, the tempo is
            // re-estimated once each new predicted beat has elapsed.
            tempoEstimation.calculateTempo(resampledData);
        }
    }


    private double[] resampleOnsetDetectionFunction() {
        float[] output = new float[512];
        float[] input = new float[onsetDFBufferSize];
        double[] resampledOnsetDetectionFunctionData = new double[512];

        System.arraycopy(onsetDF, 0, input, 0, onsetDFBufferSize);

        Resampler resampler = new Resampler(true, 1, 200);
        resampler.process(1,input, 0, onsetDFBufferSize, true, output,  0, 512);

        for ( int i = 0; i < output.length; i++) {
            resampledOnsetDetectionFunctionData[i] = output[i];
        }

        return resampledOnsetDetectionFunctionData;
    }

    @Override
    public boolean isBeatDueInFrame() {
        return beatDueInFrame;
    }

    @Override
    public int getTempo() {
        return (int) tempoEstimation.tempo();
    }

    public double getBeatTimeInSeconds(long frameNumber,int hopSize, int samplingFrequency) {
        return ((double) hopSize / (double) samplingFrequency) * (double) frameNumber;
    }

    private void initializeArrays() {
        // initialise df_buffer to zeros
        for (int i = 0; i < onsetDFBufferSize; i++) {
            onsetDF[i] = 0;
            if ((i % Math.round(tempoEstimation.beatPeriod())) == 0) {
                onsetDF[i] = 1;
            }
        }
    }


}