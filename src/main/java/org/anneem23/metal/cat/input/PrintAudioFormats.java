package org.anneem23.metal.cat.input;

import javax.sound.sampled.*;
import java.util.Map;

/**
 * PrintAudioFormats Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #PrintAudioFormats(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class PrintAudioFormats {

    public static void main(String[] args) {
        for (Mixer.Info info : Shared.getMixerInfo(false, true)) {
            try {
                printInfos(info);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printInfos(Mixer.Info mixerInfo) throws LineUnavailableException {
        Mixer mixer = AudioSystem.getMixer(mixerInfo); // default mixer
        mixer.open();

        System.out.printf("Supported SourceDataLines of default mixer (%s):\n\n", mixer.getMixerInfo().getName());
        for(Line.Info info : mixer.getSourceLineInfo()) {
            if(SourceDataLine.class.isAssignableFrom(info.getLineClass())) {
                SourceDataLine.Info info2 = (SourceDataLine.Info) info;
                System.out.println(info2);
                System.out.printf("  max buffer size: \t%d\n", info2.getMaxBufferSize());
                System.out.printf("  min buffer size: \t%d\n", info2.getMinBufferSize());
                AudioFormat[] formats = info2.getFormats();
                System.out.println("  Supported Audio formats: ");
                for(AudioFormat format : formats) {
                    System.out.println("    "+format);
          System.out.printf("      encoding:           %s\n", format.getEncoding());
          System.out.printf("      channels:           %d\n", format.getChannels());
          System.out.printf(format.getFrameRate()==-1?"":"      frame rate [1/s]:   %s\n", format.getFrameRate());
          System.out.printf("      frame size [bytes]: %d\n", format.getFrameSize());
          System.out.printf(format.getSampleRate()==-1?"":"      sample rate [1/s]:  %s\n", format.getSampleRate());
          System.out.printf("      sample size [bit]:  %d\n", format.getSampleSizeInBits());
          System.out.printf("      big endian:         %b\n", format.isBigEndian());

          Map<String,Object> prop = format.properties();
          if(!prop.isEmpty()) {
              System.out.println("      Properties: ");
              for(Map.Entry<String, Object> entry : prop.entrySet()) {
                  System.out.printf("      %s: \t%s\n", entry.getKey(), entry.getValue());
              }
          }
                }
                System.out.println();
            } else {
                System.out.println(info.toString());
            }
            System.out.println();
        }

        mixer.close();
    }

}
