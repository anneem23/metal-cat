package org.anneem23.metal.cat.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;

/**
 * AudioInputStreamProcessor is an {@link AudioProcessor} for {@link AudioInputStream}.
 * Can also read {@link java.io.File}
 * <p>
 * <p>Reads audio data into {@code byte[]}
 *
 * @author anneem23
 *
 */
public class AudioInputStreamProcessor implements AudioProcessor {

    private final AudioInputStream audioInputStream;
    private final AudioFormat _format;
    private int _bytesRead;

    public AudioInputStreamProcessor(AudioInputStream audioInputStream) {
        this.audioInputStream = audioInputStream;
        _format = audioInputStream.getFormat();
    }

    public AudioInputStreamProcessor(InputStream audioInputStream) throws IOException, UnsupportedAudioFileException {
        this(getAudioInputStream(audioInputStream));
    }

    public AudioInputStreamProcessor(File file)
            throws UnsupportedAudioFileException, IOException {
        this(getAudioInputStream(file));
    }


    public byte[] readBytes() throws UnsupportedAudioFileException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Read the audio data into a memory buffer.
        int nBufferSize = Shared.FRAME_SIZE * _format.getFrameSize();


        byte[] abBuffer = new byte[nBufferSize];
        int nBytesRead;

        while ((nBytesRead = this.audioInputStream.read(abBuffer)) != -1) {
            baos.write(abBuffer, 0, nBytesRead);
        }

        return baos.toByteArray();
    }

    @Override
    public byte[] readBytes(int bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] abBuffer = new byte[bytes];

        _bytesRead = this.audioInputStream.read(abBuffer);

        return abBuffer;
    }

    @Override
    public boolean bytesAvailable() {
        return _bytesRead != -1;
    }

    @Override
    public AudioFormat getFormat() {
        return _format;
    }
}
