package org.anneem23.metal.cat.mind;

import be.tarsos.dsp.onsets.OnsetHandler;
import org.anneem23.metal.cat.algorithm.BeatMatchingAlgorithm;
import org.anneem23.metal.cat.body.Arm;

import java.io.IOException;
import java.net.URI;

/**
 * MetalBrain of MetalCat.
 * <p>
 * <P>Various attributes of guitars, and related behaviour.
 * <p>
 * <P>Note that {@link BigDecimal} is used to model the price - not double or float.
 * See {@link #MetalCat(String, BigDecimal, Integer)} for more information.
 *
 * @author anneem23
 * @version 2.0
 */
public class MetalBrain {
    // 360 degrees in radians
    private static final double THREE_HUNDRED_SIXTY_DEGREES = Math.PI * 2;

    private final BeatMatchingAlgorithm _algorithm;
    private final Arm _arm;

    public MetalBrain (BeatMatchingAlgorithm algorithm, Arm arm) {
        _algorithm = algorithm;
        _arm = arm;
    }

    public int armPosition(double hertz, long timeInMillis) {
        double timeInScs = Double.valueOf(timeInMillis) / 1000.0;

        double result = Math.sin(timeInScs * hertz * THREE_HUNDRED_SIXTY_DEGREES);
        System.out.println("result: " + result);
        System.out.println("new arm position: " + ((int) (5 * result) + 12));
        return (int) (5 * result) + 12;
    }


    public Double compute(URI url) {
        try {
            return _algorithm.matchBeat(url);
        } catch (IOException e) {
            System.err.println("ERROR: " + e.toString());
            e.printStackTrace();
        }
        return -1.0;
    }



   public void dance(double bpm, long duration) throws InterruptedException {
        long start = System.currentTimeMillis();
        long currentTime;
        int lastPosition = 0;
        while ((currentTime = System.currentTimeMillis() - start) < duration) {
            double hertz = compute(null) / 60;
            System.out.println("current hertz: " + hertz);
            int newPosition = armPosition(hertz, currentTime);
            if (newPosition != lastPosition) {
                _arm.move(newPosition);
                lastPosition = newPosition;
            }
        }
    }

    /* public void dance(double bpm, long duration) throws InterruptedException {
        long start = System.currentTimeMillis();
        long currentTime;
        int lastPosition = 0;
        while ((currentTime = System.currentTimeMillis() - start) < duration) {
            double bpsecs = bpm / 60;
            long interval = ((int) bpsecs) / 1000;

            _arm.move(interval);
        }
    }*/
}
