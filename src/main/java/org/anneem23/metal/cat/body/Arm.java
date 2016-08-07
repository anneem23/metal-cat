package org.anneem23.metal.cat.body;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

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

    private final ExecutorService executorService;
    private final ArmMovement armMovement;

    public Arm() {
        executorService = Executors.newFixedThreadPool(10);
        armMovement = new ArmMovement();
    }


    @Override
    public void dance(int bpm) throws InterruptedException {
        executorService.execute(armMovement::run);
    }


    class ArmMovement implements Runnable {
        private static final int START_POSITION = 15;

        public ArmMovement() {
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
                e.printStackTrace();
            }
        }
    }
}
