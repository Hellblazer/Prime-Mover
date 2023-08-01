package desmoj.core.dist;

import java.util.Random;

/**
 * Linear congruential random generator for uniformly distributed pseudo random
 * numbers configured such that a stream of [0,1] double values is produced.
 * Implements the <code>desmoj.dist.UniformRandomGenerator</code> interface. All
 * <code>Distributions</code> in this package use this random generator by
 * default. The implementation is based on the Java API
 * <code>java.util.Random</code> class' random generator. The Java API Random
 * class uses a 48-bit seed as input to the linear congruential formula. (See
 * Donald Knuth, The Art of Computer Programming, Volume 2, Section 3.2.1.)
 * 
 * @see desmoj.core.dist.UniformRandomGenerator
 * @see desmoj.core.dist.Distribution
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
public class LinearCongruentialRandomGenerator implements
		desmoj.core.dist.UniformRandomGenerator {

	/**
	 * The random generator provided by the Java API class
	 * <code>java.util.Random</code>
	 */
	protected Random javaAPIRandomGenerator; // the random generator used

	/**
	 * Creates a DefaultrandomGenerator with seed 42. The value 42 has been
	 * taken by pure coincidence. It is not really related to Douglas Adams'
	 * "Hitchhikers Guide to the Galaxy" trilogy of five books (so far).
	 */
	public LinearCongruentialRandomGenerator() {

		javaAPIRandomGenerator = new Random(42);

	}

	/**
	 * Creates a DefaultrandomGenerator with given value as initial seed.
	 * 
	 * @param seed
	 *            long : The initial seed of the underlying pseudo random
	 *            generator
	 */
	public LinearCongruentialRandomGenerator(long seed) {
		javaAPIRandomGenerator = new Random(seed);
	}

	/**
	 * Returns the next pseudo random uniform [0,1] distributed double value
	 * from the stream produced by the underlying pseudo random number
	 * generator.
	 * 
	 * @return double : The next pseudo random uniform [0,1] distributed double
	 *         value
	 */
	public double nextDouble() {

		return javaAPIRandomGenerator.nextDouble();

	}

	/**
	 * Sets the seed for the pseudo random number generator.
	 * 
	 * @param newSeed
	 *            long : The new initial seed value for the pseudo random number
	 *            generator
	 */
	public void setSeed(long newSeed) {

		javaAPIRandomGenerator.setSeed(newSeed);

	}
}