package desmoj.core.report.html5chart;

/**
 * A chart to represent <code>Double</code> data on the y-axis of a coordinate system.
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
public abstract class AbstractNumericalCoorChartCanvasDouble extends AbstractNumericalCoorChartCanvas<Double> {
	
	public AbstractNumericalCoorChartCanvasDouble(String canvasName, int canvasHeight, int canvasWidth, AbstractChartData<Double> data) {
		super(canvasName, canvasHeight, canvasWidth, data);
	}

	/**
	 * Determines the number of scales on the y-axis and the difference between each scale.
	 */
	void determineYScale() {
		double maxEntry = this.chartData.getHighestDataValue().doubleValue();
	
		if (maxEntry <= 1) {
	        _numOfYScale = 1;
	        _yScale = 1.0;		    
	        return;
		}
		
        double[] hight_candidates       = {1.2, 1.4, 1.5, 1.8, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        int[]    hight_candidates_ticks = {6,   7,   6,   9,   10,  10,  6,   7,   8,   9,   10,  6,   7,   8,   9,   10 };
        
        int power = (int) Math.log10(maxEntry);
        int candidate = 0;
        
        for (candidate = 0; candidate < hight_candidates.length; candidate++) {
            if (Math.pow(10, power) * hight_candidates[candidate] > maxEntry) break;                                
        }
        
        _numOfYScale = hight_candidates_ticks[candidate];
        _yScale = Math.pow(10, power) * hight_candidates[candidate] / hight_candidates_ticks[candidate];
    }
}
