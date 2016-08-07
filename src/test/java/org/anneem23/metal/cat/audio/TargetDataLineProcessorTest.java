package org.anneem23.metal.cat.audio;

import org.junit.Ignore;
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
        Mixer.Info info = infos.get(0);

        _line = (TargetDataLine) getMixer(info).getLine(getMixer(info).getTargetLineInfo()[0]);
        _line.start();
        _line.open(_line.getFormat());
        _targetDataLineProcessor = new TargetDataLineProcessor(_line);
    }
}