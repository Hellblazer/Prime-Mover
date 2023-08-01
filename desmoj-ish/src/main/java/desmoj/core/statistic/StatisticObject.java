package desmoj.core.statistic;

import desmoj.core.simulator.Model;

/**
 * <code>StatisticObject</code> class is the super class of all
 * other classes collecting statistical data. <br>
 * It extends the <code>desmoj.core.simulator.Reportable</code> class so that it can provide
 * a reporter to represent the statistical data in the report. <br>
 * It also implements the <code>java.util.Observer</code> interface. So this
 * is the observer part of the observer pattern as described in [Gamm95] that is
 * observing the <code>desmoj.statistic.ValueSupplier</code>. Whenever the
 * <code>ValueSupplier</code> is changing it calls the
 * <code>update(Observable, Object)</code> method of this
 * <code>StatisticObject</code>. Which happens inside the
 * <code>notifyObservers()</code> method of the observable
 * <code>ValueSupplier</code>.
 * <p>
 * 
 * The virtual method <code>update(Observable, Object)</code> has to be
 * implemented in all derived classes so that the <code>StatisticObject</code>
 * is updated when the observed <code>ValueSupplier</code> has changed and its
 * <code>notifyObservers()</code> method is called from its
 * <code>notifyStatistics()</code> method.
 * <p>
 * 
 * @see desmoj.core.statistic.ValueSupplier
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

public abstract class StatisticObject extends desmoj.core.simulator.Reportable
		implements java.util.Observer {

	// ****** attributes ******
    
    /**
	 * Represents the value returned in case of an error. If no valid value can
	 * be returned.
	 */
	public static final double UNDEFINED = -1.0;

	/**
	 * The number of digits after the decimal point which will be displayed for
	 * the numbers in the reports. A precision of more than that is obsolete.
	 */
	protected static final double FRACTION_DIGITS = 5.0;

	/**
	 * The number needed for rounding the results to the desired precision.
	 */
	protected static final double PRECISION = java.lang.Math.pow(10.0,
			FRACTION_DIGITS);

	/**
     * Rounds a double value with repect to the <code>PRECISION</code>.
     * 
     * @return double : The rounded value.
     * @param d
     *           double : The value to be rounded
     */
    public static double round(double d) {
        return java.lang.Math.rint(PRECISION * d) / PRECISION;
    }

	// ****** methods ******

	/**
     * A unit (optional) / 21.11.12 extended bx cm
     */
    private String unit;

	/**
	 * Constructor for a StatisticObject, preliminarily without a unit assigned
	 * 
	 * @param ownerModel
	 *            Model : The model this StatisticObject is associated to.
	 * @param name
	 *            java.lang.String : The name of this StatisticObject
	 * @param showInReport
	 *            boolean : Flag for showing the report about this
	 *            StatisticObject.
	 * @param showInTrace
	 *            boolean : Flag for showing this StatisticObject in trace
	 *            files.
	 */
	public StatisticObject(Model ownerModel, String name, boolean showInReport,
			boolean showInTrace) {
		super(ownerModel, name, showInReport, showInTrace);
	}

	/**
     * Get an optional unit of reported value. Default is null.
     * This value is shown in description reports.
     * @return String : The optional unit
     */
    public String getUnit(){
        
        return this.unit;
    }

    /**
     * Textually wraps the output of <code>getUnit()</code>, adding
     * brackets and displaying "none" if case unit is <code>null</code>.
     * @return
     */
    public String getUnitText(){
        String unit = this.getUnit();
        if(unit == null || unit.length() == 0) 
            return "none";
        else
            return "["+unit+"]";
    }
    
    //   Extension by Chr. M&uuml;ller (TH Wildau) 28.11.12 -------------------------------------
    
    /**
     * Set an optional unit of reported value. Default is null.
     * This value is shown in description reports.
     */
    public void setUnit(String unit){
        this.unit           = unit;
    }

    /**
	 * Converts an <code>Object</code> to a <code>double</code> -value. If
	 * the given object is not an instance of a number wrapper class a warning
	 * is produced and the UNDEFINED-value (-1.0) is returned. <br>
	 * So the <code>StatisticObject</code> can be notified by a
	 * <code>ValueSupplier</code> with the
	 * <code>notifyStatistics(Object arg)</code> method, where the value is
	 * passed as an object in this method.
	 * 
	 * @return double : The double value the given object is converted to.
	 * @param obj
	 *            Object : The Object which will be converted to a double value.
	 */
	protected double convertToDouble(Object obj) {
		if (obj == null) // the given object is a null pointer
		{
			sendWarning("Attempt to convert a null pointer to a double value."
					+ " Zero (0.0) is returned!", "StatisticObject: "
					+ getName() + " Method: double convertToDouble"
					+ "(Object obj)",
					"A null pointer can not be converted to a double value. Zero is "
							+ "assumed and will be returned.",
					"Make sure not to pass a null pointer where an Object is expected.");

			return 0.0; // that makes sense (doesn't it?)
		}

		if (obj instanceof Byte) {
			return ((Byte) obj).doubleValue();
		}

		if (obj instanceof Short) {
			return ((Short) obj).doubleValue();
		}

		if (obj instanceof Integer) {
			return ((Integer) obj).doubleValue();
		}

		if (obj instanceof Long) {
			return ((Long) obj).doubleValue();
		}

		if (obj instanceof Float) {
			return ((Float) obj).doubleValue();
		}

		if (obj instanceof Double) {
			return ((Double) obj).doubleValue();
		}

		// the given object is not an instance of a number wrapper class
		sendWarning(
				"Attempt to convert an object which is not a number wrapper"
						+ " class to a double value. The UNDEFINED value (-1.0) is returned!",
				"StatisticObject: " + getName()
						+ " Method: double convertToDouble" + "(Object obj)",
				"The given Object is an instance of the class: "
						+ obj.getClass().toString()
						+ ". This can not be converted to a double.",
				"Make sure to use a number wrapper class for numeric values.");

		return UNDEFINED; // return (-1.0) in case of an error
	}
    
    /**
	 * Leaves a message in the trace that this StatisticObject has been updated.
	 */
	protected void traceUpdate() {
		if (currentlySendTraceNotes()) {
			sendTraceNote("updates " + this.getQuotedName());
		} // tell in the trace which StatisticObject is updated
	}
} // end class StatisticObject
