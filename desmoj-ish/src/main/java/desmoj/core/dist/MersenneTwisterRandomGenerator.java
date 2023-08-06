package desmoj.core.dist;

import java.util.Random;

/**
 * Mersenne twister uniform pseudo random generator configured such that a
 * stream of [0,1] double values is produced. Implements the
 * <code>desmoj.dist.UniformRandomGenerator</code> interface. The algorithm for
 * pseudo random number generation is based on the Mersenne twister variant
 * MT19937 and has a period of 2<sup>19937</sup> - 1; see
 * <a href="http://en.wikipedia.org/wiki/Mersenne_twister">the Mersenne twister
 * wikipedia entry</a> for details.
 *
 * @see desmoj.core.dist.UniformRandomGenerator
 * @see desmoj.core.dist.Distribution
 * @see java.util.Random
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, Johannes G&ouml;bel
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
public class MersenneTwisterRandomGenerator implements desmoj.core.dist.UniformRandomGenerator {

    /**
     * A pointer to indicate which number from the repository to read next
     */
    protected int currentIndex;

    /**
     * The random generator provided by the Java API class
     * <code>java.util.Random</code>
     */
    protected Random javaAPIRandomGenerator; // the random generator used

    /**
     * The repository for storing random numbers (which also denote the state of the
     * generator)
     */
    protected int[] mersenneTwister;

    /**
     * Creates a MersenneTwisterRandomGenerator with seed 42. The value 42 has been
     * taken by pure coincidence. It is not really related to Douglas Adams'
     * "Hitchhikers Guide to the Galaxy" trilogy of five books (so far).
     */
    public MersenneTwisterRandomGenerator() {
        this(42);
    }

    /**
     * Creates a MersenneTwisterRandomGenerator with given value as initial seed.
     *
     * @param seed long : The initial seed of the underlying pseudo random generator
     */
    public MersenneTwisterRandomGenerator(long seed) {

        mersenneTwister = new int[624];
        javaAPIRandomGenerator = new Random(42);
        this.setSeed(seed);
    }

    /**
     * Returns the next pseudo random uniform [0,1] distributed double value from
     * the stream produced by the underlying pseudo random number generator.
     *
     * @return double : The next pseudo random uniform [0,1] distributed double
     *         value
     */
    @Override
    public double nextDouble() {

        return (((long) nextInt(26) << 27) + nextInt(27)) / (double) (1L << 53);
    }

    /**
     * Returns the next pseudo random integer of a given bit length.
     *
     * @param bits int : Bit length of the integer to be returned
     *
     * @return double : The next pseudo random integer of a given bit length
     */
    public int nextInt(int bits) {

        int y = this.mersenneTwister[currentIndex];
        currentIndex++;

        // re-determine random numbers if pool exhausted
        if (currentIndex > 623)
            this.twistNumbers();

        // determine tempered random number
        y = y ^ y >>> 11;
        y = y ^ (y << 7) & 0x9d2c5680;
        y = y ^ (y << 15) & 0xefc60000;
        y = y ^ y >>> 18;

        // return number (bit length as desired)
        return y >>> 32 - bits;
    }

    /**
     * Returns the next pseudo random uniform distributed long value from the stream
     * produced by the underlying pseudo random number generator.
     */
    public long nextLong() {
        return ((long) (nextInt(32)) << 32) + nextInt(32);
    }

    /**
     * Sets the seed for the pseudo random number generator.
     *
     * @param newSeed long : The new initial seed value for the pseudo random number
     *                generator
     */
    @Override
    public void setSeed(long newSeed) {

        Random javaAPIRandomGenerator = new Random(newSeed);

        // Initialise the generator from seed
        for (int i = 0; i < 624; i++) {
            mersenneTwister[i] = javaAPIRandomGenerator.nextInt();
        }

        // Twist them!
        this.twistNumbers();
    }

    /**
     * Internal procedure to create ("twist") 624 new pseudo random numbers based on
     * the last 624 pseudo random numbers.
     */
    public void twistNumbers() {

        for (int i = 0; i < 624; i++) {
            int y = mersenneTwister[i] & 0x80000000 | mersenneTwister[(i + 1) % 624] & 0x7fffffff;
            if (y % 2 == 0) {
                mersenneTwister[i] = mersenneTwister[(i + 397) % 624] ^ y >>> 1;
            } else {
                mersenneTwister[i] = mersenneTwister[(i + 397) % 624] ^ y >>> 1 ^ 0x9908b0df;
            }
        }
        this.currentIndex = 0; // reset index to of number to read next
    }
}
