/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/


package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Vector;

public class Shared {

	public static final float SAMPLE_RATE = 44100;
	public static final int HOPSIZE = 512;
	public static final int FRAME_SIZE = 2*HOPSIZE;
	public static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
	
	public static Vector<Info> getMixerInfo(
			final boolean supportsPlayback, final boolean supportsRecording) {
		final Vector<Info> infos = new Vector<>();
		final Info[] mixers = AudioSystem.getMixerInfo();
		for (final Info mixerinfo : mixers) {
			if (supportsRecording
					&& AudioSystem.getMixer(mixerinfo).getTargetLineInfo().length != 0) {
				// Mixer capable of recording audio if target LineWavelet length != 0
				infos.add(mixerinfo);
			} else if (supportsPlayback
					&& AudioSystem.getMixer(mixerinfo).getSourceLineInfo().length != 0) {
				// Mixer capable of audio play back if source LineWavelet length != 0
				infos.add(mixerinfo);
			}
		}
		return infos;
	}

	public static String toLocalString(Object info)
	{
		if(!isWindows())
			return info.toString();
		String defaultEncoding = Charset.defaultCharset().toString();
		try
		{
			return new String(info.toString().getBytes("windows-1252"), defaultEncoding);
		}
		catch(UnsupportedEncodingException ex)
		{
			return info.toString();
		}
	}
	private static String OS = null;
	public static String getOsName()
	{
		if(OS == null)
			OS = System.getProperty("os.name");
	    return OS;
	}
	public static boolean isWindows()
	{
	   return getOsName().startsWith("Windows");
	}
}
