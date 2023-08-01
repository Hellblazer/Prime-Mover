package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Uniformly distributed stream of pseudo random numbers of type long. Values
 * produced by this distribution are uniformly distributed in the range
 * specified as parameters of the constructor.
 * 
 * @see desmoj.core.dist.Distribution
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * @author modified by Philip Joschko
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
public class DiscreteDistUniform extends DiscreteDist<Long> {

	/**
	 * Stores the upper border of the distribution range
	 */
	private long _max;

	/**
	 * Stores the lower border of the distribution range
	 */
	private long _min;

	/**
	 * Creates a stream of pseudo random numbers following a uniform
	 * distribution. The specific lower and upper borders of the range of this
	 * distribution have to be given here at creation time. Note that the lower
	 * border in fact has to be lower than the upper border. If not, the
	 * parameters will be swapped to assign the higher value to the upper border
	 * of the distribution. Two equal values for upper and lower will be
	 * accepted, but result in a constant distribution that will invariabbly
	 * return that given value. In both cases a warning mesage will be produced.<br/>
	 * Note that the range, i.e. the difference between the upper and lower border,
	 * must be less than <code>Long.MAX_VALUE</code> for arithmetic reasons.  
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param minValue
	 *            long : The minimum value for this distribution
	 * @param maxValue
	 *            long : The maximum value for this distribution
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public DiscreteDistUniform(Model owner, String name, long minValue,
			long maxValue, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);

		if (minValue <= maxValue) {
			_min = minValue;
			_max = maxValue;
		} else {
			_min = maxValue;
			_max = minValue;
		}
	}

	/**
	 * Creates the default reporter for the UniformDiscreteDist distribution.
	 * 
	 * @return Reporter : The reporter for the UniformDiscreteDist distribution
	 * @see desmoj.core.report.DiscreteDistUniformReporter
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {

		return new desmoj.core.report.DiscreteDistUniformReporter(this);

	}

	/**
     * Abstract method to map a double <code>p</code> from 0...1 to the 
     * distribution's domain by determining the value x that satisfies
     * <code>P(X &lt; x) = p</code>.
     * 
     * @param p double: A value between 0 and 1
     * 
     * @return Long : The value x that satisfies <code>P(X &lt; x) = p</code>
     */
    public Long getInverseOfCumulativeProbabilityFunction(double p) {

		long newSample = (long) Math.floor(_min + ((_max - _min + 1) * p));
		
		if (newSample == (_max + 1)) // marginal case for p = 1.00000000
		    newSample = _max; 

		return newSample;
	}

	/**
	 * Returns the lower border of the range of this distribution.
	 * 
	 * @return long : The lower border of the random number's range
	 */
	public long getLower() {

		return _min;

	}

	/**
	 * Returns the upper border of the range of this distribution.
	 * 
	 * @return long : The upper border of the random number's range
	 */
	public long getUpper() {

		return _max;

	}
        
    /**
	 * Returns the next uniformly distributed integer sample. The value returned
	 * is basically the uniformly distributed pseudo random number produced by
	 * the underlying random generator stretched to match the range specified by
	 * the client via constructor parameters.
	 * 
	 * @return Long : The next uniformly distributed sample
	 */
    public Long sample() {

        return super.sample();
    }

	/**
	 * Overrides the same method of <code>desmoj.dist.Distribution</code>. It
	 * makes no sense to set a <code>UniformDiscreteDist</code> object with a
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
					"Attempt to set a UniformDiscreteDist distribution with a negative "
							+ "lowerBorder to nonNegative. This will be done, but doesn't make sense!",
					"DiscreteDistUniform: " + this.getName()
							+ " Method: public void "
							+ "setNonNegative(boolean newValue)",
					"The given distribution has a negative border but all negative values "
							+ "will be ignored.",
					"Make sure not to set a UniformDiscreteDist distribution with a negative "
							+ "lowerBorder to nonNegative.");
		}

		if (newValue == true && _max < 0) {
			sendWarning(
					"Attempt to set a UniformDiscreteDist distribution with a negative "
							+ "upperBorder to nonNegative. The command will be ignored!",
					"DiscreteDistUniform: " + this.getName()
							+ " Method: public void "
							+ "setNonNegative(boolean newValue)",
					"The given distribution has a negative upper border. When all negative "
							+ "values will be ignored, one won't get any values.",
					"Make sure not to set a UniformDiscreteDist distribution with a negative "
							+ "upperBorder to nonNegative.");

			return; // just do nothing but return
		}

		this.nonNegative = newValue;
	}
}