package org.anneem23.metal.cat.btrack.fft;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ComplexTest {

    @Test
    public void canDoTan() {
        Complex a = new Complex(5.0, 6.0);
        Complex expedted = new Complex(-0.000006685231390246571 , 1.0000103108981198);
        assertThat(a.tan(), is(equalTo(expedted)));
    }

    @Test
    public void canDoAbs() {
        Complex a = new Complex(5.0, 6.0);
        assertThat(a.abs(), is(7.810249675906654));
    }

    @Test
    public void canDoConjugate() {
        Complex a = new Complex(5.0, 6.0);
        Complex expected = new Complex(5.0, -6.0);
        assertThat(a.conjugate(), is(equalTo(expected)));
    }

    @Test
    public void canDivideTwoComplexNumbers() {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(-3.0, 4.0);
        Complex expected = new Complex(0.36, -1.52);
        assertThat(a.divides(b), is(equalTo(expected)));
    }

}