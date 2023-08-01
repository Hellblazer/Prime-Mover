package desmoj.core.report.html5chart;

/**
 * A chart to represent <code>Long</code> data on the y-axis of a coordinate system.
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
public abstract class AbstractNumericalCoorChartCanvasLong extends AbstractNumericalCoorChartCanvas<Long> {
	
	public AbstractNumericalCoorChartCanvasLong(String canvasName, int canvasHeight, int canvasWidth, AbstractChartData<Long> data) {
		super(canvasName, canvasHeight, canvasWidth, data);
	}
	
	/**
	 * Determines the number of scales on the y-axis and the difference between each scale.
	 */
	void determineYScale() {
	        
		long maxEntry = this.chartData.getHighestDataValue().longValue();
	
		if (maxEntry <= 10) {
	        _numOfYScale = maxEntry;
	        _yScale = 1l;		    
	        return;
		}
		
		int[]    hight_candidates       = {12, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 100};
		int[]    hight_candidates_ticks = {6,   3, 10,  5,  6,  8, 10,  6,  7,  8,  9, 10};
		
		int power = (int) Math.log10(maxEntry)-1;
		int base = (int) Math.pow(10, power);
		int candidate = 0;
		
	    for (candidate = 0; candidate < hight_candidates.length; candidate++) {
	        if (base * hight_candidates[candidate] > maxEntry) break;		        		        
	    }
		
        _numOfYScale = hight_candidates_ticks[candidate];
        _yScale = (long) (base * hight_candidates[candidate] / hight_candidates_ticks[candidate]);
        
        while (_numOfYScale <= 5 && _yScale % 10 == 0) {
            _numOfYScale *= 2;
            _yScale /= 2;
        }
	}
}
