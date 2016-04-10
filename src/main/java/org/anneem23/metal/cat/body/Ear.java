package org.anneem23.metal.cat.body;

import org.anneem23.metal.cat.raumfeld.MetalListener;
import org.fourthline.cling.controlpoint.SubscriptionCallback;

/**
 * Ear Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #Ear(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class Ear {

    private final MetalCallback callback;

    public Ear(MetalCallback callback) {
        this.callback = callback;
    }

    public void listen() {
        /*Thread clientThread = new Thread(new MetalListener(callback));
        clientThread.setDaemon(false);
        clientThread.start();*/
    }
}
