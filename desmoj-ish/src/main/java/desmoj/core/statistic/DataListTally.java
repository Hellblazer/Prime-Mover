package desmoj.core.statistic;

import desmoj.core.report.DataListHistogramReporter;
import desmoj.core.report.DataListTallyReporter;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Reportable;

import java.util.Observable;

/**
 * The <code>DataListTally</code> class is providing a statistic analysis about one value. The mean value and the
 * standard deviation is calculated on basis of the total number of observations. All observed values are stored in a
 * list to be able to calculate quartiles or histograms.
 *
 * @author Tim Janz
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @see Tally
 */

public class DataListTally extends Tally {

    /**
     * A list to store values passed to this Tally.
     */
    private DataList _list = new DataList(this.getModel(), this.getName() + " Data-List", false, false);
    /**
     * Should a histogram-reporter be used for the DataList?
     */
    private boolean _reportDataListHistogram = true;
    /**
     * Is the DataList already sorted?
     */
    private boolean _sorted = false;

    /**
     * Constructor for a DataListTally object.
     *
     * @param ownerModel   Model : The model this Tally is associated to
     * @param name         java.lang.String : The name of this Tally object
     * @param showInReport boolean : Flag for showing the report about this Tally.
     * @param showInTrace  boolean : Flag for showing the trace output of this Tally.
     */
    public DataListTally(Model ownerModel, String name, boolean showInReport, boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace);
    }

    /**
     * Creates a Reporter for this DataListTally.
     *
     * @return Reporter : The reporter for this DataListTally.
     */
    @Override
    public Reporter createDefaultReporter() {
        DataListTallyReporter result = new DataListTallyReporter(this);
        return result;
    }

    /**
     * Returns the sorted DataList.
     *
     * @return the DataList.
     */
    public DataList getDataListSorted() {
        if (!_sorted) {
            _list = _list.sort(_list);
            _sorted = true;
        }
        return _list;
    }

    /**
     * Will a histogram-reporter be used to create a report for the DataList?
     *
     * @return true if the reporting is enabled, false if not.
     */
    public boolean getReportDataListHistogram() {
        return _reportDataListHistogram;
    }

    /**
     * Resets the DataListTally and its DataList.
     */
    @Override
    public void reset() {
        super.reset();

        if (_list != null) {
            _list.clear();
        }

        _sorted = false;
    }

    /**
     * Should a histogram-reporter be used for the DataList?
     *
     * @param value boolean : true, if the DataList should be reported using a histogram-reporter, false if not.
     */
    public void setReportDataListHistogram(boolean value) {
        _reportDataListHistogram = value;
    }

    /**
     * Updates this <code>DataListTally</code> object by fetching the actual value of the <code>ValueSupplier</code> and
     * processing it. The
     * <code>ValueSupplier</code> is passed in the constructor of this
     * <code>DataListTally</code> object. This <code>update()</code> method complies
     * with the one described in DESMO, see [Page91].
     */
    @Override
    public void update() {
        super.update();
        internalUpdate(getLastValue());
    }

    /**
     * Updates this <code>DataListTally</code> object with the double value given as parameter. In some cases it might
     * be more convenient to pass the value this
     * <code>DataListTally</code> will be updated with directly within the
     * <code>update(double val)</code> method instead of going via the
     * <code>ValueSupplier</code>.
     *
     * @param val double : The value with which this <code>Tally</code> will be updated.
     */
    @Override
    public void update(double val) {
        super.update(val);
        internalUpdate(val);
    }

    /**
     * Implementation of the virtual <code>update(Observable, Object)</code> method of the <code>Observer</code>
     * interface. This method will be called automatically from an <code>Observable</code> object within its
     * <code>notifyObservers()</code> method. <br>
     * If no Object (a<code>null</code> value) is passed as arg, the actual value of the ValueSupplier will be fetched
     * with the <code>value()</code> method of the ValueSupplier. Otherwise it is expected that the actual value is
     * passed in the Object arg.
     *
     * @param o   java.util.Observable : The Observable calling this method within its own
     *            <code>notifyObservers()</code> method.
     * @param arg Object : The Object with which this <code>Tally</code> is updated. Normally a double number which is
     *            added to the statistics or
     *            <code>null</code>.
     */
    @Override
    public void update(Observable o, Object arg) {
        super.update(o, arg);
        internalUpdate(getLastValue());
    }

    /**
     * Internal method to update the DataList with a new sample.
     *
     * @param value double : The new sample.
     */
    private void internalUpdate(double value) {
        if (_sorted) {
            _sorted = false;
        }
        _list.add(value);
    }

    /**
     * A simple List for storing values passed to a Tally. This list is created to produce low overhead while
     * simulating.
     */
    public class DataList extends Reportable {

        /**
         *
         */
        protected DataList nextInStack = null;
        /**
         * The first Element in the list.
         */
        private Element _first = null;
        /**
         * The last Element in the list.
         */
        private Element _last = null;
        /**
         * The list's length.
         */
        private int _length = 0;

        /**
         * Constructor for a new DataList
         *
         * @param owner        Model : The model this DataList is associated to
         * @param name         java.lang.String : The name of this DataList object
         * @param showInReport boolean : Flag for showing the report about this DataList.
         * @param showInTrace  boolean : Flag for showing the trace output of this DataList.
         */
        public DataList(Model owner, String name, boolean showInReport, boolean showInTrace) {
            super(owner, name, showInReport, showInTrace);
        }

        /**
         * Adds an Element to the end of the DataList.
         *
         * @param value double : The value of the new Element in the list.
         */
        public void add(double value) {
            if (_first == null) {
                _first = _last = new Element(value);
            } else {
                _last.setNext(new Element(value));
                _last = _last.getNext();
            }

            _length++;
        }

        /**
         * Removes all Elements from this List.
         */
        public void clear() {
            _first = _last = null;
            _length = 0;
        }

        /**
         * Returns a Reporter to produce a report about this DataList.
         *
         * @return desmoj.report.Reporter : The Reporter for this DataList.
         */
        @Override
        public Reporter createDefaultReporter() {
            return new DataListHistogramReporter(getDataListSorted(), getMinimum(), getMaximum());
        }

        /**
         * Returns the list's first Element.
         *
         * @return the first Element or <code>null</code> if the list is empty.
         */
        public Element getFirst() {
            return _first;
        }

        /**
         * Returns the list's last Element.
         *
         * @return the last Element of <code>null</code> if the list is empty.
         */
        public Element getLast() {
            return _last;
        }

        /**
         * Returns the list's length.
         *
         * @return the length of this DataList.
         */
        public int getLength() {
            return _length;
        }

        /**
         * Returns the name of the Tally, this list belongs to.
         *
         * @return the Tally's name.
         */
        public String getTallyName() {
            return DataListTally.this.getName();
        }

        /**
         * Sorts the given list using a merge-sort algorithm
         *
         * @param list DataList : the list to be sorted.
         * @return The sorted list.
         */
        public DataList sort(DataList list) {

            String name = this.getName() + " (sorted)";

            if (list._length <= 1) {
                list.rename(name);

                if (_reportDataListHistogram) {
                    list.reportOn();
                    list.traceOn();
                }

                return list;
            }

            DataList stack = null;

            int pass = 0;
            int l = 0;

            while (getLength() > 0) {
                DataList tmp = stack;
                stack = popTwoSorted();
                stack.nextInStack = tmp;
                l++;

                int p = ++pass;

                while ((p & 1) == 0) {
                    stack = merge(stack);
                    l--;
                    p >>= 1;
                }
            }

            while (l > 1) {
                stack = merge(stack);
                l--;
            }

            stack.rename(name);

            if (_reportDataListHistogram) {
                stack.reportOn();
                stack.traceOn();
            }

            return stack;
        }

        /**
         * Creates a String containing all Elements of the list and its length.
         *
         * @return the list as String.
         */
        @Override
        public String toString() {
            String result = "[";
            Element e = _first;

            while (e != null) {
                result += e;

                if (e.getNext() != null) {
                    result += ", ";
                }

                e = e.getNext();
            }

            return result + "] (" + _length + ")";
        }

        /**
         * This Method is part of the sorting-algorithm. Merges two list on the list-stack to produce one sorted List
         * for them.
         *
         * @param stack DataList : A list to be merged with its predecessor on its list-stack.
         * @return A merged DataList created from given DataList and its predecessor on the list-stack.
         */
        private DataList merge(DataList stack) {
            DataList l1 = stack;
            DataList l2 = stack.nextInStack;

            DataList result = new DataList(getModel(), null, false, false);
            int l = l1.getLength() + l2.getLength();

            while (result.getLength() < l) {
                if (l1._first != null && (l2._first == null || l1._first.getValue() <= l2._first.getValue())) {
                    result.add(l1._first.getValue());
                    l1._first = l1._first.getNext();
                    l1._length--;
                } else {
                    result.add(l2._first.getValue());
                    l2._first = l2._first.getNext();
                    l2._length--;
                }
            }

            stack = result;
            stack.nextInStack = l2.nextInStack;

            return stack;
        }

        /**
         * This Method is part of the sorting-algorithm. Cuts the first two Elements from the list and sort them.
         *
         * @return A DataList containing the first two elements in a sorted order.
         */
        private DataList popTwoSorted() {
            DataList result = new DataList(getModel(), null, false, false);

            if (_first.getNext() == null) {
                result._first = result._last = _first;
                result._length = 1;
                _first = _last = null;
                _length = 0;

                return result;
            }

            if (_first.getValue() > _first.getNext().getValue()) {
                result._first = _first.getNext();
                result._last = _first;
            } else {
                result._first = _first;
                result._last = _first.getNext();
            }

            _first = _first.getNext().getNext();
            result._first.setNext(result._last);
            result._last.setNext(null);

            result._length = 2;
            _length = _length - 2;

            return result;
        }

        /**
         * A List's Element.
         */
        public class Element {
            /**
             * The next Element in the List.
             */
            private Element _next;

            /**
             * The value of this Element.
             */
            private double _value;

            /**
             * Constructs a new Element with the given value.
             *
             * @param value double : The value of this Element.
             */
            public Element(double value) {
                _value = value;
            }

            /**
             * Returns the next Element in the List.
             *
             * @return the next Element in the list of <code>null</code> if this is the last Element.
             */
            public Element getNext() {
                return _next;
            }

            /**
             * Returns the Element's value.
             *
             * @return the value of this Element.
             */
            public double getValue() {
                return _value;
            }

            /**
             * Sets the next Element in the list.
             *
             * @param next Element : The Element to be the successor of this Element.
             */
            public void setNext(Element next) {
                _next = next;
            }

            /**
             * Returns the Element's value as String.
             */
            @Override
            public String toString() {
                return "" + _value;
            }
        }
    }
}
