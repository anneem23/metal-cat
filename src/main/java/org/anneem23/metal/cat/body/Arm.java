package org.anneem23.metal.cat.body;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

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
public class Arm {

    public Arm() {
        final GpioController gpio = GpioFactory.getInstance();
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "Metal Cat Servo", PinState.LOW);
        SoftPwm.softPwmCreate(15, 0, 200);
    }

    public void move(int position) throws InterruptedException {
        SoftPwm.softPwmWrite(15, position);
        Thread.sleep(20);
    }



    public void move() throws InterruptedException {
        SoftPwm.softPwmWrite(15, 20);
        SoftPwm.softPwmWrite(15, 15);
        //Thread.sleep(20);
    }

}
