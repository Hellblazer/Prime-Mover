package desmoj.core.report.html5chart;

import desmoj.core.simulator.Model;

/**
 * A chart data table with data values from type <code>N</code>.
 *
 * @author Johanna Djimandjaja
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public abstract class AbstractChartDataTable<N extends Number> implements AbstractChartData<N> {

    /** The owner Model this chart data is associated to. */
    protected Model ownerModel;

    /** The data values. */
    protected N[] table;

    /**
     * Returns the data value at a given index. If the data doesn't exist,
     * <code>null</code> will be returned.
     *
     * @param index
     * @return N: the data value at a given index
     */
    @Override
    public N getDataValue(int index) {
        if (index < 0 || index >= table.length) {
            return null;
        } else {
            return table[index];
        }
    }

    /**
     * Returns the highest data value. If there is no entry or the highest data value is a negative number, null will be
     * returned. Entries of
     * <code>Double.POSITIVE_INFINITY</code> will be ignored.
     *
     * @return N: the highest data value for this chart
     */
    @Override
    public N getHighestDataValue() {
        N max = null;
        for (N entry : table) {
            if ((max == null || entry.doubleValue() > max.doubleValue())
            && entry.doubleValue() != Double.POSITIVE_INFINITY) {
                max = entry;
            }
        }

        return max;
    }

    /**
     * Returns the number of data for this chart.
     *
     * @return int: the number of data for this chart
     */
    @Override
    public int getNumOfData() {
        return table.length;
    }

    @Override
    public Model getOwnerModel() {
        return this.ownerModel;
    }

    /**
     * Returns whether this chart data is empty or not.<br> Returns <code>true</code> if
     * <code>this.getNumOfData()==0</code>, else returns <code>false</code>.
     *
     * @return boolean: whether this chart data is empty or not
     */
    @Override
    public boolean isEmpty() {
        return (getNumOfData() == 0);
    }

}
