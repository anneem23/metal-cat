package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

/**
 * AudioProcessor reads Audio input data.
 *
 */
public interface AudioProcessor {

    void openStream() throws Exception;

    byte[] readBytes(int bytes) throws IOException;

    boolean bytesAvailable();

    AudioFormat getFormat();

}
