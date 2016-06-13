package org.anneem23.metal.cat.audio;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AudioDispatcherTest {

    private AudioDispatcher _audioDispatcher;
    
    @Mock private AudioSampleListener _listener;

    @Test
    public void canDispatchAudioSamples() throws IOException, UnsupportedAudioFileException, InterruptedException {
        givenDispatcherInputFromFile("/120bpm.wav");
        givenMockListenerIsRegistered();
        whenExecutingDispatcher();
        thenMockListenerWasInvokedTimes(5213);
    }

    private void thenMockListenerWasInvokedTimes(int wantedNumberOfInvocations) {
        verify(_listener, times(wantedNumberOfInvocations)).updateSamples(any(double[].class));
    }

    private void whenExecutingDispatcher() throws InterruptedException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(_audioDispatcher);
        Thread.sleep(10000l);
    }

    private void givenDispatcherInputFromFile(String file) throws IOException, UnsupportedAudioFileException {
        InputStream is = AudioDispatcher.class.getResourceAsStream(file);
        AudioInputStreamProcessor processor = new AudioInputStreamProcessor(is);
        _audioDispatcher = new AudioDispatcher(processor);
    }

    private void givenMockListenerIsRegistered() {
        _audioDispatcher.register(_listener);
    }

}