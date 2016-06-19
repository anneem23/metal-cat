package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AudioSampleConverter {

    private final AudioFormat audioFormat;

    public AudioSampleConverter(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    // Return the number of samples of all channels
    public long getSampleCount(long frameLength) {
        long total = (frameLength * audioFormat.getFrameSize() * 8) / audioFormat.getSampleSizeInBits();
        return total / audioFormat.getChannels();
    }

    public double[] convert(byte[] abAudioData) {
        ShortBuffer buf=ByteBuffer.wrap(abAudioData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        int numOfSamples = buf.remaining();
        double[] amplitudes = new double[numOfSamples];
        for (int i = 0; i < numOfSamples; i++) {
            amplitudes[i] = buf.get() * (1.0/32768.0);
        }
        return amplitudes;
    }


}

