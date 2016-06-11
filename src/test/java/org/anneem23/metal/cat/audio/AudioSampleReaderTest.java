package org.anneem23.metal.cat.audio;

import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AudioSampleReaderTest {

    private AudioSampleReader _audioSampleReader;
    private byte[] _audioData;
    private double[] _samples;

    @Test
    public void canReadBytesFromWavFile() throws IOException, UnsupportedAudioFileException {
        givenTestFileWithName("/140bpm.wav");
        whenReadingBytes();
        thenExpectedAudioFormatIs(AudioFormat.Encoding.PCM_SIGNED, 44100.0f);
        thenExpectedDataIs(5405400);
    }

    @Test
    public void canGetSamplesFromWavFile() throws IOException, UnsupportedAudioFileException {
        givenTestFileWithName("/140bpm.wav");
        givenBytesAreRead();
        whenGettingSamples();
        thenExpectedSamplesAre(2702700);
    }

    private void givenBytesAreRead() throws IOException, UnsupportedAudioFileException {
        whenReadingBytes();
    }

    private void whenGettingSamples() {
        _samples = _audioSampleReader.getSamples(_audioData);
    }

    private void thenExpectedSamplesAre(int numberOfSamples) {
        assertThat(_samples.length, is(numberOfSamples));
    }

    private void thenExpectedDataIs(int numberOfBytes) {
        assertThat(_audioData.length, is(numberOfBytes));
    }

    private void whenReadingBytes() throws IOException, UnsupportedAudioFileException {
        _audioData = _audioSampleReader.readBytes();
    }

    private void givenTestFileWithName(String name) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = AudioSampleReaderTest.class.getResourceAsStream(name);
        _audioSampleReader = new AudioSampleReader(inputStream);
    }

    private void thenExpectedAudioFormatIs(AudioFormat.Encoding encoding, float frameRate) {
        assertThat(_audioSampleReader.getFormat().getEncoding(), is(encoding));
        assertThat(_audioSampleReader.getFormat().getFrameRate(), is(frameRate));
    }

}