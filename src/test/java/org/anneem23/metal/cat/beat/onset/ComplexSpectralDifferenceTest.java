package org.anneem23.metal.cat.beat.onset;

import org.anneem23.metal.cat.audio.AudioSampleReader;
import org.anneem23.metal.cat.audio.Shared;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * ComplexSpectralDifferenceTest Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #ComplexSpectralDifferenceTest(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class ComplexSpectralDifferenceTest {

    private ComplexSpectralDifference complexSpectralDifference;




    @Test
    public void testOnsetDetection120Bpm() throws IOException, UnsupportedAudioFileException {
        complexSpectralDifference = new ComplexSpectralDifference(Shared.FRAME_SIZE, Shared.HOPSIZE);
        double sample = complexSpectralDifference.onsetDetection(getData("120bpm.wav"));
        assertThat(sample, is(0.0));
    }

    private double[] getData(String resourceName) throws IOException, UnsupportedAudioFileException {
        InputStream inputStream = ComplexSpectralDifferenceTest.class.getResourceAsStream("/" + resourceName);
        AudioSampleReader audioSampleReader = new AudioSampleReader(inputStream);
        return audioSampleReader.getSamples(audioSampleReader.readBytes());
    }
}