package org.anneem23.metal.cat.audio;

import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AudioInputStreamProcessorTest {

    private AudioInputStreamProcessor _audioInputStreamProcessor;
    private byte[] _audioData;

    @Test
    public void canReadBytesFromWavFile() throws IOException, UnsupportedAudioFileException {
        givenTestFileWithName("/140bpm.wav");
        whenReadingBytes();
        thenExpectedDataIs(5405400);
    }

    @Test
    public void canReadBytesFromWavFile2() throws IOException, UnsupportedAudioFileException {
        givenTestFileWithName("/120bpm.wav");
        whenReadingBytes();
        thenExpectedDataIs(5336100);
    }

    private void thenExpectedDataIs(int numberOfBytes) {
        assertThat(_audioData.length, is(numberOfBytes));
    }

    private void whenReadingBytes() throws IOException, UnsupportedAudioFileException {
        _audioData = _audioInputStreamProcessor.readBytes();
    }

    private void givenTestFileWithName(String name) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = AudioSampleConverterTest.class.getResourceAsStream(name);
        _audioInputStreamProcessor = new AudioInputStreamProcessor(getAudioInputStream(inputStream));
    }

}