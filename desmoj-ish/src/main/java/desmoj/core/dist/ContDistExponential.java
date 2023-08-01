package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Negative-exponentially distributed stream of pseudo random numbers of type
 * double.
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
public class ContDistExponential extends ContDist {

	/**
	 * The mean value of the negative-exponential distribution.
	 */
	protected double mean;

	/**
	 * Constructs a simple negative-exponentially distributed pseudo random
	 * generator with the given value as mean of the distribution. Only positive
	 * values are allowed.
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param mean
	 *            double : The mean value for this distribution
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public ContDistExponential(Model owner, String name, double mean,
			boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
		this.mean = mean;
	}

	/**
	 * Creates the default reporter for the RealDistExponential distribution.
	 * 
	 * @return Reporter : The reporter for the RealDistExponential distribution
	 * @see desmoj.core.report.ContDistExponReporter
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {

		return new desmoj.core.report.ContDistExponReporter(this);

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
    public Double getInverseOfCumulativeProbabilityFunction(double p) {

        return -java.lang.Math.log(p) * mean;
	}

	/**
	 * Returns the mean value of the negative-exponential distribution.
	 * 
	 * @return double : the mean value of the negative-exponential distribution
	 */
	public double getMean() {

		return mean; // a mean machine ;-)

	}
        
    /**
	 * Returns the next negative exponential pseudo random number. 
	 * 
	 * @return Double : The next negative exponential pseudo random number
	 */
    public Double sample() {

        return super.sample();
    }
}