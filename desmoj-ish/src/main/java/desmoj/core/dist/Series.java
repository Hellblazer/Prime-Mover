package desmoj.core.dist;

import java.util.ArrayList;
import java.util.Collection;

import desmoj.core.simulator.Model;

/**
 * A series is a special distribution returning preset, user-defined entries
 * from a list. Series may be used to simulate certain non-random scenarios
 * within the simulation or to include external sources of (preudo) random
 * distributions
 * <p>
 *
 * The internal list can be set to be traversed backwards and/or to repeat once
 * its end has been reached.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Broder Fredrich
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
public abstract class Series<O extends Object> extends Distribution {

    /**
     * List of all entries passed to the series.
     */
    private ArrayList<O> _allValList;

    /**
     * Reading Direction of the value list. False: Forward (default) True: Backwards
     */
    private boolean _direction;

    /**
     * List index of <b>next</b> sample to return.
     */
    private int _index;

    /**
     * Whether or not the list is getting repeated once the last value has been
     * read.
     */
    private boolean _repeat;

    /**
     * Creates a new Series. Default behaviour when returning samples is - starting
     * at 1st element - reading forward - non-repeating
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public Series(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        _direction = false;
        _repeat = false;
        _index = 0;
        _allValList = new ArrayList<>();
    }

    /**
     * Adds a new sample entry at the end of the entry list.
     *
     * @param element E : entry to be added
     */
    public void add(O element) {
        _allValList.add(element);
    }

    /**
     * Adds all elements of a given Collection to the entry list.
     *
     * @param collection Collection<E> : The collection whose entries will be added.
     */
    public void addAll(Collection<O> collection) {
        _allValList.addAll(collection);
    }

    /**
     * Removes all entries from the sample list.
     */
    public void clearList() {
        _allValList.clear();
        _index = 0;
    }

    /**
     * Creates the default reporter for the series.
     *
     * @return Reporter : The reporter for the Series
     */
    @Override
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new desmoj.core.report.SeriesReporter(this);
    }

    /**
     * Returns the amount of samples this series can still return, given the current
     * reading direction and repeating property.
     *
     * @return int : Amount of samples that can be returned before the series
     *         reaches its end.
     */
    public int getNumberOfAvailableElements() {
        if (_repeat)
            return Integer.MAX_VALUE;
        else {
            if (!_direction) {
                return _allValList.size() - _index;
            } else
                return _index + 1;
        }
    }

    /**
     * Returns whether this series is getting read forward.
     *
     */
    public boolean isforward() {
        return !_direction;
    }

    /**
     * Removes am entry at a certain position out of the sample list, lowering the
     * index of all following entries by one.
     *
     * @param position int : The position to remove the value from.
     */
    public void remove(int position) {
        _allValList.remove(position);
    }

    /**
     * Returns the next sample from the entry list.
     *
     * @return E : The sample to be returned.
     */
    public O sample() {
        O returnval;
        if (_allValList.isEmpty()) {
            sendWarning("Failed to return sample value", "Series : " + getName() + " in sample()",
                        "Sample value list is empty.",
                        "Make sure to add values to the series before trying to get samples from it!");
            return null;
        }
        if (!_direction) {
            if (_index < _allValList.size()) {
                returnval = _allValList.get(_index);
                _index++;
            } else {
                if (_repeat) {
                    _index = 1;
                    returnval = _allValList.get(_index - 1);
                } else {
                    sendWarning("Failed to return sample", "Series : " + getName() + " in sample()",
                                "The value list has reached its end, and the series is set to non-repeating.",
                                "To get further samples from this series, you must either add additional ones"
                                + "or tell the series to repeat itself by calling setRepeating(true).");
                    returnval = null;
                }
            }
        } else if (_index >= 0) {
            returnval = _allValList.get(_index);
            _index--;
        } else {
            if (_repeat) {
                _index = _allValList.size() - 2;
                returnval = _allValList.get(_index + 1);
            } else {
                sendWarning("Failed to return sample", "Series : " + getName() + " in sample()",
                            "The value list has reached its end, and the series is set to non-repeating.",
                            "To get further samples from this series, you must either revert the reading direction by calling setReverse(true)"
                            + "or tell the series to repeat itself by calling setRepeating(true).");
                returnval = null;
            }
        }
        return returnval;
    }

    /**
     * Convenience method to return the series' sample as <code>Object</code>. For
     * type safety, method <code>sample()</code> should be preferred. However, this
     * method is useful for environments requiring a non-genetic access point to
     * obtain samples from any distribution.
     *
     * @return Object : A sample from this this distribution wrapped as
     *         <code>Object</code>.
     */
    @Override
    public Object sampleObject() {
        return sample();
    }

    /**
     * Replaces the entry at a certain position with a given entry.
     *
     * @param element  E : The new entry to replace the old one.
     * @param position int : The position at which the entry should be replaced.
     */
    public void set(O element, int position) {
        _allValList.set(position, element);
    }

    /**
     * Sets whether reading samples from the list is continued from start once the
     * last sample has been returned. If the list is getting read backwards,
     * enabling this will cause the series to start over from the end of the list
     * once the first value has been read.
     *
     * @param repeat boolean : Whether this series repeats its value list once the
     *               last value has been returned.
     */

    public void setRepeating(boolean repeat) {
        _repeat = repeat;
    }

    /**
     * Sets the reading direction for the sample list of this series.
     *
     *
     * @param direc boolean : true - forward; false - backwards
     */
    public void setReverse(boolean direc) {
        if (!_direction && direc) {
            _index = _index - 2;
        } else if (_direction && !direc) {
            _index = _index + 2;
        }
        _direction = direc;
    }
}
