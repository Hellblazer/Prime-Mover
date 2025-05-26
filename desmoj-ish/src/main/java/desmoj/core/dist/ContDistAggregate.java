package desmoj.core.dist;

import desmoj.core.simulator.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate Distribution composed of a list of input Distributions. The operator between these to input distributions
 * can be specified individually
 *
 * @author Peter Wueppen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public class ContDistAggregate extends ContDist {

    /**
     * The input distributions.
     */
    protected ArrayList<NumericalDist<?>> dists;

    /**
     * The operator to combine the Distributions with.
     */
    protected Operator operator;

    /**
     * Creates a stream of pseudo random numbers following a distribution that is specified by a list of other
     * distributions and an operator to aggregate them.
     * <p>
     *
     * Note that since it is possible to combine distributions of different return types, this distribution will always
     * cast their results to double and return a double itself.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param dists        List<NumericalDist> : The input distributions.
     * @param operator     Operator : The operator to be used to aggregate the distributions.
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public ContDistAggregate(Model owner, String name, List<NumericalDist<?>> dists, Operator operator,
                             boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);

        if (operator == null) {
            operator = Operator.PLUS;
            sendWarning(
            "Attempt to set a ContDistAggregate distribution with a missing (null) operator. The plus operator wil be used.",
            "ContDistAggregate: " + this.getName()
            + ", constructor ContDistAggregate(Model owner, String name, List<NumericalDist<?>> dists, Operator operator, boolean showInReport, boolean showInTrace)",
            "Operator given is null.", "Make sure to provide a non-null operator.");
        }
        this.operator = operator;

        this.dists = new ArrayList<>();
        for (NumericalDist<?> d : dists) {
            if (d != null) {
                this.dists.add(d);
            }
        }
        if (this.dists.isEmpty()) {
            this.dists.add(new ContDistConstant(owner, "0", 0, false, false));
            sendWarning(
            "Attempt to set a ContDistAggregate distribution without distributions provided by list dists. A singe constant distribution yielding 0 will be used instead.",
            "ContDistAggregate: " + this.getName()
            + ", constructor ContDistAggregate(Model owner, String name, NumericalDist<?> dist1, NumericalDist<?> dist2, Operator operator, boolean showInReport, boolean showInTrace)",
            "List of distribution given is empty or entries are all null.",
            "Make sure to provide non-null distributions.");
        }

    }

    /**
     * Creates a stream of pseudo random numbers following a distribution that is specified by two other distributions
     * and an operator to aggregate them.
     * <p>
     *
     * Note that since it is possible to combine distributions of different return types, this distribution will always
     * cast their results to double and return a double itself.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param dist1        NumericalDist<?> : The first input distribution.
     * @param dist2        NumericalDist<?> : The second input distribution.
     * @param operator     Operator : The operator to be used to combine the two distributions.
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public ContDistAggregate(Model owner, String name, NumericalDist<?> dist1, NumericalDist<?> dist2,
                             Operator operator, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);

        if (operator == null) {
            operator = Operator.PLUS;
            sendWarning(
            "Attempt to set a ContDistAggregate distribution with a missing (null) operator. The plus operator wil be used.",
            "ContDistAggregate: " + this.getName()
            + ", constructor ContDistAggregate(Model owner, String name, NumericalDist<?> dist1, NumericalDist<?> dist2, Operator operator, boolean showInReport, boolean showInTrace)",
            "Operator given is null.", "Make sure to provide a non-null operator.");
        }
        this.operator = operator;
        this.dists = new ArrayList<>();

        if (dist1 != null) {
            this.dists.add(dist1);
        } else {
            this.dists.add(new ContDistConstant(owner, "0", 0, false, false));
            sendWarning(
            "Attempt to set a ContDistAggregate distribution with a missing (null) dist1. A constant distribution yielding 0 will be used instead.",
            "ContDistAggregate: " + this.getName()
            + ", constructor ContDistAggregate(Model owner, String name, NumericalDist<?> dist1, NumericalDist<?> dist2, Operator operator, boolean showInReport, boolean showInTrace)",
            "First distribution given is null.", "Make sure to provide a non-null distribution.");
        }

        if (dist2 != null) {
            this.dists.add(dist2);
        } else {
            this.dists.add(new ContDistConstant(owner, "0", 0, false, false));
            sendWarning(
            "Attempt to set a ContDistAggregate distribution with a missing (null) dist2. A constant distribution yielding 0 will be used instead.",
            "ContDistAggregate: " + this.getName()
            + ", constructor ContDistAggregate(Model owner, String name, NumericalDist<?> dist1, NumericalDist<?> dist2, Operator operator, boolean showInReport, boolean showInTrace)",
            "Second distribution given is null.", "Make sure to provide a non-null distribution.");
        }
    }

    /**
     * Creates the default reporter for the ContDistAggregate distribution.
     *
     * @return Reporter : The reporter for the ContDistAggregate distribution
     * @see desmoj.core.report.ContDistAggregateReporter
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.ContDistAggregateReporter(this);

    }

    /**
     * Returns the first input distribution.
     *
     * @return The first input distribution
     */
    public List<NumericalDist<?>> getDists() {
        return new ArrayList<>(dists);
    }

    /**
     * Method to map a double <code>p</code> from 0...1 to the distribution's domain by determining the value x that
     * satisfies <code>P(X &lt; x) = p</code>. Not supported, i.e. throwing an
     * <code>UnsupportedOperationException</code>.
     *
     * @param p double: A value between 0 and 1
     * @return An <code>UnsupportedOperationException</code> exception
     */
    @Override
    public Double getInverseOfCumulativeProbabilityFunction(double p) {
        throw new UnsupportedOperationException(this.getName() + " does not support inverse cumulative probabilities.");
    }

    /**
     * The Operator used to combine the input distributions.
     *
     * @return The Operator used to combine the two distributions.
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Returns the next sample from this distribution. The value depends upon the seed, the number of values taken from
     * the stream by using this method before and the two input distributions as well as the operator specified for this
     * distribution.
     * <p>
     * Evaluation is done from left to right, e.g. for three distributions, the results is <code>(dist1.sample x
     * d2.sample()) x d3.sample()</code>, where
     * <code>x</code> is the operation used.
     *
     * @return double : The next sample from this distribution.
     */
    @Override
    public Double sample() {

        incrementObservations(); // increase count of samples
        double newSample = Double.NaN;

        do {

            newSample = dists.get(0).sample().doubleValue();

            for (int i = 1; i < dists.size(); i++) {
                newSample = operator.result(newSample, dists.get(i).sample().doubleValue());
            }

        } while (nonNegative && newSample < 0);

        if (this.currentlySendTraceNotes()) {
            this.traceLastSample(Double.toString(newSample));
        }

        return newSample;
    }
}
