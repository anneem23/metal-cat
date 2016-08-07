package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import java.util.ArrayList;
import java.util.List;

public class Shared {

	public static final float SAMPLE_RATE = 44100;
	public static final int HOP_SIZE = 512;
	public static final int FRAME_SIZE = 2* HOP_SIZE;

	public static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 32, 1, true, false);

    private Shared() {}
	
	public static List<Info> getMixerInfo(
			final boolean supportsPlayback, final boolean supportsRecording) {
		final List<Info> infos = new ArrayList<>();
		final Info[] mixers = AudioSystem.getMixerInfo();
		for (final Info mixerinfo : mixers) {
			try (Mixer mixer = AudioSystem.getMixer(mixerinfo)) {
				if (supportsRecording
						&& mixer.getTargetLineInfo().length != 0) {
					// Mixer capable of recording audio if target LineWavelet length != 0
					infos.add(mixerinfo);
				} else if (supportsPlayback
						&& mixer.getSourceLineInfo().length != 0) {
					// Mixer capable of audio play back if source LineWavelet length != 0
					infos.add(mixerinfo);
				}
			}
		}
		return infos;
	}
}
