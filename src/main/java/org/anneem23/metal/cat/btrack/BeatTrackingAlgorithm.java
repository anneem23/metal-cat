package org.anneem23.metal.cat.btrack;

/**
 * Beat tracking algorithm intended for use in live beat tracking
 * <p>
 * <p>Provides API method to process chunks of audio data
 * <p>
 * <p>Implementors:  {@link BeatTracker}
 *
 * @author anneem23
 */
public interface BeatTrackingAlgorithm {

    /**
     * process an audio frame for the purpose of beat tracking
     * @param audioBuffer audio buffer in doubles
     */
    void processAudioFrame(double[] audioBuffer);

    /**
     * is there a beat in the current frame
     * @return true if there is a beat, false if not
     */
    boolean isBeatDueInFrame();

    int getTempo();
}
