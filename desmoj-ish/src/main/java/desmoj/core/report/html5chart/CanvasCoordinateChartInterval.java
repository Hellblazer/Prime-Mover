package desmoj.core.report.html5chart;

/**
 * A canvas to display a chart with its data divided into sections of an interval. 
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
public interface CanvasCoordinateChartInterval<N extends Number> extends CanvasCoordinateChart<N> {

	/**
	 * Returns the lower limit of the range of the data at the given index.
	 * @param dataIndex
	 * @return
	 */
	double getLowerLimit(int dataIndex);

	/**
	 * Returns the upper limit of the range of the data at the given index.
	 * @param dataIndex
	 * @return
	 */
	double getUpperLimit(int dataIndex);
	
	

}
