package org.anneem23.metal.cat.body;

import org.anneem23.btrack.audio.AudioDispatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EarTest {

    private Ear _ear;
    @Mock private AudioDispatcher _audioDispatcherMock;

    @Test
    public void shouldUseAudioDispatcher() throws IOException, UnsupportedAudioFileException, InterruptedException {
        givenEarWithFileInput();
        whenListening();
        thenAudioDispatcherShouldRun();
    }

    private void thenAudioDispatcherShouldRun() {
        verify(_audioDispatcherMock, times(1)).run();
    }

    private void whenListening() throws IOException, UnsupportedAudioFileException, InterruptedException {
        _ear.listen();
        Thread.sleep(10L);
    }

    private void givenEarWithFileInput() throws IOException, UnsupportedAudioFileException {
        _ear = new Ear(_audioDispatcherMock);
    }

}