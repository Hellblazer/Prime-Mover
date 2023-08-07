package desmoj.core.dist;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.BinomialDistribution;

import desmoj.core.simulator.Model;

/**
 * Distribution returning binomial distributed long values. The binomial
 * distribution describes the probability of having a certain amount of
 * successes in a series of indepedent Bernoulli experiments, all having the
 * same success probability.
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

public class DiscreteDistBinomial extends DiscreteDist<Long> {

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
         * @param val  long : The entry value
         * @param freq double : The cumulative frequency of this entry value
         */
        private Entry(long val, double freq) {
            entryValue = val;
            entryCumProbability = freq;
        }

    }

    /**
     * The amount of separate Bernoulli experiments that lead to the result.
     */
    protected int amount;

    /**
     * The probability of success in each separate Bernoulli experiment.
     */
    protected double probability;

    /**
     * List to store the computed distribution values for each outcome.
     */
    private List<Entry> valueList;

    /**
     * Creates a stream of pseudo random numbers following a binomial distribution.
     * The specific parameters p (probability) and n (amount) have to be given here
     * at creation time.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param probability  double : The probability of success in each separate
     *                     Bernoulli experiment.
     * @param amount       int : The amount of separate Bernoulli experiments that
     *                     lead to the result.
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public DiscreteDistBinomial(Model owner, String name, double probability, int amount, boolean showInReport,
                                boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        this.probability = probability;
        this.amount = amount;
        valueList = new ArrayList<>();
        Entry e;
        BinomialDistribution bdist = new BinomialDistribution(this.amount, this.probability);
        for (int i = 0; i < this.amount; i++) {
            e = new Entry(i, bdist.cumulativeProbability(i));
            valueList.add(e);
            System.out.println(i + " " + bdist.cumulativeProbability(i));

        }
        e = new Entry(this.amount, 1.0);
        valueList.add(e);
    }

    /**
     * Creates the default reporter for the DiscreteDistBinomial distribution.
     *
     * @return Reporter : The reporter for the DiscreteDistBinomial distribution
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.DiscreteDistBinomialReporter(this);

    }

    /**
     * Returns the amount of separate Bernoulli experiments that lead to the result.
     *
     * @return int : The amount of separate Bernoulli experiments that lead to the
     *         result.
     */
    public int getAmount() {

        return amount;

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
    @Override
    public Long getInverseOfCumulativeProbabilityFunction(double p) {

        int i = 0;
        while ((i < valueList.size()) && (valueList.get(i).entryCumProbability < p)) {
            i++;
        }

        return valueList.get(i).entryValue;
    }

    /**
     * Returns the probability of success in each separate Bernoulli experiment
     *
     * @return double : The probability of success in each separate Bernoulli
     *         experiment.
     */
    public double getProbability() {

        return probability;
    }

    /**
     * Returns the next sample from this distribution. The value depends upon the
     * seed, the number of values taken from the stream by using this method before
     * and the probability and amount of trials specified for this distribution.
     *
     * @return Long : The next binomial distributed sample from this distribution.
     */
    @Override
    public Long sample() {

        return super.sample();
    }

}
