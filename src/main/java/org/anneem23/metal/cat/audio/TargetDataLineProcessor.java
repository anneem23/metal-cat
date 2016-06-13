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

    private final TargetDataLine _line;
    private int _bytesRead;

    public TargetDataLineProcessor(TargetDataLine targetLine) {
        this._line = targetLine;
    }

    @Override
    public void openStream() throws Exception {
        _line.open(new AudioFormat(48000, 16, 1, true, false));
        _line.start();
    }

    @Override
    public byte[] readBytes(int bytes) throws IOException {
        byte[] targetData = new byte[Shared.FRAME_SIZE];
        _bytesRead = _line.read(targetData, 0, targetData.length);

        return targetData;
    }

    @Override
    public boolean bytesAvailable() {
        return _bytesRead != -1;
    }

    @Override
    public AudioFormat getFormat() {
        return _line.getFormat();
    }
}
