package desmoj.core.dist;

import java.util.ArrayList;
import java.util.List;

import desmoj.core.simulator.Model;

/**
 * Distribution returning Geometrically distributed int values. The Geometrical
 * distribution describes the probability of having a certain waiting time for
 * the first success in a series of indepedent Bernoulli experiments, all having
 * the same success probability.
 * <p>
 *
 * Note: The generated values are not the amount of experiments done to get the
 * first success, but the amount of failures before the first success.
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

public class DiscreteDistGeo extends DiscreteDist<Long> {

    private static class Entry {

        /**
         * The cumulative probability of the entry Value, P(X <= entryValue).
         */
        private double entryCumProbability;

        /**
         * The entry value (amount of successes this entry is about)
         */
        private long entryValue;

        /**
         * Constructs a simple entry pair with the given value and cumulative
         * probability.
         *
         * @param val  int : The entry value
         * @param freq double : The cumulative frequency of this entry value
         */
        private Entry(long val, double freq) {
            entryValue = val;
            entryCumProbability = freq;
        }

    }

    /**
     * The probability of success in each separate Bernoulli experiment.
     */
    protected double probability;

    /**
     * Internal Value to more easily calculate cumulative probabilites.
     */
    private double reverseProbPower;

    /**
     * List to store the computed distribution values for each outcome.
     */
    private List<Entry> valueList;

    /**
     * Creates a stream of pseudo random numbers following a Geometrical
     * distribution. The specific parameter p (probability) has to be given here at
     * creation time.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param probability  double : The probability of success in each separate
     *                     Bernoulli experiment.
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public DiscreteDistGeo(Model owner, String name, double probability, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);

        if (probability <= 0 || probability >= 1) {
            sendWarning("Distribution not properly instantiated",
                        "DiscreteDistGeo : " + getName() + " at construction time",
                        "The given probability is not a value between 0 and 1",
                        "To get samples from this distribution, you must set the "
                        + "probability to be a double value larger than 0 and smaller than 1");
        }

        this.probability = probability;
        valueList = new ArrayList<>();
        Entry e;

        /**
         * Infinite Distribution, so cumulative probability values cannot be stored
         * completely. Thus, the values of roughly 99% of all cases get stored while the
         * rest is calculated on the fly if needed. Value is capped at 5 (lower bound)
         * and 100 (upper bound)
         */
        int storedAmount = (int) (Math.log(0.01) / Math.log(1 - probability));
        if (storedAmount < 5)
            storedAmount = 5;
        else if (storedAmount > 100)
            storedAmount = 100;

        reverseProbPower = (1 - probability);
        for (int i = 0; i < storedAmount; i++) {

            e = new Entry(i, 1 - reverseProbPower);

            valueList.add(e);
            reverseProbPower = reverseProbPower * (1 - probability);

        }
    }

    /**
     * Creates the default reporter for the DiscreteDistGeo distribution.
     *
     * @return Reporter : The reporter for the DiscreteDistGeo distribution
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.DiscreteDistGeoReporter(this);

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
    @Override
    public Long getInverseOfCumulativeProbabilityFunction(double p) {

        long newSample; // aux variable

        int i = 0;
        while ((i < valueList.size()) && (valueList.get(i).entryCumProbability < p)) {
            i++;
        }

        if (i < valueList.size()) {
            newSample = valueList.get(i).entryValue;
        } else {
            double tempReverseProbPower = reverseProbPower;
            while ((1 - tempReverseProbPower) < p) {
                tempReverseProbPower = tempReverseProbPower * (1 - probability);
                i++;
            }
            newSample = i;
        }

        return newSample;
    }

    /**
     * Returns the success probability in each separate Bernoulli experiment.
     *
     * @return double : The success probability in each separate Bernoulli
     *         experiment.
     */
    public double getProbability() {

        return probability;
    }

    /**
     * Returns the next sample from this distribution. The value depends upon the
     * seed, the number of values taken from the stream by using this method before
     * and the probability specified for this distribution.
     *
     * @return Long : The next Geometrically distributed sample from this
     *         distribution.
     */
    @Override
    public Long sample() {

        return super.sample();
    }

}
