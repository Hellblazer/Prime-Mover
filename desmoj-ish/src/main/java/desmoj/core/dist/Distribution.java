package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Base class for all pseudo random number distributions used in this package.
 * Defines a set of methods usefull for all kinds of random distributions that
 * can be based upon a stream of uniform distributed pseudo random numbers.
 * Prefabricated distributions implemented in this package can handle uniform,
 * normal (gaussian), bernoulli, poisson and heuristic distributions with return
 * values of the primitive data types double (floating point), long (integer)
 * and boolean (true or false). Inherit from this class if you want to implement
 * new types of distributions handing back values of other types than those
 * listed above. Basic idea is to use a pseudo random generator which produces a
 * uniformly distributed stream of double numbers between 0 and 1 use inverse
 * transformation to generate the desired distribution. See also [Page91, p.
 * 107] Note that although this class implements all methods, it is set to be
 * abstract, since instantiating this class would not produce any meaningfull
 * distribution to be used by a client.
 * 
 * @see desmoj.core.dist.UniformRandomGenerator
 * @see desmoj.core.dist.LinearCongruentialRandomGenerator
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
public abstract class Distribution extends desmoj.core.simulator.Reportable {

	/**
	 * The status of the random number generation. If set to true, antithetic
	 * values are delivered. These depend upon the kind of distribution, so this
	 * value here will probably be most useful to switch the algorithm in the
	 * implementation of the abstract <code>sample()</code> method between
	 * "normal" and "antithetic" value generation. This feature is not
	 * associated to the pseudo random generator since the algorithm for
	 * calculating antithetic values might not require antithetic uniformly
	 * distributed values.
	 */
	protected boolean antithetic;

	/**
	 * The seed of the underlying pseudorandom generator. The seed value is
	 * passed on to the underlying <code>UniformRandomGenerator</code> but since
	 * those generators are not supposed to keep track of their initial seed
	 * value it is stored here to make sure they are not lost.
	 */
	protected long initialSeed;

	/**
	 * This flag shows, if a distribution may produce negative samples or not.
	 * This is important, if the value of a distribution's sample is to be used
	 * for creating a TimeSpan object, which allows positive values only. If
	 * this switch is set to <code>true</code>, the distribution will only
	 * return positive samples. If a negative sample is drawn, it will be
	 * dismissed and new samples will be drawn until a positive is produced,
	 * which will be returned.
	 */
	protected boolean nonNegative;

	/**
	 * The underlying uniform pseudo random generator available to every
	 * distribution inheriting from this abstract class. Valid generators have
	 * to implement the <code>desmoj.dist.UniformRandomGenerator</code>
	 * interface. By default the <code>desmoj.dist.DefaultRandomGenerator</code>
	 * is used.
	 * 
	 * @see desmoj.core.dist.UniformRandomGenerator
	 * @see desmoj.core.dist.LinearCongruentialRandomGenerator
	 */
	protected desmoj.core.dist.UniformRandomGenerator randomGenerator;

	/**
	 * Creates a RandomDistribution object which gets its initial seed from the
	 * experiment's seedgenerator. The
	 * <code>LinearCongruentialRandomGenerator</code> is used as the underlying
	 * uniform pseudo random number generator for all pseudo random distribution
	 * .
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param showInReport
	 *            boolean : Flag to show distribution in report
	 * @param showInTrace
	 *            boolean : Flag to show distribution in trace
	 */
	public Distribution(Model owner, String name, boolean showInReport,
			boolean showInTrace) {

		super(owner, name, showInReport, showInTrace); // construct the
														// reportable
		if (randomGenerator == null) {
			try {
				randomGenerator = owner.getExperiment()
						.getDistributionManager().getRandomNumberGenerator()
						.newInstance(); // default RandomGenerator
			} catch (InstantiationException e) {
				randomGenerator = new LinearCongruentialRandomGenerator();
			} catch (IllegalAccessException e) {
				randomGenerator = new LinearCongruentialRandomGenerator();
			}
		}
		owner.getExperiment().getDistributionManager().register(this);

		// set seed in case experiment running
		// (for not yet running experiments, this happens automatically
		// when the experiment is started)
		if (owner.getExperiment().isRunning()) {
			randomGenerator.setSeed(initialSeed);
		}
	}

	/**
	 * Changes the underlying random generator to the one given as a parameter.
	 * Custom random generators have to implement the
	 * desmoj.dist.UniormRandomGenerator interface. Note that changing the
	 * underlying random generator forces a reset, since a new generator might
	 * produce a completely different stream of pseudo random numbers that won't
	 * enable us to reproduce the stream of numbers probably delivered by the
	 * previously used generator.
	 * 
	 * @param randomGenerator
	 *            java.util.Random : the random generator used for creating
	 *            distributions
	 */
	public void changeRandomGenerator(
			desmoj.core.dist.UniformRandomGenerator randomGenerator) {

		this.randomGenerator = randomGenerator;
		reset();

	}

	/**
	 * Creates the default reporter associated with this distribution. The basic
	 * <code>DistributionReporter</code> returned as a default implementation of
	 * this method simply reports the distribution's name, number of
	 * observations (samples given), seed and point of simulation time of the
	 * last reset.
	 * 
	 * @return Reportable : The reporter associated with this distribution
	 * @see desmoj.core.report.DistributionReporter
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {

		return new desmoj.core.report.DistributionReporter(this);

	}

	/**
	 * Returns the seed value since last reset.
	 * 
	 * @return long : The initial seed value
	 */
	public long getInitialSeed() {

		return initialSeed;

	}

	/**
	 * Tells if this distribution can return negative samples.
	 * 
	 * @return boolean : If <code>true</code> it returns positive samples only
	 */
	public boolean getNonNegative() {

		return nonNegative;

	}

	/**
	 * Returns the number of Samples given by this distribution. The number of
	 * samples is increased whenever the sample() method is called. It is based
	 * on the random numbers of the distribution, not on the number of random
	 * numbers produced by the underlying random generator, since some
	 * distributions use algorithms consuming more than one uniformly
	 * distributed random number to produce one sample following the desired
	 * distribution.
	 * 
	 * @return long : the number of samples given to clients
	 */
	public long getNumSamples() {

		return getObservations();

	}

	/**
	 * Returns the current status for antithetic random number generation in
	 * this distribution.
	 * 
	 * @return boolean : The status of antithetic pseudo random number
	 *         generation
	 * @see desmoj.core.dist.Distribution#setAntithetic
	 */
	public boolean isAntithetic() {

		return antithetic;

	}

	/**
	 * Resets the pseudo random generator's seed and the number of samples given
	 * to zero. The field antithetic keeps the value it has had before the
	 * reset.
	 */
	public void reset() {

		if (randomGenerator == null) {
			try {
				randomGenerator = this.getModel().getExperiment()
						.getDistributionManager().getRandomNumberGenerator()
						.newInstance(); // default RandomGenerator
			} catch (InstantiationException e) {
				randomGenerator = new LinearCongruentialRandomGenerator();
			} catch (IllegalAccessException e) {
				randomGenerator = new LinearCongruentialRandomGenerator();
			}
		}

		// sets seed to the seed specified in constructor or by call to
		// setSeed(long)
		randomGenerator.setSeed(initialSeed); // initialSeed stays unchanged
		// here

		// antithetic = false;
		/*
		 * no need to change this to false. If this distribution has delivered
		 * antithetic random number than it will do so after the reset, too.
		 */

		super.reset(); // reset the Reportable, too.
	}

	/**
	 * Resets the pseudo random generator's seed to the value passed, the number
	 * of samples given to zero and sets antithetic to false for this
	 * distribution. Acts the same as a call of method <code>reset()</code> and
	 * a consecutive call to <code>setSeed(long)</code>.
	 * 
	 * @param newSeed
	 *            long : new seed to be used by underlying random number
	 *            generator after reset
	 */
	public void reset(long newSeed) {

		randomGenerator.setSeed(newSeed);

		this.initialSeed = newSeed; // initialSeed is changed here

		// antithetic = false;
		/*
		 * no need to change this to false. If this distribution has delivered
		 * antithetic random number than it will do so after the reset, too.
		 */

		super.reset(); // reset the Reportable, too.

	}
	
    /**
     * Convenience method to return the distribution's sample as <code>Object</code>.
     * For type safety, method <code>sample()</code> should be preferred. However, 
     * this method is useful for environments requiring a non-genetic access point 
     * to obtain samples from any distribution. 
     * 
     * @return Object : A sample from this this distribution wrapped as <code>Object</code>.
     */
    public abstract Object sampleObject();

	/**
	 * Switches this distribution to produce antithetic samples. To obtain
	 * antithetic random numbers, call this method with the parameter
	 * <code>true</code>. Antithetic random numbers are used to minimize the
	 * standard deviation of a series of simulation runs. The results of a run
	 * with normal random numbers has to be standardized with the results of a
	 * run using antithetic random numbers, thus doubling the number of samples
	 * needed, but also lowering the standard deviation of the results of that
	 * simulation. See [Page91, p.139].
	 * 
	 * @param newAntiStatus
	 *            boolean : Parameter <code>true</code> switches antithetic mode
	 *            on, <code>false</code> switches antithetic mode off
	 */
	public void setAntithetic(boolean newAntiStatus) {

		antithetic = newAntiStatus;
		reset();

	}

	/**
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
		this.nonNegative = newValue;
	}

	/**
	 * Sets the underlying pseudo random number generator's seed to the value
	 * given. The seed controls the starting value of the random generators and
	 * all following generated pseudo random numbers. Resetting the seed between
	 * two simulation runs will let you use identical streams of random numbers.
	 * That will enable you to compare different strategies within your model
	 * based on the same random number stream produced by the random generator.
	 * 
	 * @param newSeed
	 *            long : new seed used by underlying pseudo random number
	 *            generator
	 */
	public void setSeed(long newSeed) {

		randomGenerator.setSeed(newSeed); // well, the seed is passed on...
		// ;-)
		initialSeed = newSeed; // remember new seed for next reset()
		reset(); // and do a reset of statistics to display when a new seed
		// was
		// set

	}

	/**
	 * Generates the trace output of each sample. This method is called by
	 * sample().
	 * 
	 * @param sample
	 *            String : The last sample, converted to a String
	 */
	protected void traceLastSample(String sample) {

		if (this.currentlySendTraceNotes())
			this.sendTraceNote("samples " + sample + " from " + this.getName());

	}
}