package desmoj.core.report;

/**
 * 
 * Use this class to create ASCII formatted Simulation Error Output.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Gunnar Kiesel
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

public class ASCIIErrorOutput extends ErrorFileOut implements OutputType {

	/** Create a new ASCIIDErrorOutput class * */
	public ASCIIErrorOutput() {
		super(1, "desmoj.core.report.ASCIITableFormatter");
	}

	/***************************************************************************
	 * Create a new ASCIIDErrorOutput class
	 * 
	 * @param simTimeFloatingDigits
	 *            int: The number of floating point digits of the simulation
	 *            time values to be displayed
	 **************************************************************************/
	public ASCIIErrorOutput(int simTimeFloatingDigits) {
		super(simTimeFloatingDigits, "desmoj.core.report.ASCIITableFormatter");
	}

	/** returns the file appendix for this output type * */
	public String getAppendix() {
		return ".txt";
	}

	/***************************************************************************
	 * set the time of the current output
	 * 
	 * @param timeFloats
	 *            int: time of the curent output
	 **************************************************************************/
	public void setTimeFloats(int timeFloats) {
		this.formatter.setTimePrecision(timeFloats);
	}

}