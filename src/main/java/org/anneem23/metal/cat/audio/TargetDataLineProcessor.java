package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

/**
 * An AudioProcessor to read chunks of audio data
 * from a {@link TargetDataLine}.
 *
 */
public class TargetDataLineProcessor implements AudioProcessor {

    private final TargetDataLine targetLine;
    private int _bytesRead;

    public TargetDataLineProcessor(TargetDataLine targetLine) {
        this.targetLine = targetLine;
    }

    @Override
    public byte[] readBytes(int bytes) throws IOException {
        byte[] targetData = new byte[Shared.FRAME_SIZE];
        _bytesRead = targetLine.read(targetData, 0, targetData.length);

        return targetData;
    }

    @Override
    public boolean bytesAvailable() {
        return _bytesRead != -1;
    }

    @Override
    public AudioFormat getFormat() {
        return targetLine.getFormat();
    }
}
