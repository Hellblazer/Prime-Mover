package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Weibull-distributed stream of pseudo random numbers of type double.
 *
 * @author Tim Lechler, Johannes G&ouml;bel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public class ContDistWeibull extends ContDistExponential {

    /**
     * The beat value of the Weibull distribution.
     */
    protected double beta;

    /**
     * Constructs a simple Weibull-distributed pseudo random generator with the given values as mean and beta of the
     * distribution. Only positive values are allowed.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param mean         double : The mean value for this distribution
     * @param beta         double : The beat value for this distribution
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public ContDistWeibull(Model owner, String name, double mean, double beta, boolean showInReport,
                           boolean showInTrace) {
        super(owner, name, mean, showInReport, showInTrace);
        this.beta = beta;
    }

    /**
     * Returns the beta value of the Weibull distribution.
     *
     * @return double : the mean value of the Weibull distribution
     */
    public double getBeta() {

        return beta;

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
    public Double getInverseOfCumulativeProbabilityFunction(double p) {

        return Math.pow(super.getInverseOfCumulativeProbabilityFunction(p), 1 / beta);
    }

    /**
     * Returns the next pseudo random number of the Weibull distribution.
     *
     * @return Double : The next negative exponential pseudo random number
     */
    @Override
    public Double sample() {

        return super.sample();
    }
}
