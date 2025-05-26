package desmoj.core.dist;

import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * Erlang distributed stream of pseudo random numbers of type double. Erlang distributed streams are specified by a mean
 * value and their order.
 *
 * @author Tim Lechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public class ContDistErlang extends ContDistExponential {

    /**
     * The order "k" of the Erlang distribution
     */
    protected long k;

    /**
     * Creates a stream of pseudo random numbers following a normal (a.k.a. "Gaussian") distribution. The specific mean
     * and standard deviation values have to be given here at creation time, as well as the seed for the underlying
     * pseudo random generator.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param order        long : The order of the Erlang distribution (>=1)
     * @param mean         double : The mean value for this distribution
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public ContDistErlang(Model owner, String name, long order, double mean, boolean showInReport,
                          boolean showInTrace) {
        super(owner, name, mean, showInReport, showInTrace);

        k = order;
    }

    /**
     * Creates the default reporter for the RealDistErlang distribution.
     *
     * @return Reporter : The reporter for the RealDistErlang distribution
     * @see desmoj.core.report.ContDistErlangReporter
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.ContDistErlangReporter(this);

    }

    /**
     * Abstract method to map a double <code>p</code> from 0...1 to the distribution's domain by determining the value x
     * that satisfies
     * <code>P(X &lt; x) = p</code>.
     *
     * @param p double: A value between 0 and 1
     * @return Double : The value x that satisfies <code>P(X &lt; x) = p</code>
     */
    @Override
    public Double getInverseOfCumulativeProbabilityFunction(final double p) {

        double newSample = StatisticObject.UNDEFINED; //
        var gammadist = new GammaDistribution(k, this.mean); // special case

        try {
            newSample = gammadist.inverseCumulativeProbability(p);
        } catch (OutOfRangeException e) {
        }

        return newSample;
    }

    /**
     * Returns the mean value of this Erlang distribution.
     *
     * @return double : The mean value of this Erlang distribution.
     */
    @Override
    public double getMean() {

        return super.getMean();

    }

    /**
     * Returns the order of the Erlang distribution.
     *
     * @return double : The order of this Erlang distribution.
     */
    public long getOrder() {

        return k;

    }

    /**
     * Returns the next Erlang distributed sample from this distribution. The algorithm used is taken from DESMO-C from
     * Thomas Schniewind [Schni98] Volume 2, page 222, file realdist.cc. It has been adapted to Java and extended to
     * handle antithetic random numbers if antithetic mode is switched on.
     *
     * @return Double : The next Erlang distributed sample
     */
    @Override
    public Double sample() {

        double newSample = 0.0; // auxiliary variable for computing the sample

        for (int i = 1; i <= k; i++) { // iteration over order of Erlang number
            newSample += super.sample();
        }

        newSample = newSample / k;

        incrementObservations(-(k - 1)); // super class was sampled k times,
        // reduce observations by k-1 so
        // that one Erlang
        // sample counts only as one
        // observation

        if (this.currentlySendTraceNotes()) {
            this.traceLastSample(Double.toString(newSample));
        }

        return newSample;

    }
}
