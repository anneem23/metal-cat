package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;

public class AudioSampleConverter {

    private final AudioFormat _audioFormat;

    public AudioSampleConverter(AudioFormat audioFormat) {
        _audioFormat = audioFormat;
    }

    // Return the number of samples of all channels
    public long getSampleCount(long frameLength) {
        long total = (frameLength * _audioFormat.getFrameSize() * 8) / _audioFormat.getSampleSizeInBits();
        return total / _audioFormat.getChannels();
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

