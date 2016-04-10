package org.anneem23.metal.cat.body;

import java.net.URI;

/**
 * MetalCallback Model Object.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #CatCallback(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
@FunctionalInterface
public interface MetalCallback {
    void dance(URI url) throws InterruptedException;
}
