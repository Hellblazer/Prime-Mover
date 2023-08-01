package desmoj.core.statistic;

import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimClock;

/**
 * The <code>TextHistogram</code> class is providing a statistic analysis about
 * text values. Given Strings are counted and the report will show the amount of
 * every given text. If the given parameter is a null pointer it will not be
 * counted.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Lorna Slawski
 * @author based on the classes <code>Histogram</code> and
 *         <code>ValueStatistics</code> from Soenke Claassen
 * @author based on DESMO-C from Thomas Schniewind, 1998
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
@SuppressWarnings("deprecation")
public class TextHistogram extends desmoj.core.simulator.Reportable implements java.util.Observer {

    // ****** attributes ******

    /**
     * The last String received from the TextSupplier
     */
    private String _lastString;

    /**
     * The alphabetically sorted map. The keys are given by the Strings and their
     * values are their amount.
     */
    private Map<String, Long> _map;

    /**
     * The ValueSupplier providing the Strings which will be processed.
     */
    private TextSupplier _textSuppl;

    /**
     * Constructor for a TextHistogram object that will NOT be connected to a
     * <code>TextSupplier</code>.
     *
     * @param ownerModel   Model : The model this TextHistogram is associated to.
     * @param name         java.lang.String : The name of this TextHistogram object.
     * @param showInReport boolean : Flag for showing the report about this
     *                     Histogram.
     * @param showInTrace  boolean : Flag for showing the trace output of this
     *                     Histogram.
     */
    public TextHistogram(Model ownerModel, String name, boolean showInReport, boolean showInTrace) {
        // call the constructor of StatisticObject
        super(ownerModel, name, showInReport, showInTrace);

        // no ValueSupplier will be observed
        this._textSuppl = null;

        // make a new table
        this._map = new TreeMap<String, Long>();
    }

    /**
     * Constructor for a TextHistogram object that will be connected to a
     * <code>TextSupplier</code>.
     *
     * @param ownerModel   Model : The model this TextHistogram is associated to.
     * @param name         java.lang.String : The name of this TextHistogram object.
     * @param textValSup   TextSupplier : The TextSupplier providing the String for
     *                     this TextHistogram. The given TextValueSupplier will be
     *                     observed by this TextHistogram object.
     * @param showInReport boolean : Flag for showing the report about this
     *                     TextHistogram.
     * @param showInTrace  boolean : Flag for showing the trace output of this
     *                     TextHistogram.
     */
    public TextHistogram(Model ownerModel, String name, TextSupplier textSup, boolean showInReport,
                         boolean showInTrace) {
        // call the constructor of StatisticObject
        super(ownerModel, name, showInReport, showInTrace);

        // textSup is no valid TextSupplier
        if (textSup == null) {
            sendWarning("Attempt to produce a TextHistogram about a non existing "
            + "TextValueSupplier. The command will be ignored!",
                        "TextHistogram: " + this.getName() + " Constructor: TextHistogram"
                        + " (Model ownerModel, String name, TextSupplier textSup, "
                        + "boolean showInReport, boolean showInTrace)",
                        "The given TextSupplier: textSup is only a null pointer.",
                        "Make sure to pass a valid TextSupplier when constructing a new " + "TextHistogram object.");
            return; // just return
        }
        this._textSuppl = textSup;

        // this TextHistogram will observe the given TextValueSupplier
        this._textSuppl.addObserver(this);

        // make a new table
        this._map = new TreeMap<String, Long>();
    }

    /**
     * Returns a Reporter to produce a report about this TextHistogram.
     *
     * @return desmoj.report.TextHistogramReporter : The Reporter for this
     *         TextHistogram.
     *
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {
        return new desmoj.core.report.TextHistogramReporter(this);
    }

    /**
     * Returns the last observed String of this TextHistogram object.
     *
     * @return java.lang.String : The last String observed so far.
     */
    public String getLastString() {
        return this._lastString;
    }

    /**
     * Returns the String with the least number of entries. Several Strings are
     * separated by a comma. If there are no entries yet an empty String ("") is
     * being returned.
     *
     * @return java.lang.String: The String with the least number of entries.
     */
    public String getLeastFrequentedString() {
        long leastValue = Long.MAX_VALUE; // largest possible value
        String leastStrings = "";
        long value;
        for (String str : this._map.keySet()) {
            value = this._map.get(str);
            if (value < leastValue) {
                leastStrings = str;
                leastValue = value;
            } else if (value == leastValue) {
                leastStrings = leastStrings + ", " + str;
            }
        }
        return leastStrings;
    }

    /**
     * Returns the String with the largest number of entries. Several Strings are
     * separated by a comma. If there are no entries yet an empty String ("") is
     * being returned.
     *
     * @return java.lang.String: The String with the highest number of entries.
     */
    public String getMostFrequentedString() {
        long mostValue = 0;
        String mostStrings = "";
        long value;
        for (String str : this._map.keySet()) {
            value = this._map.get(str);
            if (value > mostValue) {
                mostStrings = str;
                mostValue = value;
            } else if (value == mostValue) {
                mostStrings = mostStrings + ", " + str;
            }
        }
        return mostStrings;
    }

    /**
     * Returns the number of Strings observed.
     *
     * @return int : The number of Strings which have been observed.
     */
    public int getNumberOfStringsObserved() {
        return this._map.size();
    }

    /**
     * Returns the number of observations for the given String.
     *
     * @param obsStr java.lang.String: The String of which want to get the number of
     *               observations made for.
     * @return long : The number of observations for the given String.
     */
    public long getObservationsOfString(String obsStr) {
        if (this._map.containsKey(obsStr)) {
            return this._map.get(obsStr);
        } else {
            return 0;
        }

    }

    /**
     * Returns an array of Strings observed.
     *
     * @return java.lang.String[] : The array of Strings observed.
     */
    public String[] getStringsObserved() {
        String[] observed = new String[getNumberOfStringsObserved()];
        int i = 0;
        for (String str : this._map.keySet()) {
            observed[i] = str;
            i++;
        }
        return observed;
    }

    /**
     * Resets this TextHistogram object by deleting all observed Strings.
     */
    @Override
    public void reset() {
        super.reset();
        this._map.clear();
    }

    /**
     * Updates this <code>TextHistogram</code> object by fetching the actual value
     * of the <code>TextSupplier</code> and processing it. If the actual value is a
     * <code>null</code> pointer it will not be counted and an error is given out in
     * the error report. The <code>TextSupplier</code> is passed in the constructor
     * of this <code>TextHistogram</code> object. This <code>update()</code> method
     * complies with the one described in DESMO, see [Page91].
     */
    public void update() {
        if (this._textSuppl == null) {
            sendWarning("Attempt to update a TextHistogram that is not "
            + "connected to a TextSupplier. No String is provided with which "
            + "the histogram could be updated. The command will be ignored!",
                        "TextHistogram: " + this.getName() + " Method: update()",
                        "The given TextSupplier is only a null pointer.",
                        "Make sure to update a TextHistogram only when it is connected "
                        + "to a valid TextSupplier. Or use the update(String updateStr) method.");

            return; // that's it
        }
        this._lastString = this._textSuppl.text();
        if (this._lastString == null) {
            sendWarning("Attempt to insert a null pointer to the TextHistogram. "
            + "The null pointer will not be counted! ", "update(): ", "Invalid reference. ",
                        "Make sure to pass a valid String when calling the " + "update method.");
            return; // that's it
        } else {
            incrementObservations();
            if (this._map.containsKey(this._lastString)) {
                this._map.put(this._lastString, this._map.get(this._lastString) + 1);
            } else {
                this._map.put(this._lastString.toString(), 1L);
            }
            traceUpdate(); // leave a message in the trace
        }
    }

    /**
     * Implementation of the virtual <code>update(Observable, Object)</code> method
     * of the <code>Observer</code> interface. This method will be called
     * automatically from an <code>Observable</code> object within its
     * <code>notifyObservers()</code> method. <br>
     * If no Object (a<code>null</code> value) is passed as arg, the actual value of
     * the TextSupplier will be fetched with the <code>text()</code> method of the
     * TextSupplier. Otherwise it is expected that the actual text is passed in the
     * Object arg. If the the actual value is a <code>null</code> pointer it will
     * not be counted and an error is given out in the error report.
     *
     * @param o   java.util.Observable : The Observable calling this method within
     *            its own <code>notifyObservers()</code> method.
     * @param arg Object : The Object with which this <code>Statistic Object</code>
     *            is updated. Normally a String which is added to the statistics or
     *            <code>null</code> when there is an actual text to be given with
     *            the <code>text()</code> method.
     */
    @Override
    public void update(Observable o, Object arg) {
        // update was called with no arg Object OR from the SimClock
        if (arg == null || o instanceof SimClock) {
            if (_textSuppl == null) {
                sendWarning("Attempt to update a TextHistogram that is not "
                + "connected to a TextValueSupplier. No value is provided with which "
                + "the statistic could be updated. The command will be ignored!",
                            "TextHistogram: " + this.getName() + " Method: update " + "(Observable o, Object arg)",
                            "The given TextValueSupplier: TextValSuppl is only a null pointer.",
                            "Make sure to update a TextHistogram only when it is connected "
                            + "to a valid TextValueSupplier. Or use the update(String) method.");

                return; // that's it
            }

            // get the actual value from the TextSupplier
            this._lastString = this._textSuppl.text();

            if (this._lastString == null) {
                sendWarning("Attempt to insert a null pointer to the TextHistogram. "
                + "The null pointer will not be counted! ", "update(): ", "Invalid reference. ",
                            "Make sure to pass a valid String when calling the " + "update method.");
                return; // that's it
            }
        }

        else {
            if (arg instanceof String) {
                // get the value out of the Object arg
                this._lastString = arg.toString();
            } else {
                sendWarning("Attempt to update a TextHistogram with an argument "
                + "arg, that can not be recognized. The attempted action is ignored!",
                            "TextHistogram: " + this.getName() + " Method: update (Observable " + "o, Object arg)",
                            "The passed Object in the argument arg could not be recognized.",
                            "Make sure to pass null or a String object as the arg argument.");
                return; // do nothing, just return
            }
        }
        incrementObservations(); // use the method from the Reportable class
        if (this._map.containsKey(this._lastString)) {
            this._map.put(this._lastString, this._map.get(this._lastString) + 1);
        } else {
            this._map.put(this._lastString, 1L);
        }
        traceUpdate(); // leave a message in the trace
    }

    /**
     * Updates this <code>TextHistogram</code> object with the String value given as
     * parameter. If the parameter is a <code>null</code> pointer it will not be
     * counted and an error is given out in the error report. In some cases it might
     * be more convenient to pass the String value this <code>TextHistogram</code>
     * will be updated with directly within the
     * <code>update(String updateStr)</code> method instead of going via the
     * <code>TextSupplier</code>.
     *
     * @param updateStr String : The String with which this
     *                  <code>TextHistogram</code> will be updated.
     */
    public void update(String updateStr) {
        if (updateStr == null) {
            sendWarning("Attempt to insert a null pointer to the TextHistogram. "
            + "The null pointer will not be counted! ", "update(): ", "Invalid reference. ",
                        "Make sure to pass a valid String when calling the " + "update method.");
            return; // that's it
        } else {
            this._lastString = updateStr;
            super.incrementObservations();
            if (this._map.containsKey(updateStr)) {
                this._map.put(updateStr, this._map.get(updateStr) + 1);
            } else {
                this._map.put(updateStr, 1L);
            }
            traceUpdate(); // leave a message in the trace
        }
    }

    /**
     * Returns the TextSupplier object providing all the Strings.
     *
     * @return TextSupplier : The TextSupplier object providing the Strings for this
     *         TextHistogram.
     */
    protected TextSupplier getTextSupplier() {
        return this._textSuppl;
    }

    /**
     * Leaves a message in the trace that this StatisticObject has been updated.
     */
    protected void traceUpdate() {
        if (currentlySendTraceNotes()) {
            sendTraceNote("updates " + this.getQuotedName());
        } // tell in the trace which StatisticObject is updated
    }
}
//\ Kein Zeilenumbruch am Dateiende.
