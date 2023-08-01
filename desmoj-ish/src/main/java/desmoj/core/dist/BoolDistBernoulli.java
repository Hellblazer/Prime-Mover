package desmoj.core.dist;

import desmoj.core.report.BoolDistBernoulliReporter;
import desmoj.core.simulator.Model;

/**
 * Boolean Bernoulli distribution returning <code>true</code> values with the
 * given probability. Samples of this distribution can either be true or false
 * with a given probability for value "true". The probabilitiy for "true" can
 * only be set via the constructor. It has to be a value between 0 and 1. Higher
 * values will be interpreted as 1 (always return "true"). Negative values will
 * be interpreted as 0 (always return "false").
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
public class BoolDistBernoulli extends BoolDist {

	/**
	 * Stores the probability of returning "true" as a double for maximum
	 * precision.
	 */
	protected double trueProbability;

	/**
	 * Constructs a boolean Bernoulli distribution with the given probability to
	 * return a "true" value. The given probability has to be in the range
	 * between 0 and 1. Higher values will be interpreted as 1 (always return
	 * "true"). Negative values are interpreted as 0 (always return "false").
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param probabilityForTrue
	 *            double : The probability for producing a <code>true</code>
	 *            sample
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public BoolDistBernoulli(Model owner, String name,
			double probabilityForTrue, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
		trueProbability = probabilityForTrue;
	}

	/**
	 * Creates the default reporter for the <code>BoolDistBernoulli</code>
	 * distribution.
	 * 
	 * @return Reporter : The reporter for the <code>BoolDistBernoulli</code>
	 *         distribution
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {

		return new BoolDistBernoulliReporter(this);

	}

	/**
	 * Returns the probability for <code>true</code> values. The value returned
	 * is passed through the constructor.
	 * 
	 * @return double : the probability of a <code>true</code> value being
	 *         returned
	 */
	public double getProbability() {

		return trueProbability;

	}

	/**
	 * Returns the next Bernoulli distributed sample of the distribution. The
	 * returned value will depend upon the seed of the underlying random
	 * generator and the probability given for this distribution.
	 * 
	 * @return boolean : The next Bernoulli distributed random sample
	 */
	public boolean sample() {

		incrementObservations(); // increase count of samples

		// direct mapping between probability [0,1] and sample from
		// randomgenerator [0,1]
		// probability indicates level when to return "true".
		boolean newSample = randomGenerator.nextDouble() < trueProbability;
		if (antithetic)
			newSample = !newSample;

		if (this.currentlySendTraceNotes())
			this.traceLastSample(Boolean.toString(newSample));

		return newSample;

	}
}