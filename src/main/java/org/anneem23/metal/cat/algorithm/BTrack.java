package org.anneem23.metal.cat.algorithm;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.onsets.OnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PrintOnsetHandler;
import org.anneem23.metal.cat.algorithm.function.ComplexSpectralDifference;
import org.anneem23.metal.cat.algorithm.function.OnsetDetectionFunction;

public class BTrack implements AudioProcessor, OnsetDetector {

    private OnsetHandler _onsetHandler;
    private final OnsetDetectionFunction _onsetDetector;

    private int _m0 = 10;
    private double _tightness = 5;
    private double _alpha = 0.9;
    private final double[] _acf = new double[512];
    private final int _hopSize;

    private final int _onsetDFBufferSize;
    private double[] _onsetDF;

    private double _estimatedTempo = 120.0;
    private final int _tempo = 120;
    private final boolean _tempoFixed = false;
    private final double _tempoToLagFactor = 60.*44100./512.;
    private double[][] _tempoTransitionMatrix = new double[41][41]; /**<  tempo transition matrix */
    private double[] _tempoObservationVector = new double[41];

    private double[] _cumulativeScore;
    private double _latestCumulativeScoreValue;

    private long _beatPeriod;
    private boolean _beatDueInFrame;
    private int _beatCounter = -1;

    private double[] _combFilterBankOutput = new double[128];
    private double[] _weightingVector = new double[128];

    private double[] _delta = new double[41];                       /**<  to hold final tempo candidate array */
    private double[] _prevDelta = new double[41];                   /**<  previous delta */
    private double[] _prevDeltaFixed = new double[41];              /**<  fixed tempo version of previous delta */





    public BTrack(AudioDispatcher d, int fftSize, int hopSize){
        double rayparam = 43;
        _onsetDetector = new ComplexSpectralDifference(fftSize, hopSize);
        _onsetDFBufferSize = (512*512)/hopSize;
        _onsetDF = new double[_onsetDFBufferSize];
        _cumulativeScore = new double[_onsetDFBufferSize];
        _beatPeriod = Math.round(60/((((double) hopSize)/44100)*_tempo));
        _hopSize = hopSize;


        _onsetHandler = new PrintOnsetHandler();

        d.addAudioProcessor(this);

        initializeArrays();

        // create rayleigh weighting vector
        for (int n = 0;n < 128;n++)
        {
            _weightingVector[n] = ((double) n / Math.pow(rayparam,2)) * Math.exp((-1*Math.pow((double)-n,2)) / (2*Math.pow(rayparam,2)));
        }

        // initialise prev_delta
        for (int i = 0;i < 41;i++)
        {
            _prevDelta[i] = 1;
        }

        double t_mu = 41/2;
        double m_sig;
        double x;
        // create tempo transition matrix
        m_sig = 41/8;
        for (int i = 0;i < 41;i++)
        {
            for (int j = 0;j < 41;j++)
            {
                x = j+1;
                t_mu = i+1;
                _tempoTransitionMatrix[i][j] = (1 / (m_sig * Math.sqrt(2*Math.PI))) * Math.exp( (-1*Math.pow((x-t_mu),2)) / (2*Math.pow(m_sig,2)) );
            }
        }


    }

    private void initializeArrays() {
        // initialise df_buffer to zeros
        for (int i = 0;i < _onsetDFBufferSize;i++)
        {
            _onsetDF[i] = 0;
            _cumulativeScore[i] = 0;


            if ((i %  Math.round(_beatPeriod)) == 0)
            {
                _onsetDF[i] = 1;
            }

        }

    }


    public boolean process(AudioEvent audioEvent) {
        double odfSample = _onsetDetector.onsetDetection(audioEvent);
        findOnsets(odfSample, audioEvent.getTimeStamp());
        System.out.println("estimated tempo: " + _estimatedTempo);
        return true;
    }

    public void processingFinished() {
//        System.out.println("estimated tempo: " + _estimatedTempo);
    }

    private void findOnsets(double odfSample, double timeStamp){
        // we need to ensure that the onset
        // detection function sample is positive
        odfSample = Math.abs(odfSample);

        // add a tiny constant to the sample to stop it from ever going
        // to zero. this is to avoid problems further down the line
        odfSample = odfSample + 0.0001;

        _m0--;
        _beatCounter--;
        _beatDueInFrame = false;

        // move all samples back one step
        for (int i=0;i < (_onsetDFBufferSize-1);i++)
        {
            _onsetDF[i] = _onsetDF[i+1];
        }

        // add new sample at the end
        _onsetDF[_onsetDFBufferSize-1] = odfSample;

        // update cumulative score
        updateCumulativeScore(odfSample);

        // if we are halfway between beats
        if (_m0 == 0)
        {
            predictBeat();
        }

        // if we are at a beat
        if (_beatCounter == 0)
        {
            _beatDueInFrame = true;	// indicate a beat should be output
            _onsetHandler.handleOnset(timeStamp,-1.0);
            // recalculate the tempo
            calculateTempo();
        }
    }

    private void updateCumulativeScore(double odfSample) {
        int start, end, winsize;
        double max;

        start = _onsetDFBufferSize - Math.round(2*_beatPeriod);
        end = _onsetDFBufferSize - Math.round(_beatPeriod/2);
        winsize = end-start+1;

        double[] w1 = new double[winsize];
        double v = -2*_beatPeriod;
        double wcumscore;


        // create window
        for (int i = 0;i < winsize;i++)
        {
            w1[i] = Math.exp((-1*Math.pow(_tightness*Math.log(-v/_beatPeriod),2))/2);
            v = v+1;
        }

        // calculate new cumulative score value
        max = 0;
        int n = 0;
        for (int i=start;i <= end;i++)
        {
            wcumscore = _cumulativeScore[i]*w1[n];

            if (wcumscore > max)
            {
                max = wcumscore;
            }
            n++;
        }

        // shift cumulative score back one
        for (int i = 0;i < (_onsetDFBufferSize-1);i++)
        {
            _cumulativeScore[i] = _cumulativeScore[i+1];
        }

        // add new value to cumulative score
        _cumulativeScore[_onsetDFBufferSize-1] = ((1-_alpha)*odfSample) + (_alpha*max);

        _latestCumulativeScoreValue = _cumulativeScore[_onsetDFBufferSize-1];

    }

    private void calculateTempo() {
        // adaptive threshold on input
        adaptiveThreshold(_onsetDF,512);

        // calculate auto-correlation function of detection function
        calculateBalancedACF(_onsetDF);

        // calculate output of comb filterbank
        calculateOutputOfCombFilterBank();


        // adaptive threshold on rcf
        adaptiveThreshold(_combFilterBankOutput,128);


        int t_index;
        int t_index2;
        // calculate tempo observation vector from beat period observation vector
        for (int i = 0;i < 41;i++)
        {
            t_index = (int) Math.round(_tempoToLagFactor / ((double) ((2*i)+80)));
            t_index2 = (int) Math.round(_tempoToLagFactor / ((double) ((4*i)+160)));


            _tempoObservationVector[i] = _combFilterBankOutput[t_index-1] + _combFilterBankOutput[t_index2-1];
        }


        double maxval;
        double maxind;
        double curval;

        // if tempo is fixed then always use a fixed set of tempi as the previous observation probability function
        if (_tempoFixed)
        {
            for (int k = 0;k < 41;k++)
            {
                _prevDelta[k] = _prevDeltaFixed[k];
            }
        }

        for (int j=0;j < 41;j++)
        {
            maxval = -1;
            for (int i = 0;i < 41;i++)
            {
                curval = _prevDelta[i]*_tempoTransitionMatrix[i][j];

                if (curval > maxval)
                {
                    maxval = curval;
                }
            }

            _delta[j] = maxval*_tempoObservationVector[j];
        }


        normaliseArray(_delta,41);

        maxind = -1;
        maxval = -1;

        for (int j=0;j < 41;j++)
        {
            if (_delta[j] > maxval)
            {
                maxval = _delta[j];
                maxind = j;
            }

            _prevDelta[j] = _delta[j];
        }

        _beatPeriod = Math.round((60.0*44100.0)/(((2*maxind)+80)*((double) _hopSize)));

        if (_beatPeriod > 0)
        {
            _estimatedTempo = 60.0/((((double) _hopSize) / 44100.0)*_beatPeriod);
        }
    }

    private void normaliseArray(double[] array, int length) {
        double sum = 0;

        for (int i = 0;i < length;i++)
        {
            if (array[i] > 0)
            {
                sum = sum + array[i];
            }
        }

        if (sum > 0)
        {
            for (int i = 0;i < length;i++)
            {
                array[i] = array[i] / sum;
            }
        }

    }

    private void calculateOutputOfCombFilterBank() {
        int numelem;

        for (int i = 0;i < 128;i++)
        {
            _combFilterBankOutput[i] = 0;
        }

        numelem = 4;

        for (int i = 2;i <= 127;i++) // max beat period
        {
            for (int a = 1;a <= numelem;a++) // number of comb elements
            {
                for (int b = 1-a;b <= a-1;b++) // general state using normalisation of comb elements
                {
                    _combFilterBankOutput[i-1] = _combFilterBankOutput[i-1] + (_acf[(a*i+b)-1]*_weightingVector[i-1])/(2*a-1);	// calculate value for comb filter row
                }
            }
        }
    }

    private void calculateBalancedACF(double[] onsetDF) {
        int l, n;
        double sum, tmp;

        // for l lags from 0-511
        for (l = 0;l < 512;l++)
        {
            sum = 0;

            // for n samples from 0 - (512-lag)
            for (n = 0;n < (512-l);n++)
            {
                tmp = onsetDF[n] * onsetDF[n+l];	// multiply current sample n by sample (n+l)
                sum = sum + tmp;	// add to sum
            }

            _acf[l] = sum / (512-l);		// weight by number of mults and add to acf buffer
        }
    }

    private void adaptiveThreshold(double[] onsetDF, int dfSize) {
        int i, k, t;
        double[] x_thresh = new double[dfSize];

        int p_post = 7;
        int p_pre = 8;

        t = Math.min(dfSize,p_post);	// what is smaller, p_post or df size. This is to avoid accessing outside of arrays

        // find threshold for first 't' samples, where a full average cannot be computed yet
        for (i = 0;i <= t;i++)
        {
            k = Math.min((i+p_pre),dfSize);
            x_thresh[i] = calculateMeanOfArray(onsetDF,1,k);
        }
        // find threshold for bulk of samples across a moving average from [i-p_pre,i+p_post]
        for (i = t+1;i < dfSize-p_post;i++)
        {
            // use Mean from apache commons-math
            x_thresh[i] = calculateMeanOfArray(onsetDF,i-p_pre,i+p_post);
        }
        // for last few samples calculate threshold, again, not enough samples to do as above
        for (i = dfSize-p_post;i < dfSize;i++)
        {
            k = Math.max((i-p_post),1);
            x_thresh[i] = calculateMeanOfArray(onsetDF,k,dfSize);
        }

        // subtract the threshold from the detection function and check that it is not less than 0
        for (i = 0;i < dfSize;i++)
        {
            onsetDF[i] = onsetDF[i] - x_thresh[i];
            if (onsetDF[i] < 0)
            {
                onsetDF[i] = 0;
            }
        }
    }

    private double calculateMeanOfArray(double[] array, int startIndex, int endIndex) {
        int i;
        double sum = 0;

        int length = endIndex - startIndex;

        // find sum
        for (i = startIndex;i < endIndex;i++)
        {
            sum = sum + array[i];
        }

        if (length > 0)
        {
            return sum / length;	// average and return
        }
        else
        {
            return 0;
        }

    }

    private void predictBeat() {
        int windowSize = (int) _beatPeriod;
        double[] futureCumulativeScore = new double[_onsetDFBufferSize + windowSize];
        double[] w2 = new double[windowSize];
        // copy cumscore to first part of fcumscore
        for (int i = 0;i < _onsetDFBufferSize;i++)
        {
            futureCumulativeScore[i] = _cumulativeScore[i];
        }

        // create future window
        double v = 1;
        for (int i = 0;i < windowSize;i++)
        {
            w2[i] = Math.exp((-1*Math.pow((v - (_beatPeriod/2)),2))   /  (2*Math.pow((_beatPeriod/2) ,2)));
            v++;
        }

        // create past window
        v = -2*_beatPeriod;
        int start = _onsetDFBufferSize - Math.round(2*_beatPeriod);
        int end = _onsetDFBufferSize - Math.round(_beatPeriod/2);
        int pastwinsize = end-start+1;
        double[] w1 = new double[pastwinsize];

        for (int i = 0;i < pastwinsize;i++)
        {
            w1[i] = Math.exp((-1*Math.pow(_tightness*Math.log(-v/_beatPeriod),2))/2);
            v = v+1;
        }



        // calculate future cumulative score
        double max;
        int n;
        double wcumscore;
        for (int i = _onsetDFBufferSize;i < (_onsetDFBufferSize+windowSize);i++)
        {
            start = i - Math.round(2*_beatPeriod);
            end = i - Math.round(_beatPeriod/2);

            max = 0;
            n = 0;
            for (int k=start;k <= end;k++)
            {
                wcumscore = futureCumulativeScore[k]*w1[n];

                if (wcumscore > max)
                {
                    max = wcumscore;
                }
                n++;
            }

            futureCumulativeScore[i] = max;
        }


        // predict beat
        max = 0;
        n = 0;

        for (int i = _onsetDFBufferSize;i < (_onsetDFBufferSize+windowSize);i++)
        {
            wcumscore = futureCumulativeScore[i]*w2[n];

            if (wcumscore > max)
            {
                max = wcumscore;
                _beatCounter = n;
            }

            n++;
        }
        // set next prediction time
        _m0 = _beatCounter+Math.round(_beatPeriod/2);

    }

    public boolean isBeatDueInFrame() {
        return _beatDueInFrame;
    }



    public void setHandler(OnsetHandler handler) {
        _onsetHandler = handler;
    }
}