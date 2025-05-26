package desmoj.core.statistic;

/**
 * Supplies observers with texts of their interest. It resembles the "observable" part of the observer pattern as
 * described in [Gamm95]. Thus it extends the standard "Observable" class provided by the Java.util package. <br There
 * are two possible ways to have other objects register changes of another object's status: <ol <liCall the
 * <code"update(String updateStr)"</codemethod of an observer to pass the String to be counted directly to one other
 * object interested. Using the direct call allows easy passing of a String to a single observer. No instance of this
 * <codeTextSupplier</codeclass is needed using direct passing of Strings.</li <liUsing the <codeTextSupplier</codeto
 * publish the information to interested observers requires you to derive a class and implement the call of the
 * observer's <codeupdate(Observable, Object)</codemethod with the required texts. This way of passing Strings involves
 * more code to be written, but offer the potential to have multiple observers and change the number of observers during
 * runtime. These options are not available when using the simpler version above.</li </ol
 *
 * @author Lorna Slawski based on the class ValueSupplier
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

@SuppressWarnings("deprecation")
public abstract class TextSupplier extends java.util.Observable {
    /*
     * The name of the TextSupplier as a String.
     */
    private String _name;

    /**
     * Constructs a simple TextSupplier by giving it a name.
     */
    public TextSupplier(String name) {
        // call the constructor of java.util.Observable
        super();

        // get hold of the name of this TextSupplier
        this._name = name;
    }

    /**
     * Returns the name of the named object. This is the same name displayed in reports and trace files when this named
     * object is shown in those reports or trace files.
     *
     * @return java.lang.String : The name of the named object.
     */
    public String getName() {

        return _name;
    }

    /**
     * Returns the quoted name of the named object. This is the name displayed in reports and trace files when this
     * named object is shown.
     *
     * @return java.lang.String : The quoted name of the named object
     */
    public String getQuotedName() {

        return "'" + _name + "'";
    }

    /**
     * Indicates that this TextSupplier (as an Observable) has changed and notifies all its observers of this change
     * providing the modified text as parameter. This is the "Push-model", thus pushing the String (in the parameter
     * <codearg</code) to the Observer (thus the <codeStatisticObject</code).
     *
     * @param arg Object : The Object wrapping the text with which the observers will be updated.
     */
    public void notifyStatistics(Object arg) {
        setChanged(); // call the method from the Observable

        notifyObservers(arg); // call the method from the Observable
    }

    /**
     * Changes the name of the named object. This might be necessary for an automatically created named object of a user
     * defined model, but should not be used for elements of the framework or basic objects of the user model. Changing
     * names of objects while runtime will confuse any trace output and generally make traces more difficult if not
     * impossible to follow.
     *
     * @param newName java.lang.String : The new name for the named object.
     */
    public void rename(String newName) {

        _name = newName;
    }

    /**
     * When using the Pull-Model or the automatic update function this method must be overridden by the user in a way
     * that it provides the text (as a <codejava.lang.String</code) desired.
     *
     * @return String : The String within this method.
     */
    public abstract String text();

    /**
     * Overrides the java.lang.Object's toString method to return the named object's name when given as parameter to a
     * method that expects a string to be passed.
     *
     * @return java.lang.String : The named object's name.
     */
    @Override
    public String toString() {
        return _name;
    }
}
