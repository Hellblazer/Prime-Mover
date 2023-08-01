package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Uniformly distributed stream of pseudo random numbers of type double. Values
 * produced by this distribution are uniformly distributed in the range
 * specified as parameters of the constructor.
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
public class ContDistUniform extends ContDist {

	/**
	 * Stores the upper border of the distribution range
	 */
	private double _max;

	/**
	 * Stores the lower border of the distribution range
	 */
	private double _min;

	/**
	 * Creates a stream of pseudo random numbers following a uniform
	 * distribution between the lower and the upper value parameter. The
	 * specific lower and upper borders of the range of this distribution have
	 * to be given here at creation time. Note that the lower border in fact has
	 * to be lower than the upper border. If not, the parameters will be swapped
	 * to assign the higher value to the upper border of the distribution. Two
	 * equal values for upper and lower will be accepted, but result in a
	 * constant distribution that will invariably return that given value.
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param lowerBorder
	 *            double : The minimum value for this distribution
	 * @param upperBorder
	 *            double : The maximum value for this distribution
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public ContDistUniform(Model owner, String name, double lowerBorder,
			double upperBorder, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);

		if (lowerBorder <= upperBorder) {
			_min = lowerBorder;
			_max = upperBorder;
		} else {
			_min = upperBorder;
			_max = lowerBorder;
		}
	}

	/**
	 * Creates the default reporter for the RealDistUniform distribution.
	 * 
	 * @return Reporter : The reporter for the RealDistUniform distribution
	 * @see desmoj.core.report.ContDistUniformReporter
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {

		return new desmoj.core.report.ContDistUniformReporter(this);

	}

	/**
     * Abstract method to map a double <code>p</code> from 0...1 to the 
     * distribution's domain by determining the value x that satisfies
     * <code>P(X &lt; x) = p</code>.
     * 
     * @param p double: A value between 0 and 1
     * 
     * @return N : The value x that satisfies P(X &lt; x) = p
     */
    public Double getInverseOfCumulativeProbabilityFunction(double p) {

		return _min + ((_max - _min) * p);
	}

	/**
	 * Returns the lower border of the range of this uniform distribution.
	 * 
	 * @return double : The lower border of the range of this uniform
	 *         distribution
	 */
	public double getLower() {

		return _min;

	}

	/**
	 * Returns the upper border of the range of this distribution.
	 * 
	 * @return double : The upper border of the range of this uniform
	 *         distribution
	 */
	public double getUpper() {

		return _max;

	}
        
    /**
	 * Returns the next floating point sample from this uniform distribution.
	 * The value returned is basically the uniformly distributed pseudo random
	 * number produced by the underlying random generator stretched to match the
	 * range specified by the client via construtor parameters.
	 * 
	 * @return Double : The next floating point sample from this uniform
	 *         distribution
	 */
    public Double sample() {

        return super.sample();
    }

	/**
	 * Overrides the same method of <code>desmoj.dist.Distribution</code>. It
	 * makes no sense to set a <code>RealDistUniform</code> object with a
	 * negative lower border to nonNegative. In this case a warning will be
	 * sent. <br>
	 * Sets the nonNegative switch to the given value. If nonNegative is set to
	 * <code>true</code> the distribution returns positive samples only,
	 * otherwise it also produces negative samples, if possible.
	 * 
	 * @param newValue
	 *            boolean : If <code>true</code> the distribution is set to
	 *            return positive samples only, otherwise it also produces
	 *            negative samples, if possible.
	 */
	public void setNonNegative(boolean newValue) {
		if (newValue == true && _min < 0) {
			sendWarning(
					"Attempt to set a RealDistUniform distribution with a negative "
							+ "lowerBorder to nonNegative. This will be done, but doesn't make sense!",
					"RealDistUniform: " + this.getName()
							+ " Method: public void "
							+ "setNonNegative(boolean newValue)",
					"The given distribution has a negative border but all negative values "
							+ "will be ignored.",
					"Make sure not to set a RealDistUniform distribution with a negative "
							+ "lowerBorder to nonNegative.");
		}

		if (newValue == true && _max < 0) {
			sendWarning(
					"Attempt to set a RealDistUniform distribution with a negative "
							+ "upperBorder to nonNegative. The command will be ignored!",
					"RealDistUniform: " + this.getName()
							+ " Method: public void "
							+ "setNonNegative(boolean newValue)",
					"The given distribution has a negative upper border. When all negative "
							+ "values will be ignored, one won't get any values.",
					"Make sure not to set a RealDistUniform distribution with a negative "
							+ "upperBorder to nonNegative.");

			return; // just do nothing but return
		}

		this.nonNegative = newValue;
	}
}