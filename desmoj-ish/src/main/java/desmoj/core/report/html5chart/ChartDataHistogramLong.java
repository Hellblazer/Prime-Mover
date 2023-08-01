package desmoj.core.report.html5chart;

import desmoj.core.simulator.Model;

/**
 * A chart data for a Text Histogram.<br>
 * Data of a chart, which display a relationship between two variables:
 * Strings and values from type <code>Long</code>.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Johanna Djimandjaja
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
 */
public class ChartDataHistogramLong extends AbstractChartDataTable<Long> {
	
	private String[] _observedStrings;

	/**
	 * Create an Object to represent the chart data with
	 * data values from type long and data classes from type String.
	 * @param ownerModel
	 * 			Model : The owner model this chart data is associated to.
	 * @param strings
	 * 			String[] : The Strings representing the cells.
	 * @param entries
	 * 			Long[] : The data value of each cell.
	 */
	public ChartDataHistogramLong(Model ownerModel, String[] strings, Long[] entries) {
		this.ownerModel = ownerModel;
		//the length of the strings array and the entries array must be the same.
		int numOfObs = entries.length;
		if (numOfObs == strings.length) {
			_observedStrings = strings;
			table = entries;
		} else {
			if (numOfObs < strings.length) {
				table = entries;
				_observedStrings = new String[numOfObs];
				for (int i = 0; i < numOfObs; i++) {
					_observedStrings[i] = strings[i];
				}
			} else {
				_observedStrings = strings;
				numOfObs = strings.length;
				table = new Long[numOfObs];
				for (int i = 0; i < numOfObs; i++) {
					table[i] = entries[i];
				}
			}
		}
	}
	
	/**
	 * Returns the String at the given index.
	 * @param index
	 * @return
	 * 			String: the String at the given index
	 */
	public String getObservedString(int index) {
		if (index < 0 || index >= _observedStrings.length) return "";
		return _observedStrings[index];
	}
}
