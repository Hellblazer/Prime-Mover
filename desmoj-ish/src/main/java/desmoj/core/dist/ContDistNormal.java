package desmoj.core.dist;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;

/**
 * This hybrid class is able to produce either a normally "Gaussian" distributed
 * stream of pseudo random numbers of type double (also referred to as
 * "symmetric normal distribution" for clarity) or an "asymmetric normal
 * distribution" in which different standard variance values are assumed on both
 * sides of the mode.
 * <p>
 *
 * The algorithm used for random number sampling is derived from the Java API
 * class <code>java.util.Random</code> and modified to also produce antithetic
 * values if antithetic mode is switched on.
 *
 * @see desmoj.core.dist.UniformRandomGenerator
 * @see java.util.Random
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
public class ContDistNormal extends ContDist {

    /**
     * Flag indicates whether there is already a calculated Gaussian value present
     * in the buffer variable <code>nextGaussian</code>. Necessary for algorithm
     * taken from Java API class <code>java.util.Random</code>. When calculating a
     * pseudo random number using the algorithm implemented here, two Gaussian
     * values are computed at a time, so the other value is stored to be used next
     * time the client asks for a new value, thus saving on computation time. If
     * <code>true</code>, there is a next Gaussian value already calculated, if
     * <code>false</code> a new pair of Gaussian values has to be generated.
     */
    protected boolean haveNextGaussian;

    /**
     * The mode value for this type of distribution.
     */
    protected double mode;

    /**
     * Buffer for storing the next gaussian value already calculated. Necessary for
     * algorithm taken from Java API class <code>java.util.Random</code>. When
     * computing a Gaussian value two samples of a pseudo random number stream are
     * taken and calculated to produce two gaussian values, even if only one is
     * used. So the other value is stored to be delivered next time a Gaussian value
     * is requested by a client.
     */
    protected double nextGaussian;

    /**
     * The standard deviation for this type of distribution (left)
     */
    protected double stdDevLeft;

    /**
     * The standard deviation for this type of distribution (left)
     */
    protected double stdDevRight;

    /**
     * Flag that indicates whether this distribution is asymmetric, i.e. using the
     * same standard deviation values on each side of the mode (<code>true</code>)
     * or not (<code>false</code>).
     */
    private boolean symmetric;

    /**
     * Creates a stream of pseudo random numbers following a symmetric normal (also
     * known as "Gaussian") distribution. The specific mean and standard deviation
     * values have to be given here at creation time.
     *
     * @param owner             Model : The distribution's owner
     * @param name              java.lang.String : The distribution's name
     * @param mean              double : The mean value of the normal distribution,
     *                          equal to its mode
     * @param standardDeviation double : The standard deviation for this
     *                          distribution
     * @param showInReport      boolean : Flag for producing reports
     * @param showInTrace       boolean : Flag for producing trace output
     */
    public ContDistNormal(Model owner, String name, double mean, double standardDeviation, boolean showInReport,
                          boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        this.mode = mean; // this only holds for the symmetric case
        stdDevLeft = standardDeviation;
        stdDevRight = standardDeviation;
        symmetric = true;
    }

    /**
     * Creates a stream of pseudo random numbers following an asymmetric normal
     * distribution subject to different standard deviation values applied left and
     * right to the mode. Note that (different from the symmetric case) the mean of
     * this distribution is <i>not</i> equal to the mode; the mean of the asymmetric
     * distribution can be obtained by calling <code>getMean</code>.
     *
     * @param owner                  Model : The distribution's owner
     * @param name                   java.lang.String : The distribution's name
     * @param mode                   double : The mode value of the normal
     *                               distribution
     * @param standardDeviationLeft  double : The left standard deviation for this
     *                               distribution
     * @param standardDeviationRight double : The right standard deviation for this
     *                               distribution
     * @param showInReport           boolean : Flag for producing reports
     * @param showInTrace            boolean : Flag for producing trace output
     */
    public ContDistNormal(Model owner, String name, double mode, double standardDeviationLeft,
                          double standardDeviationRight, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        this.mode = mode;
        stdDevLeft = standardDeviationLeft;
        stdDevRight = standardDeviationRight;
        symmetric = (standardDeviationLeft != standardDeviationRight) ? false : true;
    }

    /**
     * Creates the default reporter for the ContDistNormal distribution.
     *
     * @return Reporter : The reporter for the ContDistNormal distribution
     * @see desmoj.core.report.ContDistNormalReporter
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.ContDistNormalReporter(this);

    }

    /**
     * Abstract method to map a double <code>p</code> from 0...1 to the
     * distribution's domain by determining the value x that satisfies
     * <code>P(X &lt; x) = p</code>.
     *
     * @param p double: A value between 0 and 1
     *
     * @return N : The value x that satisfies <code>P(X &lt; x) = p</code>
     */
    @Override
    public Double getInverseOfCumulativeProbabilityFunction(double p) {
        try {
            return new NormalDistribution(this.getMean(), this.getStdDev()).inverseCumulativeProbability(p);
        } catch (NotStrictlyPositiveException e) {
            return StatisticObject.UNDEFINED;
        }
    }

    /**
     * Returns the mean value of this normal distribution. If this normal
     * distribution is symmetric, this value is equal to the mode as obtained by
     * <code>getMode()</code>.
     *
     * @return double : The mode value of this normal distribution.
     */
    public double getMean() {

        if (this.isSymmetric())
            return mode;
        else
            return mode + (this.stdDevRight - this.stdDevLeft) / Math.sqrt(2 * Math.PI);
    }

    /**
     * Returns the mode value of this normal distribution. If this normal
     * distribution is symmetric, this value is equal to the mean as obtained by
     * <code>getMean()</code>.
     *
     * @return double : The mode value of this normal distribution.
     */
    public double getMode() {

        return mode;
    }

    /**
     * Returns the standard deviation of this normal distribution. If this normal
     * distribution is asymmetric, the left standard deviation will be returned.
     *
     * @return double : The standard deviation of this normal distribution.
     */
    public double getStdDev() {
        return stdDevLeft; // easy
    }

    /**
     * Returns the left standard deviation of this normal distribution. If this
     * normal distribution is symmetric, this returns the same as
     * <code>getStdDevRight()</code>.
     *
     * @return double : The left standard deviation of this normal distribution.
     */
    public double getStdDevLeft() {
        return stdDevLeft;
    }

    /**
     * Returns the right standard deviation of this normal distribution. If this
     * normal distribution is symmetric, this returns the same as
     * <code>getStdDevLeft()</code>.
     *
     * @return double : The right standard deviation of this normal distribution.
     */
    public double getStdDevRight() {
        return stdDevRight;
    }

    /**
     * Returns whether this distribution is symmetric, i.e. using the same standard
     * deviation values on each side of the mode (<code>true</code>) or not
     * (<code>false</code>).
     *
     * @return boolean : Indicator for this distibution's symmetry.
     */
    public boolean isSymmetric() {
        return this.symmetric;
    }

    /**
     * Returns the next normally distributed sample from this distribution. The
     * value depends upon the seed, the number of values taken from the stream by
     * using this method before and the mean and standard deviation values specified
     * for this distribution. The basic algorithm has been taken from the Java API
     * <code>java.util.Random.nextGaussian()</code> with the feature of antithetic
     * random numbers added.
     *
     * @return Double : The next normally (also known as "Gaussian") distributed
     *         sample from this distribution.
     */
    @Override
    public Double sample() {

        double newSample; // aux variable

        incrementObservations(); // increase count of samples

        do {
            if (haveNextGaussian) { // a Gaussian already calculated?

                haveNextGaussian = false; // set Flag that last Gaussian is
                // gone

                newSample = nextGaussian < 0 ? nextGaussian * stdDevLeft + mode : nextGaussian * stdDevRight + mode; // the
                                                                                                                     // Gaussian
                                                                                                                     // value

                // gaussian

                // following code changed by Soenke

            } else {

                double v1, v2, s; // interim variables needed for calculation

                if (isAntithetic()) {

                    do { // loop with antithetic random numbers switched on
                        v1 = 2 * (1 - randomGenerator.nextDouble()) - 1; // between
                        // -1
                        // and
                        // 1
                        v2 = 2 * (1 - randomGenerator.nextDouble()) - 1; // between
                        // -1
                        // and
                        // 1
                        s = v1 * v1 + v2 * v2;
                    } while (s >= 1);

                } else {

                    do { // loop with normal random number generation
                        v1 = 2 * randomGenerator.nextDouble() - 1; // between
                        // -1
                        // and 1
                        v2 = 2 * randomGenerator.nextDouble() - 1; // between
                        // -1
                        // and 1
                        s = (v1 * v1) + (v2 * v2);
                    } while (s >= 1);

                }

                double multiplier = Math.sqrt(-2 * Math.log(s) / s);
                nextGaussian = v2 * multiplier; // 2nd Gaussian stored for
                // future requests
                haveNextGaussian = true; // set flag that other Gaussian is
                // available
                newSample = v1 < 0 ? (v1 * multiplier) * stdDevLeft + mode : (v1 * multiplier) * stdDevRight + mode; // the
                                                                                                                     // Gaussian
                                                                                                                     // value

            }
        } while (nonNegative && newSample < 0); // get a new sample if it should
        // be non negative but actually is negative.

        if (this.currentlySendTraceNotes())
            this.traceLastSample(Double.toString(newSample));

        return newSample;
    }
}
