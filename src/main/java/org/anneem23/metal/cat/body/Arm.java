package org.anneem23.metal.cat.body;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final int START_POSITION = 15;

    public Arm() {
        final GpioController gpio = GpioFactory.getInstance();
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "Metal Cat Servo", PinState.LOW);
        SoftPwm.softPwmCreate(RaspiPin.GPIO_15.getAddress(), START_POSITION, 200);
    }


    @Override
    public void dance(int bpm) throws InterruptedException {
        double hertz = bpm / 60.0;
        int millis = (int) (1000 / hertz);

        SoftPwm.softPwmWrite(RaspiPin.GPIO_15.getAddress(), 10);
        Thread.sleep(millis);
        SoftPwm.softPwmWrite(RaspiPin.GPIO_15.getAddress(), START_POSITION);
    }



}
