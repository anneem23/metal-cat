package org.anneem23.metal.cat.algorithm;

import org.anneem23.metal.cat.body.Arm;

import java.io.IOException;
import java.net.URI;

public interface BeatMatchingAlgorithm {

    Double matchBeat(URI url) throws IOException;

}
