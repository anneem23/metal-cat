package org.anneem23.metal.cat.algorithm;

import org.anneem23.metal.cat.data.CommandLineHandler;
import org.anneem23.metal.cat.data.FileHandler;

import java.io.IOException;
import java.net.URI;

public class BpmTools implements BeatMatchingAlgorithm {

    private URI _url;

    public void setInput(URI url) {
        _url = url;
    }

    public Double matchBeat(URI url) throws IOException {
        if (!FileHandler.fileExists(_url)) {
            String path = FileHandler.download(_url);
            Double bpm = CommandLineHandler.getBPM(path);
            System.out.println("bpm is " + bpm);
            return bpm;
        }
        return -1.0;
    }
}
