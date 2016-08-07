package org.anneem23.metal.cat;

import org.anneem23.metal.cat.btrack.BeatTrackerIntegrationTest;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

public class MetalCatIntegrationTest {

    private MetalCat metalCat;

    @Test
    public void shouldMeasure140Bpm() throws IOException, UnsupportedAudioFileException, InterruptedException {
        givenFileResource("140bpm.wav");
        whenListening();
        thenBpmMeasured(140);
    }

    @Test
    public void shouldMeasure120Bpm() throws IOException, UnsupportedAudioFileException, InterruptedException {
        givenFileResource("recording.wav");
        whenListening();
        thenBpmMeasured(120);
    }

    public void givenFileResource(String resourceName) throws IOException, UnsupportedAudioFileException {
        String fileName = BeatTrackerIntegrationTest.class.getResource("/" + resourceName).getFile();
        metalCat = new MetalCat(fileName);
    }

    private void whenListening() throws IOException, UnsupportedAudioFileException, InterruptedException {
        metalCat.waitForMusic();
        Thread.sleep(2000L);
    }

    private void thenBpmMeasured(int expectedBpm) {
        assertThat((double) metalCat.bpm(), is(closeTo(expectedBpm, 3)));
    }


}
