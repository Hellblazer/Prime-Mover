package desmoj.core.statistic;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

/**
 * <code>StatisticObjectSupportingTimeSpans</code> class is the super class of
 * the specific data collector classes able to handle <code>TimeSpan</code> objects. If the data collector should be
 * used to measure the lengths of
 * <code>TimeSpan</code>s, the method
 * <code>setShowTimeSpansInReport(true)</code> should be called to appropriately
 * assign the (reference) unit.
 *
 * @author Soenke Claassen
 * @author based on DESMO-C from Thomas Schniewind, 1998
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */

public abstract class StatisticObjectSupportingTimeSpans extends StatisticObject implements java.util.Observer {

    // ****** attributes ******

    /**
     * Flag, used to know if values should be printed as TimeSpans in report.
     */
    private boolean _showTimeSpansInReport = false;

    // ****** constructor ******

    /**
     * Constructor for a StatisticObjectSupportingTimeSpans, preliminarily without a unit assigned
     *
     * @param ownerModel   Model : The model this StatisticObject is associated to.
     * @param name         java.lang.String : The name of this StatisticObject
     * @param showInReport boolean : Flag for showing the report about this StatisticObject.
     * @param showInTrace  boolean : Flag for showing this StatisticObject in trace files.
     */
    public StatisticObjectSupportingTimeSpans(Model ownerModel, String name, boolean showInReport,
                                              boolean showInTrace) {
        super(ownerModel, name, showInReport, showInTrace);
    }

    // ****** methods ******

    /**
     * Are values printed as TimeSpans in the report?
     *
     * @return true if values are printed as TimeSpans, false if not.
     */
    public boolean getShowTimeSpansInReport() {
        return _showTimeSpansInReport;
    }

    @Override
    /**
     * {@inheritDoc}
     */ public String getUnit() {
        String unit = "";
        // Using time-based output, no unit explicitly assigned
        if (this.getShowTimeSpansInReport() && (super.getUnit() == null || super.getUnit().length() == 0)
        && TimeOperations.getTimeFormatter() != null) {
            unit = TimeOperations.getTimeFormatter().getUnit();
        } else {
            // Otherwise, use the unit explicitly set
            unit = super.getUnit();
        }
        return unit;
    }

    /**
     * Sets if values should be interpreted and printed as TimeSpans (subject to the experiment's reference unit) in the
     * Report. Unless a unit is explicitly otherwise using <code>setUnit(...)</code>, this class assumes updates refer
     * to the reference time unit.
     *
     * @param value boolean : true, if values should be printed as TimeSpans, false if not.
     */
    public void setShowTimeSpansInReport(boolean value) {
        _showTimeSpansInReport = value;
    }

    /**
     * Updates this data collector object with a specific <code>TimeSpan</code>.
     *
     * @param t TimeSpan : The time span to update this data collector.
     */
    public abstract void update(TimeSpan t);
} // end class StatisticObjectSupportingTimeSpans
