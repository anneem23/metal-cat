package org.anneem23.metal.cat.audio;

import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AudioSampleConverterTest {

    private AudioSampleConverter _audioSampleConverter;
    private byte[] _audioData;
    private double[] _samples;

    @Test
    public void canGetSamplesFromWavFile() throws IOException, UnsupportedAudioFileException {
        givenTestFileWithName("/140bpm.wav");
        whenGettingSamples();
        thenExpectedSamplesAre(2702700);
    }


    private void whenGettingSamples() {
        _samples = _audioSampleConverter.convert(_audioData);
    }

    private void thenExpectedSamplesAre(int numberOfSamples) {
        assertThat(_samples.length, is(numberOfSamples));
    }

    private void thenExpectedDataIs(int numberOfBytes) {
        assertThat(_audioData.length, is(numberOfBytes));
    }


    private void givenTestFileWithName(String name) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = AudioSampleConverterTest.class.getResourceAsStream(name);
        _audioSampleConverter = new AudioSampleConverter(audioFormat);
        AudioInputStreamProcessor audioProcessor = new AudioInputStreamProcessor(inputStream);
        _audioData = audioProcessor.readBytes();
    }


}