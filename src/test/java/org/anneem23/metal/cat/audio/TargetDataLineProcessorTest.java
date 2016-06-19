package org.anneem23.metal.cat.audio;

import org.junit.Test;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.List;

import static javax.sound.sampled.AudioSystem.getMixer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TargetDataLineProcessorTest {

    private TargetDataLineProcessor _targetDataLineProcessor;
    private byte[] _audioData;
    private TargetDataLine _line;

    @Test
    public void testReadCanReadBytesFromMicrophone() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        givenMicrophoneInput();
        whenReadingData();
        thenExpectedDataIs(Shared.FRAME_SIZE);
    }
    private void thenExpectedDataIs(int numberOfBytes) {
        assertThat(_audioData.length, is(numberOfBytes));
    }

    private void whenReadingData() throws IOException {
        _audioData = _targetDataLineProcessor.readBytes(Shared.FRAME_SIZE);

        _line.close();
    }

    private void givenMicrophoneInput() throws LineUnavailableException {
        final List<Mixer.Info> infos = Shared.getMixerInfo(false, true);
        Mixer.Info info = infos.get(1);

        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, Shared.AUDIO_FORMAT);
        _line = (TargetDataLine) getMixer(info).getLine(targetInfo);
        _line.start();
        _line.open(Shared.AUDIO_FORMAT);
        _targetDataLineProcessor = new TargetDataLineProcessor(_line);
    }
}