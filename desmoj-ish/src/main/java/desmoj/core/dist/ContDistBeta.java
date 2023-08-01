package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Distribution returning Beta distributed double values.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Peter Wueppen
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

public class ContDistBeta extends ContDist {

    /**
     * Distribution specific parameter alpha
     */
    protected double alpha;

    /**
     * Distribution specific parameter beta
     */
    protected double beta;

    /**
     * Creates a stream of pseudo random numbers following a Beta distribution. The
     * specific parameters alpha and beta have to be given here at creation time.
     * 
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param alpha        double: Distribution specific parameter alpha
     * @param beta         double : Distribution specific parameter beta
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public ContDistBeta(Model owner, String name, double alpha, double beta, boolean showInReport,
                        boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        this.alpha = alpha;
        this.beta = beta;
        // TODO Auto-generated constructor stub
    }

    /**
     * Creates the default reporter for the BetaDist distribution.
     * 
     * @return Reporter : The reporter for the BetaDist distribution
     * @see desmoj.core.report.ContDistBetaReporter
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.ContDistBetaReporter(this);

    }

    /**
     * 
     * @return double : Distribution specific shape parameter alpha
     */
    public double getAlpha() {

        return alpha;
    }

    /**
     * 
     * @return double : Distribution specific scale parameter beta
     */
    public double getBeta() {

        return beta;

    }

    /**
     * Method to map a double <code>p</code> from 0...1 to the distribution's domain
     * by determining the value x that satisfies <code>P(X &lt; x) = p</code>. Not
     * supported, i.e. throwing an <code>UnsupportedOperationException</code>.
     * 
     * @param p double: A value between 0 and 1
     * 
     * @return An <code>UnsupportedOperationException</code> exception
     */
    @Override
    public Double getInverseOfCumulativeProbabilityFunction(double p) {
        throw new UnsupportedOperationException(this.getName()
        + " does not support determined inverse cumulative probabilities.");
    }

    /**
     * Returns the next sample from this distribution. The value depends upon the
     * seed, the number of values taken from the stream by using this method before
     * and the alpha and beta parameters specified for this distribution.
     * 
     * @return Double : The next Beta distributed sample from this distribution.
     */
    @Override
    public Double sample() {

        double newSample = -1; //
        double randomNumber1 = randomGenerator.nextDouble();
        double randomNumber2 = randomGenerator.nextDouble();
        var gammadist1 = new org.apache.commons.math3.distribution.GammaDistribution(alpha, 1);
        var gammadist2 = new org.apache.commons.math3.distribution.GammaDistribution(beta, 1);
        incrementObservations(); // increase count of samples

        if (isAntithetic()) {
            double gammaval1 = gammadist1.inverseCumulativeProbability(1 - randomNumber1);
            double gammaval2 = gammadist2.inverseCumulativeProbability(1 - randomNumber2);
            newSample = gammaval1 / (gammaval1 + gammaval2);
        } else {
            double gammaval1 = gammadist1.inverseCumulativeProbability(randomNumber1);
            double gammaval2 = gammadist2.inverseCumulativeProbability(randomNumber2);
            newSample = gammaval1 / (gammaval1 + gammaval2);
        }

        if (this.currentlySendTraceNotes())
            this.traceLastSample(Double.toString(newSample));

        return newSample;
    }
}
