package desmoj.core.report.html5chart;

import java.awt.Color;

/**
 * A general canvas representing the area to display drawings.
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
public interface Canvas {
	/**
	 * Gets the height of the canvas.
	 * 
	 * @return
	 * 			int : The height of the canvas.
	 */
	int getCanvasHeight();
	
	/**
	 * Returns the ID of the canvas.
	 * @return
	 */
	String getCanvasID();
	
	/**
	 * Gets the width of the canvas.
	 * @return
	 * 			int : The width of the canvas.
	 */
	int getCanvasWidth();

	/**
	 * Returns the default color.
	 * 
	 * @return
	 */
	Color getDefaultColor();

}
