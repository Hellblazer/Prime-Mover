package desmoj.core.dist;

import desmoj.core.simulator.NamedObject;

/**
 * Controls all distributions used during an experiment. Provides the service of automatic seed generation for all
 * distributions registered at the distributionmanager. Note that all distributions register at instantiation time at
 * the experiment's distributionmanager automatically.
 *
 * @author Tim Lechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public class DistributionManager extends NamedObject {

    /**
     * Value sets antithetic mode for all distributions registering at the distributionmanager.
     */
    private boolean _antitheticMode;

    /**
     * The current default random number generator to be used by newly created distributions.
     */
    private Class<? extends UniformRandomGenerator> _currentDefaultGenerator;

    /**
     * Keeps references to all distributions of this experiment
     */
    private java.util.ArrayList<Distribution> _distributions;

    /**
     * The seed of the internal seed-generator
     */
    private long _seed;

    /**
     * Produces all starting seeds for registered distributions.
     */
    private UniformRandomGenerator _seedGenerator;

    /**
     * Creates a new distributionManager with the given name and the given initial seed for the seed-generator.
     *
     * @param name java.lang.String : The distributionmanager's name
     * @param seed long : The initial seed for the seedgenerator
     */
    public DistributionManager(String name, long seed) {

        super(name + "_DistributionManager"); // create the NamedObject

        _antitheticMode = false; // set antithetic mode to false by default

        _seedGenerator = new MersenneTwisterRandomGenerator(seed); // create seed generator
        _currentDefaultGenerator = MersenneTwisterRandomGenerator.class;

        // _seedGenerator = new LinearCongruentialRandomGenerator(seed); // create seed
        // generator
        // _currentDefaultGenerator = LinearCongruentialRandomGenerator.class;

        _distributions = new java.util.ArrayList<>(); // init List
        // for dist

        _seed = seed;
    }

    /**
     * De-registers a distribution from the experiment.
     *
     * @param dist desmoj.dist.Distribution : The distribution to be deregistered
     */
    public void deRegister(Distribution dist) {

        _distributions.remove(dist); // remove from List

    }

    /**
     * Returns a list containing all distributions.
     */
    public java.util.List<Distribution> getDistributions() {
        return new java.util.ArrayList<>(this._distributions);
    }

    /**
     * Returns the initial seed.
     *
     * @return long : the initial seed
     */
    public long getSeed() {
        return _seed;
    }

    /**
     * Provides all registered distributions with new seed values, thus resetting all distribution statistics at the
     * same time.
     */
    public void newSeedAll() {

        for (Distribution d : _distributions) {
            d.setSeed(nextSeed());
        }
    }

    /**
     * Returns a new seed value to be used as an initial seed for registered distributions.
     *
     * @return long : A new seed value for a registered distribution
     */
    public long nextSeed() {

        // get a positive seed value
        return (long) (_seedGenerator.nextDouble() * 100000000);

    }

    /**
     * Registers a new distribution at the experiment to control antithetic mode and set random seed values.
     *
     * @param dist desmoj.dist.Distribution : The distribution to be registered
     */
    public void register(Distribution dist) {

        dist.setAntithetic(_antitheticMode); // set antithetic mode to default
        dist.setSeed(nextSeed()); // set new seed
        _distributions.add(dist); // add to Vector

    }

    /**
     * Resets all registered distributions. Just calls all distribution's individual reset method.
     */
    public void resetAll() {

        for (Distribution d : _distributions) {
            d.reset();
        }
    }

    /**
     * Sets antithetic mode to true on all registered distributions regardless of their previous status. No reset of
     * statistical counters.
     *
     * @param antitheticMode boolean : The new status of antithetic mode
     */
    public void setAntitheticAll(boolean antitheticMode) {

        for (Distribution d : _distributions) {
            d.setAntithetic(antitheticMode);
        }
    }

    /**
     * Sets the underlying pseudo random number generator to be used by all distributions created from now on. The
     * default generator is LinearCongruentialRandomGenerator; any other generator to be used must implement the
     * interface UniformRandomGenerator.
     *
     * @param randomNumberGenerator Class : The random number generator class to be used
     * @see desmoj.core.dist.LinearCongruentialRandomGenerator
     * @see desmoj.core.dist.UniformRandomGenerator
     */
    public void setRandomNumberGenerator(Class<? extends UniformRandomGenerator> randomNumberGenerator) {

        this._currentDefaultGenerator = randomNumberGenerator;

    }

    /**
     * Sets the seed of the SeedGenerator to the given value. If the seed is not set here, its default is zero, unless
     * specified in the experimentoptions.
     *
     * @param newSeed long : The new seed for the seedgenerator
     */
    public void setSeed(long newSeed) {

        _seed = newSeed;

        _seedGenerator.setSeed(newSeed); // go ahead and set it!

    }

    /**
     * Returns the underlying pseudo random number generator to be used by all distributions. This method is intended
     * for internal use (i.e. called by Distribution) only.
     *
     * @see desmoj.core.dist.LinearCongruentialRandomGenerator
     * @see desmoj.core.dist.MersenneTwisterRandomGenerator
     * @see desmoj.core.dist.UniformRandomGenerator
     */
    protected Class<? extends UniformRandomGenerator> getRandomNumberGenerator() {

        return this._currentDefaultGenerator;

    }
}
