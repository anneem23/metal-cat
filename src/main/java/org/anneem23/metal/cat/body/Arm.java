package org.anneem23.metal.cat.body;

import com.pi4j.io.gpio.*;
import com.pi4j.wiringpi.SoftPwm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MetalCats' Arm.
 * <p>
 * <P>Talks to the servo via GPIO Pin 15
 * <p>
 * <P>See {@link SoftPwm} for more info.
 *
 * @author anneem23
 * @version 2.0
 */
public class Arm implements Moveable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Arm.class);
    private final ExecutorService executorService;
    private final Runnable movement;

    public Arm() {
        executorService = Executors.newFixedThreadPool(1);
        movement = new LedMovement();
    }


    @Override
    public void dance(int bpm) throws InterruptedException {
        movement.run();
    }

    class LedMovement implements Runnable {

        private final GpioPinDigitalOutput pin;

        public LedMovement() {
            // create gpio controller
            final GpioController gpio = GpioFactory.getInstance();

            // provision gpio pin #01 as an output pin and turn on
            pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MetalLED", PinState.LOW);

            // set shutdown state for this pin
            pin.setShutdownOptions(true, PinState.LOW);
            pin.low();
        }

        @Override
        public void run() {
            pin.toggle();
        }
    }


    class ServoMovement implements Runnable {
        private static final int START_POSITION = 15;

        public ServoMovement() {
            final GpioController gpio = GpioFactory.getInstance();
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "Metal Cat Servo", PinState.LOW);
            SoftPwm.softPwmCreate(RaspiPin.GPIO_15.getAddress(), 0, 100);
        }

        @Override
        public void run() {
            try {
                SoftPwm.softPwmWrite(RaspiPin.GPIO_15.getAddress(), 10);
                Thread.sleep(100);
                SoftPwm.softPwmWrite(RaspiPin.GPIO_15.getAddress(), START_POSITION);
                //Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("Arm movement failed due to ", e);
            }
        }
    }
}
