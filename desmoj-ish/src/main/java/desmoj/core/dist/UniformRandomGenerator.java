package desmoj.core.dist;

/**
 * Interface for uniform pseudo random number generators to be used by the
 * distribution classes to compute their samples. Random generators of this type
 * must return uniform distributed double values. Note that two constructors
 * have to be implemented. One constructor without parameters to construct a
 * UniformRandomGenerator with seed zero, and another constructor with a
 * parameter of type long to pass the initial seed value to the pseudo random
 * generation algorithm.
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
public interface UniformRandomGenerator {
	/**
	 * Returns the next uniform distributed [0,1] double pseudo random number
	 * from the random generator's stream.
	 * 
	 * @return double : The next uniformly distributed [0,1] double value
	 */
	public double nextDouble();

	/**
	 * Set the seed to be used by the pseudo random number generating algorithm.
	 * Setting the seed to a specific value forces the random generator to
	 * produce identical streams of pseudo random numbers. This is necessary to
	 * ensure that experiments in simulation can be made under identical
	 * conditions, thus enabling modelers to compare results of experiments.
	 * 
	 * @param seed
	 *            long : The seed to be used for creating the folowing stream of
	 *            pseudo random numbers
	 */
	public void setSeed(long seed);
}