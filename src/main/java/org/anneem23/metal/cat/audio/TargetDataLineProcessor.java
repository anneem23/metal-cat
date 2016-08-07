package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

/**
 * An AudioProcessor to read chunks of audio data
 * from a {@link TargetDataLine}.
 *
 * @author anneem23
 */
public class TargetDataLineProcessor implements AudioProcessor {

    private final TargetDataLine dataLine;
    private int bytesRead;

    public TargetDataLineProcessor(TargetDataLine targetLine) {
        this.dataLine = targetLine;
    }

    @Override
    public void openStream() throws Exception {
        dataLine.open(dataLine.getFormat());
        dataLine.start();
    }

    @Override
    public byte[] readBytes(int bytes) throws IOException {
        byte[] targetData = new byte[Shared.FRAME_SIZE];
        bytesRead = dataLine.read(targetData, 0, targetData.length);

        return targetData;
    }

    @Override
    public boolean bytesAvailable() {
        return bytesRead != -1;
    }

    @Override
    public AudioFormat getFormat() {
        return dataLine.getFormat();
    }
}
