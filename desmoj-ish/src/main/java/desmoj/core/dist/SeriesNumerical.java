package desmoj.core.dist;

import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * A series is a special distribution returning preset, user-defined entries
 * from a list. This subclass of Series serves to sample numbers from a custom
 * numerical data type. Series may be used to simulate certain non-random
 * scenarios within the simulation or to include external sources of (preudo)
 * random distributions
 * <p>
 *
 * The internal list can be set to be traversed backwards and/or to repeat once
 * its end has been reached.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Broder Fredrich
 *
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 */
public class SeriesNumerical<N extends Number> extends Series<N> {

    /**
     * Number of observations so far that did not return <code>null</code>
     */
    protected int _successfulObs;

    /**
     * The sum of all samples so far
     */
    protected double _sum;

    /**
     * The sum of all squared samples so far
     */
    protected double _sumSquare;

    /**
     * Creates a new SeriesNumerical. Default behaviour when returning samples is -
     * starting at 1st element - reading forward - non-repeating
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public SeriesNumerical(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        _sum = 0;
        _sumSquare = 0;
        _successfulObs = 0;
    }

    /**
     * Returns the mean value of all samples that have been returned via the
     * sample() method.
     *
     * @return double : The mean value of all returned samples
     */
    public double getMean() {

        if (_successfulObs == 0) {
            sendWarning("Attempt to get a mean value, but there is not "
            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "SeriesNumerical: " + this.getName() + " Method: double getMean()",
                        "You can not calculate a mean value as long as no samples haven been taken.",
                        "Make sure to ask for the mean value only after at least one sample has been taken.");

            return desmoj.core.statistic.StatisticObject.UNDEFINED;
        }

        // calculate mean
        return _sum / _successfulObs;
    }

    /**
     * Returns the standard deviation of all the values sampled so far.
     *
     * @return double : The standard deviation value of all returned samples
     */
    public double getStdDev() {

        if (_successfulObs < 2) {
            sendWarning("Attempt to get a standard deviation value, but there is not "
            + "sufficient data yet. UNDEFINED (-1.0) will be returned!",
                        "SeriesNumerical: " + this.getName() + " Method: double getStdDev()",
                        "You can not calculate a standard deviation value as long as less than two samples haven been taken.",
                        "Make sure to ask for the standard deviation value only after at least two samples have been taken.");

            return desmoj.core.statistic.StatisticObject.UNDEFINED;
        }

        // calculate the standard deviation
        return Math.sqrt(Math.abs(_successfulObs * _sumSquare - _sum * _sum) / (_successfulObs * (_successfulObs - 1)));
    }

    /**
     * Returns the next sample from the value list.
     *
     * @return N : The sample to be returned.
     */
    @Override
    public N sample() {
        N returnval = super.sample();
        if (returnval != null) {
            _sum += returnval.doubleValue();
            _sumSquare += returnval.doubleValue() * returnval.doubleValue();
            _successfulObs++;
        }
        return returnval;
    }

    /**
     * Convenience method to sample a period of random length by creating a
     * <code>TimeSpan</code> whose length is determined by sampling this series
     * (time unit is the experiment's reference units), thus replacing the pattern
     * <code>new TimeSpan(series.sample())</code>.
     *
     * @return TimeSpan : The TimeSpan sampled from this series
     */
    public TimeSpan sampleTimeSpan() {

        // Obtain a sample
        N sample = this.sample();
        if (sample.doubleValue() < 0) {
            sendWarning("Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                        "SeriesNumerical: " + getName() + " Method: TimeSpan sampleTimeSpan()",
                        "The series returned a negative sample (" + sample.doubleValue() + ").",
                        "Make sure to sample TimeSpans from non-negative series only.");
            return new TimeSpan(0);
        }

        // Return a TimeSpan using the appropriate constructor
        if (sample instanceof Double || sample instanceof Float) {
            return new TimeSpan(sample.doubleValue());
        } else {
            return new TimeSpan(sample.longValue());
        }
    }

    /**
     * Convenience method to sample a period of random length by creating a
     * <code>TimeSpan</code> whose length is determined by sampling this series
     * (time unit given explicitly as parameter), thus replacing the pattern
     * <code>new TimeSpan(series.sample(), unit)</code>.
     *
     * @param unit TimeUnit: the TimeUnit to assign to the sampled value
     *
     * @return TimeSpan : The TimeSpan sampled from this series
     */
    public TimeSpan sampleTimeSpan(TimeUnit unit) {

        if (unit == null) { // no time unit given
            throw (new desmoj.core.exception.SimAbortedException(new desmoj.core.report.ErrorMessage(null,
                                                                                                     "Can't create TimeSpan object! Simulation aborted.",
                                                                                                     "SeriesNumerical: "
                                                                                                     + getName()
                                                                                                     + " Method: TimeSpan sampleTimeSpan(TimeUnit unit)",
                                                                                                     "Time unit passed is null",
                                                                                                     "Make sure to pass a non-null time unit.",
                                                                                                     null)));
        }

        // Obtain a sample
        N sample = this.sample();
        if (sample.doubleValue() < 0) {
            sendWarning("Failed to sample a TimeSpan. Returning TimeSpan(0) instead.",
                        "SeriesNumerical: " + getName() + " Method: TimeSpan sampleTimeSpan(TimeUnit unit)",
                        "The series returned a negative sample (" + sample.doubleValue() + ").",
                        "Make sure to sample TimeSpans from non-negative series only.");
            return new TimeSpan(0);
        }

        // Return a TimeSpan using the appropriate constructor
        if (sample instanceof Double || sample instanceof Float) {
            return new TimeSpan(sample.doubleValue(), unit);
        } else {
            return new TimeSpan(sample.longValue(), unit);
        }
    }
}
