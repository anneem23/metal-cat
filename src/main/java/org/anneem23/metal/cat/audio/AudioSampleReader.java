package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;

public class AudioSampleReader {

    private AudioInputStream audioInputStream;
    private AudioFormat format;

    public AudioSampleReader(InputStream inputStream)
            throws UnsupportedAudioFileException, IOException {
        audioInputStream = getAudioInputStream(inputStream);
        format = audioInputStream.getFormat();
    }

    public AudioSampleReader(File file)
            throws UnsupportedAudioFileException, IOException {
        audioInputStream = getAudioInputStream(file);
        format = audioInputStream.getFormat();
    }

    // Return audio format, and through it, most properties of
    // the audio file: sample size, sample rate, etc.
    public AudioFormat getFormat() {
        return format;
    }

    // Return the number of samples of all channels
    public long getSampleCount() {
        long total = (audioInputStream.getFrameLength() * format.getFrameSize() * 8) / format.getSampleSizeInBits();
        return total / format.getChannels();
    }


    public byte[] readBytes() throws UnsupportedAudioFileException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Read the audio data into a memory buffer.
        int nBufferSize = Shared.FRAME_SIZE * format.getFrameSize();


        byte[] abBuffer = new byte[nBufferSize];
        int nBytesRead;

        while ((nBytesRead = this.audioInputStream.read(abBuffer)) != -1) {
            baos.write(abBuffer, 0, nBytesRead);
        }

        return baos.toByteArray();
    }

    public double[] getSamples(byte[] abAudioData) {
        ShortBuffer buf=ByteBuffer.wrap(abAudioData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        int numOfSamples = buf.remaining();
        double[] amplitudes = new double[numOfSamples];
        for (int i = 0; i < numOfSamples; i++) {
            amplitudes[i] = buf.get() * (1.0/32768.0);
        }
        return amplitudes;
    }


    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        AudioSampleReader audioSampleReader = new AudioSampleReader(new File(args[0]));
        long numSamples = audioSampleReader.getSampleCount();
        double[] audioData = audioSampleReader.getSamples(audioSampleReader.readBytes());
        for (double x : audioData)
            System.out.println(x);
    }



    /*// Get the intervealed decoded samples for all channels, from sample
    // index begin (included) to sample index end (excluded) and copy
    // them into samples. end must not exceed getSampleCount(), and the
    // number of samples must not be so large that the associated byte
    // array cannot be allocated
    public void getInterleavedSamples(long begin, long end,
                                      double[] samples) throws IOException,
            IllegalArgumentException {
        long nbSamples = end - begin;
        // nbBytes = nbSamples * sampleSizeinByte * nbChannels
        long nbBytes = nbSamples * (format.getSampleSizeInBits() / 8) *
                format.getChannels();
        if (nbBytes > Integer.MAX_VALUE)
            throw new IllegalArgumentException("too many samples");
        // allocate a byte buffer
        byte[] inBuffer = new byte[(int)nbBytes];
        // read bytes from audio file
        audioInputStream.read(inBuffer, 0, inBuffer.length);
        // decode bytes into samples. Supported encodings are:
        // PCM-SIGNED, PCM-UNSIGNED, A-LAW, U-LAW
        decodeBytes(inBuffer, samples);
    }

    // Extract samples of a particular channel from interleavedSamples and
    // copy them into channelSamples
    public void getChannelSamples(int channel,
                                  double[] interleavedSamples, double[] channelSamples) {
        int nbChannels = format.getChannels();
        for (int i = 0; i < channelSamples.length; i++) {
            channelSamples[i] = interleavedSamples[nbChannels*i + channel];
        }
    }

    // Convenience method. Extract left and right channels for common stereo
    // files. leftSamples and rightSamples must be of size getSampleCount()
    public void getStereoSamples(double[] leftSamples, double[] rightSamples)
            throws IOException {
        long sampleCount = getSampleCount();
        double[] interleavedSamples = new double[(int)sampleCount*2];
        getInterleavedSamples(0, sampleCount, interleavedSamples);
        for (int i = 0; i < leftSamples.length; i++) {
            leftSamples[i] = interleavedSamples[2*i];
            rightSamples[i] = interleavedSamples[2*i+1];
        }
    }

    // Private. Decode bytes of audioBytes into audioSamples
    private void decodeBytes(byte[] audioBytes, double[] audioSamples) {
        int sampleSizeInBytes = format.getSampleSizeInBits() / 8;
        int[] sampleBytes = new int[sampleSizeInBytes];
        int k = 0; // index in audioBytes
        for (int i = 0; i < audioSamples.length; i++) {
            // collect sample byte in big-endian order
            if (format.isBigEndian()) {
                // bytes start with MSB
                for (int j = 0; j < sampleSizeInBytes; j++) {
                    sampleBytes[j] = audioBytes[k++];
                }
            } else {
                // bytes start with LSB
                for (int j = sampleSizeInBytes - 1; j >= 0; j--) {
                    sampleBytes[j] = audioBytes[k++];
                    if (sampleBytes[j] != 0)
                        j = j + 0;
                }
            }
            // get integer value from bytes
            int ival = 0;
            for (int j = 0; j < sampleSizeInBytes; j++) {
                ival += sampleBytes[j];
                if (j < sampleSizeInBytes - 1) ival <<= 8;
            }
            // decode value
            double ratio = Math.pow(2., format.getSampleSizeInBits() - 1);
            double val = ((double) ival) / ratio;
            audioSamples[i] = val;
        }
    }*/
}

