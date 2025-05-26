package desmoj.core.dist;

import desmoj.core.report.ContDistEmpiricalReporter;
import desmoj.core.simulator.Model;

/**
 * Empirically distributed stream of pseudo random numbers of type
 * <code>double</code>. Values produced by this distribution follow an empirical
 * distribution which is specified by entries consisting of the observed value and the frequency (probability) this
 * value has been observed to occur. These entries are made by using the <code>addEntry()</code> method. There are a few
 * conditions a user has to meet before actually being allowed to take a sample of this distribution.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @see desmoj.core.dist.Distribution
 */

/**
 * @author JohannesGoebel
 *
 */
public class ContDistEmpirical extends ContDist {

    /**
     * Shows if a value with cumulative probability of one has been added yet.
     */
    private boolean _cumProOneInitialized;
    /**
     * Shows if a value with cumulative probability of zero has been added yet.
     */
    private boolean _cumProZeroInitialized;
    /**
     * Shows if the empirical distribution has been properly initialized.
     */
    private boolean _isInitialized;
    /**
     * Vector to store the entries of Value/cumulative frequency pairs.
     */
    private java.util.ArrayList<Entry> _values;

    /**
     * Constructs an empirical distribution pruducing floating point values.
     * Empirical distributions have to be initialized manually before use. This is
     * done by calling the <code>addEntry(long, double)</code> method to add values
     * defining the behaviour of the desired distribution.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public ContDistEmpirical(Model owner, String name, boolean showInReport, boolean showInTrace) {

        super(owner, name, showInReport, showInTrace);
        _values = new java.util.ArrayList<>(); // Initialize List for values
        _isInitialized = false; // No entries made yet
        _cumProOneInitialized = false; // No entries made yet
        _cumProZeroInitialized = false; // No entries made yet

    }

    /**
     * Adds a new entry of an empirical value and its associated cumulative
     * frequency. No two values may be equal, since there can not be two different
     * frequencies for one observed value. Entries with same value/frequency pair as
     * an entry already made before are simply ignored.
     *
     * As soon as there is exactly one value with a cumulative probability of 0 and
     * one value with a cumulative probability of 1, this distribution is considered
     * "initialized", making it possible to obtain samples via the sample() method.
     *
     * @param value     double : The empirical value observed
     * @param frequency double : The corresponding cumulative frequency of the
     *                  empirical value
     */
    public void addEntry(double value, double frequency) {

        // frequency must be in legal range
        if ((frequency < 0.0) || (frequency > 1.0)) {
            sendWarning("Can't add empirical entry! Command ignored.",
                        "ContDistEmpirical : " + getName() + " Method: void addEntry(double value, double frequency)",
                        "The frequency parameter given is invalid becaus it is out " + "of range : " + frequency,
                        "Be sure to add entries with positive frequency in the " + "range [0,1].");
            return; // no proper parameter
        }

        // check for invalid values/frequencies

        for (int i = 0; i < _values.size(); i++) {

            if (_values.get(i).entryValue == value) {
                sendWarning("Can't add empirical entry! Command ignored.", "ContDistEmpirical : " + getName()
                            + " Method: void addEntry(double value, double frequency)",
                            "The entry given is invalid because its value is already in the list.",
                            "Be sure not to add entries with duplicate values.");
                return; // no proper parameter
            } else if (_values.get(i).entryValue > value) {
                if (i == 0 && _values.get(i).entryFrequency >= frequency) {
                    addEntryToList(i, value, frequency);
                    return;
                } else if (i == 0 || _values.get(i).entryFrequency < frequency || _values.get(i - 1).entryFrequency
                > frequency) {
                    sendWarning("Can't add empirical entry! Command ignored.", "ContDistEmpirical : " + getName()
                                + " Method: void addEntry(double value, double frequency)",
                                "The entry given is invalid because it does not fit into the list of already added entries",
                                "Be sure to add entries that fit into the empirical distribution function.");
                    return; // no proper parameter
                }

                addEntryToList(i, value, frequency);
                return;
            }
        }
        if (_values.isEmpty()) {
            addEntryToList(0, value, frequency);
        } else if (_values.get(_values.size() - 1).entryFrequency <= frequency) {
            addEntryToList(_values.size(), value, frequency);
        }

    }

    /**
     * Creates the default reporter for the RealDistEmpirical distribution.
     *
     * @return Reporter : The reporter for the RealDistEmpirical distribution
     * @see ContDistEmpiricalReporter
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.ContDistEmpiricalReporter(this);

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
    public Double getInverseOfCumulativeProbabilityFunction(double p) {

        if (!_isInitialized) { // no valid entries in Vector
            sendWarning("Invalid sample returned!",
                        "ContDistEmpirical : " + getName() + " Method: double getInverseOfCumulativeProbability()",
                        "The distribution has not been initialized properly yet, "
                        + "thus no valid samples can be taken from it!",
                        "Be sure to have the distribution initialized properly "
                        + "before using it. You can make sure by calling method "
                        + "isInitialized() which returns a boolean telling "
                        + "you whether the distribution is initialized or not.");
            return 0d; // no proper parameter but return zero to prevent
            // aborting
            // the simulation
        }

        int i = 1; // counting variable to loop values

        while (_values.get(i).entryFrequency < p) {
            i++; // try next item
        }

        double lowVal = _values.get(i - 1).entryValue;
        double lowFreq = _values.get(i - 1).entryFrequency;
        double highVal = _values.get(i).entryValue;
        double highFreq = _values.get(i).entryFrequency;
        double newSample = lowVal + (((highVal - lowVal) * (p - lowFreq)) / (highFreq - lowFreq));

        return newSample;

    }

    /**
     * Shows if the RealDistEmpirical distribution already is initialized. Being
     * initialized means that there is exactly one value with a cumulative
     * probability of 0 and one value with a cumulative probability of 1.
     *
     * @return boolean
     */
    public boolean isInitialized() {

        return _isInitialized;

    }

    /**
     * Returns the next sample specified by the empirical distribution. Return
     * values are calculated by linear interpolation between the two values
     * surrounding that value given by the random generator.
     *
     * @return Double : The next sample for this empirical distribution or 0 if the
     *         distribution has not been properly initialized yet
     */
    @Override
    public Double sample() {

        return super.sample();
    }

    private void addEntryToList(int position, double value, double frequency) {

        if (frequency == 0) {
            _cumProZeroInitialized = true;
            if (_cumProOneInitialized) {
                _isInitialized = true;
            }
        }

        if (frequency == 1) {
            _cumProOneInitialized = true;
            if (_cumProZeroInitialized) {
                _isInitialized = true;
            }
        }

        _values.add(position, new Entry(value, frequency));

    }

    /**
     * Entries in the Vector for RealDistEmpirical. In order to keep tightly coupled
     * classes in one location, this class is a member class of RealDistEmpirical
     *
     * @author Tim Lechler
     * @see desmoj.core.dist.ContDistEmpirical
     */
    private static class Entry {

        /**
         * Holds the cumulative frequency of a value/frequency pair.
         */
        private double entryFrequency;

        /**
         * Holds the value of a value/frequency pair.
         */
        private double entryValue;

        /**
         * Creates an Entry for the RealDistEmpirical distribution with the actual value
         * and cumulative frequency given as parameters.
         *
         * @param val  double : The value of the empirical sample
         * @param freq double : The cumulative frequency of the empirical sample
         */
        private Entry(double val, double freq) {
            entryValue = val;
            entryFrequency = freq;
        }

    }
}
