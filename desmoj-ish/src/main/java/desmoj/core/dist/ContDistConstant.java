package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * This constant "pseudo"-distribution returns a single constant predefined value of type Double. This "distribution" is
 * most useful for testing purposes. The value to be returned can be specified at construction time.
 *
 * @author Peter Wueppen
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @see desmoj.core.dist.Distribution
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
public class ContDistConstant extends ContDist {

    private double _constValue;

    /**
     * Constructs a simple distribution which produces samples equal to the constant value it has been given.
     *
     * @param owner         Model : The distribution's owner
     * @param name          java.lang.String : The distribution's name
     * @param constantValue double : The constant value produced by this distribution
     * @param showInReport  boolean : Flag for producing reports
     * @param showInTrace   boolean : Flag for producing trace output
     */
    public ContDistConstant(Model owner, String name, double constantValue, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        _constValue = constantValue;

    }

    /**
     * Creates the default reporter for the constant distribution.
     *
     * @return Reporter : The reporter for the constant distribution
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.ContDistConstantReporter(this);

    }

    /**
     * Returns the constant value associated with this
     * <code>ConstantDiscreteDist</code> distribution
     *
     * @return N : The constant value returned by this
     * <code>ConstantDiscreteDist</code> distribution
     */
    public double getConstantValue() {

        return _constValue;

    }

    /**
     * Method to map a double <code>p</code> from 0...1 to the distribution's domain by determining the value x that
     * satisfies <code>P(X &lt; x) = p</code>.
     *
     * @param p double: A value between 0 and 1
     * @return Double : The constant value
     */
    @Override
    public Double getInverseOfCumulativeProbabilityFunction(double p) {

        return this._constValue;

    }

    /**
     * Returns the next constant sample of this distribution. For this "pseudo"-distribution it is always is the default
     * value specified through the constructor or via the <em>setConstant</em> method.
     *
     * @return Double : The constant sample
     */
    @Override
    public Double sample() {

        return super.sample();
    }

    /**
     * Changes the constant value to the new one specified.
     *
     * @param newValue double : the new value to be returned by this pseudo distribution
     */
    public void setConstant(double newValue) {

        if (nonNegative && newValue < 0) {
            sendWarning("You set a nonNegative ContDistConstant distribution to a " + "new negative constant.",
                        "ContDistConstant: " + this.getName() + " Method: public void " + "setConstant(long newValue)",
                        "The given constant is negative. But the ContDistConstant distribution "
                        + "is set to nonNegative. That does not make sense.",
                        "Make sure not to set a nonNegative ContDistConstant distribution "
                        + "to a negative constant.");
        }

        _constValue = newValue; // sets constant return-value to new one given

    }

    /**
     * Overrides the same method of <code>desmoj.dist.Distribution</code>. It makes no sense to set a
     * <code>ConstantDiscreteDist</code> object with a negative constant to nonNegative. In this case a warning will be
     * sent. <br> Sets the nonNegative switch to the given value. If nonNegative is set to
     * <code>true</code> the distribution returns positive samples only, otherwise
     * it also produces negative samples, if possible.
     *
     * @param newValue boolean : If <code>true</code> the distribution is set to return positive samples only, otherwise
     *                 it also produces negative samples, if possible.
     */
    @Override
    public void setNonNegative(boolean newValue) {

        if (newValue && _constValue < 0) {
            sendWarning("Attempt to set a ContDistConstant distribution with a "
                        + "negative constant to nonNegative. This does not make sense!"
                        + "The negative constant will still be returned!",
                        "ContDistConstant: " + this.getName() + " Method: public void "
                        + "setNonNegative(boolean newValue)",
                        "The given distribution has a negative constant but all negative "
                        + "values should be ignored. The negative constant will be returned " + "anyway!",
                        "Make sure not to set a ContDistConstant distribution with a negative "
                        + "constant to nonNegative.");
        }

        this.nonNegative = newValue;
    }
}
