package desmoj.core.dist;

import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;

/**
 * Distribution returning values according to a user-specified distribution function.
 *
 * It is important that the used function is an actual distribution function, meaning it must be strictly monotonically
 * increasing from 0 to 1. You also need to specify the lower and upper bounds of the distribution, meaning the values
 * at which the cumulative probability is 0 and 1 respectively. If these requirements are not fulfilled, the class might
 * return false and/or no samples.
 *
 * @param <N>
 * @author Peter Wueppen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */

public class ContDistCustom extends ContDist {

    /**
     * User-defined distribution function
     */
    protected Function distFunction;

    /**
     * Lower bound of possibly sampled values. This is needed for the underlying zero root solving algorithms.
     */
    protected double lowerBound;

    /**
     * Upper bound of possibly sampled values. This is needed for the underlying zero root solving algorithms.
     */
    protected double upperBound;

    /**
     * Instantiates the CustomContDist, making it possible to get samples. The user-defined CustomFunction has to be
     * given here.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param function     Function: User-defined distribution Function
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public ContDistCustom(Model owner, String name, Function function, double lower, double upper, boolean showInReport,
                          boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        distFunction = function;
        lowerBound = lower;
        upperBound = upper;
    }

    /**
     * Creates the default reporter for the CustomContDist distribution.
     *
     * @return Reporter : The reporter for the CustomContDist distribution
     * @see desmoj.core.report.ContDistCustomReporter
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.ContDistCustomReporter(this);

    }

    /**
     * Returns the upper bound of possible values of this distribution.
     *
     * @return double : The upper bound of possible values of this distribution.
     */
    public Function getFunction() {

        return distFunction;
    }

    /**
     * Abstract method to map a double <code>p</code> from 0...1 to the distribution's domain by determining the value x
     * that satisfies
     * <code>P(X &lt; x) = p</code>.
     *
     * @param p double: A value between 0 and 1
     * @return N : The value x that satisfies <code>P(X &lt; x) = p</code>
     */
    @Override
    public Double getInverseOfCumulativeProbabilityFunction(final double p) {

        double newSample;

        Function functiontosolve = new Function() {

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public double value(double x) {

                return distFunction.value(x) - p;
                // Decrease input function by randomNumber to make the desired
                // sample be at a zero root.
            }
        };

        //        var solver = new BisectionSolver(functiontosolve);
        var solver = new BisectionSolver();
        try {
            newSample = solver.solve(Integer.MAX_VALUE, functiontosolve, lowerBound, upperBound); // Finding zero
            // root

        } catch (Exception e) {
            sendWarning("Failed to find sample, returning -1",
                        "CustomContDist : " + getName() + " Method: getInverseOfCumulativeProbability()",
                        "The solver could not deal with the distribution function specified.",
                        "Make sure the CustomFunction this Distribution is using is a proper "
                        + "distribution function and the upper and lower bounds are set accordingly");
            newSample = StatisticObject.UNDEFINED;
        }

        return newSample;
    }

    /**
     * Returns the lower bound of possible values of this distribution.
     *
     * @return double : The lower bound of possible values of this distribution.
     */
    public double getLowerBound() {

        return lowerBound;
    }

    /**
     * Returns the upper bound of possible values of this distribution.
     *
     * @return double : The upper bound of possible values of this distribution.
     */
    public double getUpperBound() {

        return upperBound;
    }

    /**
     * Returns the next sample from this distribution. The value depends upon the seed, the number of values taken from
     * the stream by using this method before and the alpha and beta parameters specified for this distribution.
     *
     * @return Double : The next gamma distributed sample from this distribution.
     */
    @Override
    public Double sample() {

        return super.sample();
    }
}
