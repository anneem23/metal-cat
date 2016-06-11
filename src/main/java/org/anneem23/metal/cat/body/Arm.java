package org.anneem23.metal.cat.body;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//TODO Runnable movement?
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

    private static final int START_POSITION = 20;    // 360 degrees in radians
    private static final double THREE_HUNDRED_SIXTY_DEGREES = Math.PI * 2;

    private final ExecutorService executorService;

    private final AtomicInteger position = new AtomicInteger(0);
    private final AtomicInteger duration = new AtomicInteger(0);
    private final AtomicBoolean moving = new AtomicBoolean(Boolean.FALSE);

    public Arm() {
        executorService = Executors.newFixedThreadPool(10);
        final GpioController gpio = GpioFactory.getInstance();
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "Metal Cat Servo", PinState.LOW);
        SoftPwm.softPwmCreate(RaspiPin.GPIO_15.getAddress(), START_POSITION, 200);
    }

    public void move(int position, int duration) throws InterruptedException {
        SoftPwm.softPwmWrite(RaspiPin.GPIO_15.getAddress(), position);
        Thread.sleep(duration/2);
        SoftPwm.softPwmWrite(RaspiPin.GPIO_15.getAddress(), START_POSITION);
        Thread.sleep(duration/2);
    }

    @Override
    public void updateBpm(double bpm) {
        double hertz = bpm / 60.0;
        double result = Math.sin(hertz * THREE_HUNDRED_SIXTY_DEGREES);
        ////System.out.println("new arm position: " + ((int) (-4 * result) + 14));
        this.position.set((int) (-4 * result) + 14);
        this.duration.set((int) (1000 / hertz));
    }

    @Override
    public void dance() throws InterruptedException {
        //System.out.println("Metal cats arm starts to move.");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (isDancing()) {
                    try {
                        move(position.get(),duration.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public boolean isDancing() {
        return moving.get();
    }

    @Override
    public void setDancing(boolean dancing) {
        moving.set(dancing);
    }


}
