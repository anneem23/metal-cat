package org.anneem23.metal.cat.btrack.tempo;

import org.anneem23.metal.cat.audio.Shared;
import org.junit.Test;

import static org.anneem23.metal.cat.btrack.tempo.TempoEstimation.MAX_BPM;
import static org.anneem23.metal.cat.btrack.tempo.TempoEstimation.MIN_BPM;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class TempoEstimationTest {

    private TempoEstimation tempoEstimation = new TempoEstimation(Shared.HOP_SIZE);

    private double[] data;

    @Test
    public void estimateTempoForSilence() {
        givenSilence();
        whenEstimatingTempo();
        thenTempoIsMin();
    }

    @Test
    public void estimateTempoForLoudness() {
        givenLoudness();
        whenEstimatingTempo();
        thenTempoIsMax();
    }

    @Test
    public void estimateTempoForRealData() {
        givenRealDataOf140bpmRecording();
        whenEstimatingTempo();
        thenTempoIs140bpm();
    }

    private void thenTempoIs140bpm() {
        assertThat(tempoEstimation.tempo(), is(greaterThan(135.0)));
        assertThat(tempoEstimation.tempo(), is(lessThanOrEqualTo(140.0)));
    }

    private void givenRealDataOf140bpmRecording() {
        data = new double[]{0.09743523597717285, 0.8999999761581421, 0.09743523597717285, -0.09006049484014511,
                0.07877794653177261, -0.0649149939417839, 0.04999982938170433, -0.035516683012247086, 0.022689620032906532,
                -0.012334355153143406, 0.004799828864634037, 1.245794717561821E-17, -0.0024829760659486055, 0.003255888819694519,
                -0.0029675629921257496, 0.002188563346862793, -0.0013367600040510297, 6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 6.538678426295519E-4, -0.0013367600040510297, 0.002188563346862793, -0.0029675629921257496,
                0.003255888819694519, -0.0024829760659486055, 1.245794717561821E-17, 0.004799828864634037, -0.012334355153143406,
                0.022689620032906532, -0.035516683012247086, 0.04999982938170433, -0.0649149939417839, 0.07877794653177261,
                -0.09006049484014511, 0.09743523597717285, 0.8999999761581421, 0.09743523597717285, -0.09006049484014511, 0.07877794653177261,
                -0.0649149939417839, 0.04999982938170433, -0.035516683012247086, 0.022689620032906532, -0.012334355153143406,
                0.004799828864634037, 1.245794717561821E-17, -0.0024829760659486055, 0.003255888819694519, -0.0029675629921257496,
                0.002188563346862793, -0.0013367600040510297, 6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                6.538678426295519E-4, -0.0013367600040510297, 0.002188563346862793, -0.0029675629921257496, 0.003255888819694519,
                -0.0024829760659486055, 1.245794717561821E-17, 0.004799828864634037, -0.012334355153143406, 0.022689620032906532,
                -0.035516683012247086, 0.04999982938170433, -0.0649149939417839, 0.07877794653177261, -0.09006049484014511,
                0.09743523597717285, 0.8999999761581421, 0.09743523597717285, -0.09006049484014511, 0.07877794653177261, -0.0649149939417839,
                0.04999982938170433, -0.035516683012247086, 0.022689620032906532, -0.012334355153143406, 0.004799828864634037,
                1.245794717561821E-17, -0.0024829760659486055, 0.003255888819694519, -0.0029675629921257496, 0.002188563346862793, -0.0013367600040510297,
                6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.538678426295519E-4, -0.0013367600040510297,
                0.002188563346862793, -0.0029675629921257496, 0.003255888819694519, -0.0024829760659486055, 1.245794717561821E-17, 0.004799828864634037,
                -0.012334355153143406, 0.022689620032906532, -0.035516683012247086, 0.04999982938170433, -0.0649149939417839, 0.07877794653177261,
                -0.09006049484014511, 0.09743523597717285, 0.8999999761581421, 0.09743523597717285, -0.09006049484014511, 0.07877794653177261, -0.0649149939417839,
                0.04999982938170433, -0.035516683012247086, 0.022689620032906532, -0.012334355153143406, 0.004799828864634037, 1.245794717561821E-17,
                -0.0024829760659486055, 0.003255888819694519, -0.0029675629921257496, 0.002188563346862793, -0.0013367600040510297, 6.538678426295519E-4,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.538678426295519E-4, -0.0013367600040510297, 0.002188563346862793, -0.0029675629921257496,
                0.003255888819694519, -0.0024829760659486055, 1.245794717561821E-17, 0.004799828864634037, -0.012334355153143406, 0.022689620032906532,
                -0.035516683012247086, 0.04999982938170433, -0.0649149939417839, 0.07877794653177261, -0.09006049484014511, 0.09743523597717285,
                0.8999999761581421, 0.09743523597717285, -0.09006049484014511, 0.07877794653177261, -0.0649149939417839, 0.04999982938170433, -0.035516683012247086,
                0.022689620032906532, -0.012334355153143406, 0.004799828864634037, 1.245794717561821E-17, -0.0024829760659486055, 0.003255888819694519,
                -0.0029675629921257496, 0.002188563346862793, -0.0013367600040510297, 6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                6.538678426295519E-4, -0.0013367600040510297, 0.002188563346862793, -0.0029675629921257496, 0.003255888819694519, -0.0024829760659486055,
                1.245794717561821E-17, 0.004799828864634037, -0.012334355153143406, 0.022689620032906532, -0.035516683012247086, 0.04999982938170433,
                -0.0649149939417839, 0.07877794653177261, -0.09006049484014511, 0.09743523597717285, 0.8999999761581421, 0.09743523597717285, -0.09006049484014511,
                0.07877794653177261, -0.0649149939417839, 0.04999982938170433, -0.035516683012247086, 0.022689620032906532, -0.012334355153143406,
                0.004799828864634037, 1.245794717561821E-17, -0.0024829760659486055, 0.003255888819694519, -0.0029675629921257496, 0.002188563346862793,
                -0.0013367600040510297, 6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.538678426295519E-4, -0.0013367600040510297,
                0.002188563346862793, -0.0029675629921257496, 0.003255888819694519, -0.0024829760659486055, 1.245794717561821E-17, 0.004799828864634037,
                -0.012334355153143406, 0.022689620032906532, -0.035516683012247086, 0.04999982938170433, -0.0649149939417839, 0.07877794653177261,
                -0.09006049484014511, 0.09743523597717285, 0.8999999761581421, 0.09743523597717285, -0.09006049484014511, 0.07877794653177261, -0.0649149939417839,
                0.04999982938170433, -0.035516683012247086, 0.022689620032906532, -0.012334355153143406, 0.004799828864634037, 1.245794717561821E-17,
                -0.0024829760659486055, 0.003255888819694519, -0.0029675629921257496, 0.002188563346862793, -0.0013367600040510297, 6.538678426295519E-4, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.538678426295519E-4, -0.0013367600040510297, 0.002188563346862793, -0.0029675629921257496,
                0.003255888819694519, -0.0024829760659486055, 1.245794717561821E-17, 0.004799828864634037, -0.012334355153143406, 0.022689620032906532,
                -0.035516683012247086, 0.04999982938170433, -0.0649149939417839, 0.07877794653177261, -0.09006049484014511, 0.09743523597717285,
                0.8999999761581421, 0.09743523597717285, -0.09006049484014511, 0.07877794653177261, -0.0649149939417839, 0.04999982938170433,
                -0.035516683012247086, 0.022689620032906532, -0.012334355153143406, 0.004799828864634037, 1.245794717561821E-17, -0.0024829760659486055,
                0.003255888819694519, -0.0029675629921257496, 0.002188563346862793, -0.0013367600040510297, 6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 6.538678426295519E-4, -0.0013367600040510297, 0.002188563346862793, -0.0029675629921257496, 0.003255888819694519,
                -0.0024829760659486055, 1.245794717561821E-17, 0.004799828864634037, -0.012334355153143406, 0.022689620032906532, -0.035516683012247086,
                0.04999982938170433, -0.0649149939417839, 0.07877794653177261, -0.09006049484014511, 0.09743523597717285, 0.8999999761581421, 0.09743523597717285,
                -0.09006049484014511, 0.07877794653177261, -0.0649149939417839, 0.04999982938170433, -0.035516683012247086, 0.022689620032906532,
                -0.012334355153143406, 0.004799828864634037, 1.245794717561821E-17, -0.0024829760659486055, 0.003255888819694519, -0.0029675629921257496,
                0.002188563346862793, -0.0013367600040510297, 6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.538678426295519E-4,
                -0.0013367600040510297, 0.002188563346862793, -0.0029675629921257496, 0.003255888819694519, -0.0024829760659486055, 1.245794717561821E-17,
                0.004799828864634037, -0.012334355153143406, 0.022689620032906532, -0.035516683012247086, 0.04999982938170433, -0.0649149939417839,
                0.07877794653177261, -0.09006049484014511, 0.09743523597717285, 0.8999999761581421, 0.09743523597717285, -0.09006049484014511, 0.07877794653177261,
                -0.0649149939417839, 0.04999982938170433, -0.035516683012247086, 0.022689620032906532, -0.012334355153143406, 0.004799828864634037,
                1.245794717561821E-17, -0.0024829760659486055, 0.003255888819694519, -0.0029675629921257496, 0.002188563346862793, -0.0013367600040510297,
                6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.538678426295519E-4, -0.0013367600040510297, 0.002188563346862793,
                -0.0029675629921257496, 0.003255888819694519, -0.0024829760659486055, 1.245794717561821E-17, 0.004799828864634037, -0.012334355153143406,
                0.022689620032906532, -0.035516683012247086, 0.04999982938170433, -0.0649149939417839, 0.07877794653177261, -0.09006049484014511,
                0.09743523597717285, 0.8999999761581421, 0.09743523597717285, -0.09006049484014511, 0.07877794653177261, -0.0649149939417839, 0.04999982938170433,
                -0.035516683012247086, 0.022689620032906532, -0.012334355153143406, 0.004799828864634037, 1.245794717561821E-17, -0.0024829760659486055,
                0.003255888819694519, -0.0029675629921257496, 0.002188563346862793, -0.0013367600040510297, 6.538678426295519E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.995711088180542, -0.9681545495986938, 1.6421756744384766, -1.9507296085357666, 1.7585783004760742, -0.6958582997322083, -1.6066011190414429,
                5.443854331970215, -10.947713851928711, 18.024511337280273, -26.31492042541504, 35.212345123291016, -43.92070770263672, 51.56642150878906,
                -57.323978424072266, 60.55282211303711, 1461.905517578125, 1690.8853759765625, 698.8248901367188, 47.18833923339844, -36.94514083862305,
                30.369876861572266, -20.266502380371094, 15.090181350708008, -7.402144432067871, 5.209743976593018, -0.53849858045578, 0.7485429048538208,
                1.8494080305099487, -0.11193503439426422, 1.8996111154556274, 0.23751363158226013, 1.4356553554534912, 1.08653724193573, 1.5059581995010376,
                0.9740831851959229, 1.0059912204742432, 1.521623969078064, 0.4018171429634094, 2.0321459770202637, -0.34159690141677856, 2.335871696472168,
                0.10541427135467529, 0.5533748865127563, 3.659374952316284, -5.135411262512207, 11.825541496276855, -15.41501235961914, 23.906173706054688,
                -28.35930633544922, 36.418601989746094, -39.394771575927734, 125.3799819946289, 961.2078857421875, 969.59326171875, 430.2826843261719,
                37.58631896972656, -29.759706497192383};
    }

    private void thenTempoIsMax() {
        assertThat(tempoEstimation.tempo(), greaterThan(MIN_BPM));
        assertThat(tempoEstimation.tempo(), lessThanOrEqualTo(MAX_BPM));
    }


    private void thenTempoIsMin() {
        assertThat(tempoEstimation.tempo(), lessThanOrEqualTo(MIN_BPM));
    }

    private void whenEstimatingTempo() {
        tempoEstimation.calculateTempo(data);
    }

    private void givenLoudness() {
        data = new double[512];
        for (int i = 0; i < 512; i++) {
            data[i] = Double.MAX_VALUE;
        }

    }

    private void givenSilence() {
        data = new double[512];
        for (int i = 0; i < 512; i++) {
            data[i] = 0.0;
        }
    }

}