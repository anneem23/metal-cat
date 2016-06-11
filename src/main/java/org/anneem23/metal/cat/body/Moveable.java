package org.anneem23.metal.cat.body;

/**
 * MetalCallback
 *
 * @author anneem23
 * @version 2.0
 */
public interface Moveable {
    void updateBpm(double bpm);
    void dance() throws InterruptedException;
    boolean isDancing();
    void setDancing(boolean dancing);
}
