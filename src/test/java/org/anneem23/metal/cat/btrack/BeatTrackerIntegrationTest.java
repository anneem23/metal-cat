package org.anneem23.metal.cat.btrack;

import org.anneem23.metal.cat.audio.AudioInputStreamProcessor;
import org.anneem23.metal.cat.audio.AudioSampleConverter;
import org.anneem23.metal.cat.audio.Shared;
import org.anneem23.metal.cat.btrack.onset.ComplexSpectralDifference;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class BeatTrackerIntegrationTest {

    private BeatTracker _beatTracker;
    private int _beatnum;



    @Test
    public void test120BeatsTrackedFor120BpmTrack() throws IOException, UnsupportedAudioFileException {
        givenBTrackWithComplexSpectralDifference();
        whenTrackingBeatsOf("120bpm.wav");
        thenNumberOfBeatsDetectedIs(120);
    }

    @Test
    public void test140BeatsTrackedFor140BpmTrack() throws IOException, UnsupportedAudioFileException {
        givenBTrackWithComplexSpectralDifference();
        whenTrackingBeatsOf("140bpm.wav");
        thenNumberOfBeatsDetectedIs(142);
    }

    private void givenBTrackWithComplexSpectralDifference() throws IOException {
        ComplexSpectralDifference csd = new ComplexSpectralDifference(Shared.FRAME_SIZE, Shared.HOPSIZE);
        _beatTracker = new BeatTracker(Shared.HOPSIZE, csd, Shared.SAMPLE_RATE);
    }

    private void thenNumberOfBeatsDetectedIs(int beats) {
        assertThat(_beatnum, is(beats));
    }

    private void whenTrackingBeatsOf(String fileName) throws IOException, UnsupportedAudioFileException {
        double[] buffer = new double[Shared.HOPSIZE];	// buffer to hold one Shared.HOPSIZE worth of audio samples

        double[] audioData = getData(fileName);
        // get number of audio frames, given the hop size and signal length
        double numframes = (int) Math.floor(((double) audioData.length) / ((double) Shared.HOPSIZE));

        double[] beats = new double[5000];
        int beatnum = 0;

        ///////////////////////////////////////////
        //////// Begin Processing Loop ////////////

        for (int i=0;i < numframes;i++) {
            // add new samples to frame
            System.arraycopy(audioData, (i * Shared.HOPSIZE), buffer, 0, Shared.HOPSIZE);

            _beatTracker.processAudioFrame(buffer);
            if (_beatTracker.isBeatDueInFrame()) {
                beats[beatnum] = _beatTracker.getBeatTimeInSeconds(i,Shared.HOPSIZE,44100);
                System.out.println (beatnum + ". beat at " + beats[beatnum] + " secs");
                beatnum = beatnum + 1;
            }
        }
        _beatnum = beatnum;
    }


    private double[] getData(String resourceName) throws IOException, UnsupportedAudioFileException {
        InputStream inputStream = BeatTrackerIntegrationTest.class.getResourceAsStream("/" + resourceName);
        AudioInputStreamProcessor ais = new AudioInputStreamProcessor(inputStream);
        AudioSampleConverter audioSampleConverter = new AudioSampleConverter(ais.getFormat());
        return audioSampleConverter.convert(ais.readBytes());
    }

}