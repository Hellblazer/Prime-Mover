package desmoj.core.report.html5chart;

import java.util.List;

import desmoj.core.simulator.Model;

/**
 * A chart data for a Time Series.<br>
 * Data of a chart, which display a relationship between two variables from type
 * <code>Double</code>.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Johanna Djimandjaja
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
 */
public class ChartDataTimeSeries extends AbstractChartDataTable<Double> {

    /**
     * List for saving the time values in the memory.
     */
    private Double[] _timeValues;

    /**
     * Group seperate output of different TimeSeries, if desired.
     */
    private String group;

    /**
     * Start of the interval of the time series.
     */
    private Double validFrom;

    /**
     * End of the interval of the time series.
     */
    private Double validTo;

    /**
     * Create an Object to represent the data for a TimeSeries.
     *
     * @param ownerModel Model : The owner model this chart data is associated to.
     * @param timeValues java.util.List : List for saving the time values in the
     *                   memory.
     * @param dataValues java.util.List : List for saving the data values in the
     *                   memory.
     * @param validFrom  Double : Start of the interval of the time series.
     * @param validTo    Double : End of the interval of the time series.
     * @param group      String : An ID to separate different plots into different
     *                   diagrams, if desired.
     */
    public ChartDataTimeSeries(Model ownerModel, List<Double> timeValues, List<Double> dataValues, Double validFrom,
                               Double validTo, String group) {
        this.ownerModel = ownerModel;
        if (dataValues == null || timeValues == null) {
            table = new Double[0];
            _timeValues = new Double[0];
        } else {
            // the length of dataValues and timeValues must be the same
            int numOfData = dataValues.size();
            if (numOfData > timeValues.size()) {
                numOfData = timeValues.size();
            }
            table = new Double[numOfData];
            _timeValues = new Double[numOfData];
            for (int i = 0; i < table.length; i++) {
                table[i] = dataValues.get(i);
                _timeValues[i] = timeValues.get(i);
            }
        }
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.group = group;
    }

    /**
     * Return an array of data values.
     *
     * @return an array of data values
     */
    public Double[] getDataValues() {
        return table;
    }

    /**
     * Returns the group ID value.
     *
     * @return the group ID value
     */
    public String getGroup() {
        return group;
    }

    /**
     * Returns an array of time values.
     *
     * @return an array of time values
     */
    public Double[] getTimeValues() {
        return _timeValues;
    }

    /**
     * Returns the lowest possible time value.
     *
     * @return the lowest possible time value
     */
    public double getValidFrom() {
        return validFrom;
    }

    /**
     * Returns the highest possible time value.
     *
     * @return the highest possible time value
     */
    public double getValidTo() {
        return validTo;
    }
}
