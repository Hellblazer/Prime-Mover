package desmoj.core.report.html5chart;

import desmoj.core.simulator.Model;

/**
 * An interface for chart data with a numerical data type.
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
public interface AbstractChartData<N extends Number> {
	
	/**
	 * Should return the data value at a given index.
	 * If the data doesn't exist, <code>0</code> should be returned.
	 * @param index
	 * @return
	 * 			N: the data value at a given index
	 */
	N getDataValue(int index);
	
	/**
	 * Should return the highest data value.
	 * If there is no entry or the highest data value is a negative number,
	 * 0 should be returned.
	 * 
	 * @return
	 * 			N: the highest data value for this chart
	 */
	N getHighestDataValue();
	
	/**
	 * Should return the number of data for this chart.
	 * @return
	 * 			int: the number of data for this chart
	 */
	int getNumOfData();

	/**
	 * Should return the owner Model this chart data is associated to.
	 * @return
	 * 			desmoj.core.simulator.Model : The model this chart data is associated to.
	 */
	Model getOwnerModel();
	
	/**
	 * Should return whether this chart data is empty or not.<br>
	 * Should return <code>true</code> if <code>this.getNumOfData()==0</code>,
	 * else should return <code>false</code>.
	 * 
	 * @return
	 * 			boolean: whether this chart data is empty or not
	 */
	boolean isEmpty();

}

