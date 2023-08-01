package desmoj.core.report;

import java.awt.Color;
import java.util.HashSet;

import desmoj.core.report.html5chart.Canvas;
import desmoj.core.report.html5chart.CanvasCoordinateChart;
import desmoj.core.report.html5chart.CanvasCoordinateChartInterval;
import desmoj.core.report.html5chart.CanvasHistogramDouble;
import desmoj.core.report.html5chart.CanvasHistogramLong;
import desmoj.core.report.html5chart.CanvasTimeSeries;

/**
 * This class helps writing the JavaScript codes to draw charts.
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
public class JavaScriptFormatter {
	
	/** The canvas that should be drawn using java script. */
	private Canvas _canvas;
	
	/**	A set containing Strings, that represents the functions that should be written on the script. */
	private HashSet<String> _functionsToWrite;
	
	/** The FileOutput this java script writer writes to. */
	private FileOutput _out;
	
	public JavaScriptFormatter() {
		_functionsToWrite = new HashSet<String>();		
	}

	/**
	 * Sets an output file to write the script to
	 * 
	 * @param out
	 *            desmoj.report.FileOutput
	 */
	public void setOutput(FileOutput out) {
		_out = out;
	}
	
	public void writeDrawingScript(Canvas canvas) {
		_canvas = canvas;		
		_out.writeln("var myCanvas = document.getElementById('" + _canvas.getCanvasID() + "');");
		_out.writeln("var myContext = myCanvas.getContext('2d');");
		
		this.setDefaultColor();
		
		if (_canvas instanceof CanvasCoordinateChart) {

			this.drawXScale();
			this.drawYScale();
			this.drawXAxis();
			this.drawYAxis();
					
			if (_canvas instanceof CanvasTimeSeries) this.drawTimeSeries();
			else {
				if (_canvas instanceof CanvasHistogramLong ||
					_canvas instanceof CanvasHistogramDouble) {
					this.drawVerticalBars();
					this.drawTextLabels();
				}
			}
			
			this.drawCanvasBorder();
			
			this.writeFunctions();
			this.reset();
			
		}		
	}
	
	/**
	 * Writes the code to draw the canvas border into the JavaScript.
	 */
	private void drawCanvasBorder() {
		_out.writeln("");	
		//draw the border of the canvas
		_out.writeln("myContext.strokeRect(0, 0, myCanvas.width, myCanvas.height);");
	}

	private void drawTextLabels() {
		_out.writeln("drawTextLabels();");
		_functionsToWrite.add("drawTextLabels");
	}

	/**
	 * Writes the code to draw the TimeSeries into the JavaScript.
	 */
	private void drawTimeSeries() {
		_out.writeln("drawTimeSeries();");
		_functionsToWrite.add("drawTimeSeries");
	}

	/**
	 * Writes the code to draw the vertical bars into the JavaScript.
	 */
	private void drawVerticalBars() {
		_out.writeln("drawVBars();");
		_functionsToWrite.add("drawVBars");
	}

	/**
	 * Writes the code to draw the x-axis and
	 * to put the label for the x-axis into the JavaScript.
	 */
	private void drawXAxis() {
		_out.writeln("drawXAxis();");
		_functionsToWrite.add("drawXAxis");
	}

	/**
	 * Writes the code to draw the scales of the x-axis and
	 * to write the labels for the scales into the JavaScript.
	 */
	private void drawXScale() {
		_out.writeln("drawXScale();");
		_functionsToWrite.add("drawXScale");
	}

	/**
	 * Writes the code to draw the y-axis and
	 * to put the label for the y-axis into the JavaScript.
	 */
	private void drawYAxis() {
		_out.writeln("drawYAxis();");
		_functionsToWrite.add("drawYAxis");
	}

	/**
	 * Writes the code to draw the scales of the y-axis and
	 * to write the labels for the scales into the JavaScript.
	 */
	private void drawYScale() {
		_out.writeln("drawYScale();");
		_functionsToWrite.add("drawYScale");
	}

	/**
	 * Changes a given number into a 2-digit hexadecimal string.<br>
	 * Numbers less then 0 will be changed to the String 00 and numbers higher then 255 will be changed to FF.
	 * @param number
	 * 			The number, that should be changed into hexadecimal.
	 * @return
	 * 			String : The given number as a 2-digit hexadecimal string.
	 */
	private String intToHexString(int number) {
		if (number <= 0) return "00";
		if (number >= 255) return "FF";
		String hex;
		if (number < 16) {
			hex = "0" + Integer.toHexString(number);
		} else {
			hex = Integer.toHexString(number);
		}
		return hex;
	}

	private void reset() {
		_canvas = null;
		_functionsToWrite.clear();		
	}

	/**
	 * Writes the code to set the default color used in for the drawings.
	 */
	private void setDefaultColor() {
		_out.writeln("");
		//default strokeStyle
		_out.writeln("myContext.strokeStyle = '"+ this.toHexString(_canvas.getDefaultColor()) + "';");
	}

	/**
	 * Returns the hexadecimal notation for the given color.<br>
	 * The notation combines the red, green, and blue color values (RGB) in hexadecimal form.
	 * The lowest value that can be given to one of the light sources is 0 (hex 00). The highest value is 255 (hex FF).<br>
	 * When the given color is <code>null</code> returns <code>#000000</code>.
	 * @param color
	 * 			The color to be coded.
	 * @return
	 * 			String : The hexadecimal notation for the given color.
	 */
	private String toHexString(Color color) {
		String hex = "";
		if (color == null) hex = "#000000";
		else {
			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();
			hex = "#" + intToHexString(r) + intToHexString(g) + intToHexString(b);
		}	
		return hex;
	}

	/**
	 * Writes the data, that should be represented by the chart into the JavaScript.
	 */
	private void writeData() {
		if (_canvas instanceof CanvasHistogramLong || _canvas instanceof CanvasHistogramDouble) {	
			String dataString = "";
			if (_canvas instanceof CanvasHistogramLong) {
			    CanvasHistogramLong canvas = (CanvasHistogramLong) _canvas;
				dataString = "" + canvas.getDataValue(0);
				for(int i = 1; i < canvas.getNumOfData(); i++) {
					dataString = dataString + ", " + canvas.getDataValue(i);
				}
			} else if (_canvas instanceof CanvasHistogramDouble) {
			    CanvasHistogramDouble canvas = (CanvasHistogramDouble) _canvas;					
					for(int i = 0; i < canvas.getNumOfData(); i++) {
						double dataValue = canvas.getDataValue(i);
						if (dataValue == Double.POSITIVE_INFINITY) {
							//dataValue should then be so high that the chart will reach the top border of the canvas
							dataValue = (double)(canvas.getChartHeight()+canvas.getTopGap())/ canvas.getChartHeight() * canvas.getNumOfYScale()*canvas.getYScale();
						}
						if (i == 0) dataString = "" + dataValue;
						else dataString = dataString + ", " + dataValue;
				}				
			} // end else
			
			//write all data values into an array
			_out.writeln("	var dataValues = [" + dataString + "];");			
		} //end (_canvas instanceof AbstractLongCoorChartCanvas || _canvas instanceof AbstractDoubleCoorChartCanvas)
		
		if (_canvas instanceof CanvasCoordinateChartInterval) {
			CanvasCoordinateChartInterval<?> intervalCanvas = (CanvasCoordinateChartInterval<?>) _canvas;
			String lowerLimitsString = "";
			String upperLimitsString = "";
			for(int i = 0; i < intervalCanvas.getNumOfData(); i++) {
				double lowerLimit = intervalCanvas.getLowerLimit(i);
				double upperLimit = intervalCanvas.getUpperLimit(i);
				
				if (lowerLimit == Double.NEGATIVE_INFINITY) lowerLimit = intervalCanvas.getStartXScale().doubleValue();
				if (upperLimit == Double.POSITIVE_INFINITY)
					upperLimit = intervalCanvas.getXScale().doubleValue()*intervalCanvas.getNumOfXScale()+intervalCanvas.getStartXScale().doubleValue();
				
				if (i == 0) {
					lowerLimitsString = lowerLimitsString + lowerLimit;
					upperLimitsString = upperLimitsString + upperLimit;						
				} else {
					lowerLimitsString = lowerLimitsString + ", " + lowerLimit;
					upperLimitsString = upperLimitsString + ", " + upperLimit;
				}
			} // end for
			
			//write the lower and upper limits for each cell
			_out.writeln("	var lowerLimits = [" + lowerLimitsString + "];");
			_out.writeln("	var upperLimits = [" + upperLimitsString + "];");				
		} // end if (_canvas instanceof CanvasIntervalCoorChart)
		
		if (_canvas instanceof CanvasTimeSeries) {
			CanvasTimeSeries tsCanvas = (CanvasTimeSeries) _canvas;
			
			Double[] dataValueBuff;
			Double[] timeValueBuff;
			String dataString = "";
			String timeString = "";
			
			for (int i = 0; i < tsCanvas.getNumOfTimeSeries(); i++) {					
				if (i == 0) {
					dataString = dataString + "[";
					timeString = timeString + "[";
				} else {
					dataString = dataString + ", [";
					timeString = timeString + ", [";						
				}
				
				dataValueBuff = tsCanvas.getDataValues(i);
				timeValueBuff = tsCanvas.getTimeValues(i);
				
				int numOfPair;
				if (dataValueBuff.length <= timeValueBuff.length) numOfPair = dataValueBuff.length;
				else numOfPair = timeValueBuff.length;
				
				// write data and time for one TimeSeries
				for (int j = 0; j < numOfPair; j++) {						
					if (j != 0) {
						dataString = dataString + ", ";
						timeString = timeString + ", ";							
					}
					dataString = dataString + dataValueBuff[j];
					timeString = timeString + timeValueBuff[j];
				}
				
				dataString = dataString + "]";
				timeString = timeString + "]";
			}
			//write all data into an array
			_out.writeln("	var dataValues = [" + dataString + "];");
			_out.writeln("	var timeValues = [" + timeString + "];");			
		} // end if (_canvas instanceof CanvasTimeSeries)
		
	}

	private void writeFuncDrawTextLabels() {
		_out.writeln("");
		_out.writeln("function drawTextLabels() {"); //function start
		if (_canvas instanceof CanvasHistogramLong && ((CanvasHistogramLong) _canvas).getNumOfData() != 0) {
		    CanvasHistogramLong thCanvas = (CanvasHistogramLong) _canvas;

			//the vertical position of the scale text
			int verTextPos = (thCanvas.getCanvasHeight()-thCanvas.getBottomGap()+10);
			
			String labelsString = "'" + thCanvas.getText(0) + "'";
			for (int i = 1; i < thCanvas.getNumOfData(); i++) {
				labelsString = labelsString + ", '" + thCanvas.getText(i) + "'";
			}			
			
			_out.writeln("	var labels = [" + labelsString + "];");
			_out.writeln("	var numOfData = " + thCanvas.getNumOfData() + ";");
			_out.writeln("	var barWidth = 4/5*" + thCanvas.getChartWidth() + " /numOfData;");
			_out.writeln("	var barGap = 1/5*" + thCanvas.getChartWidth() + "/(numOfData+1);");
			
			_out.writeln("");
			//write text for the bars the x-axis
			_out.writeln("	myContext.save();");
			_out.writeln("	myContext.textAlign = 'center';");
			_out.writeln("	myContext.textBaseline = 'top';");
			_out.writeln("	for (var i=0; numOfData>i; i++) {");

			_out.writeln("		var horTextPos = (i+1)*barGap + (i+0.5)*barWidth + " + thCanvas.getLeftGap() + ";");
			_out.writeln("		myContext.fillText(labels[i], horTextPos, " + verTextPos + ", barWidth);");

			_out.writeln("	}");
			_out.writeln("	myContext.restore();");			
		}
        if (_canvas instanceof CanvasHistogramDouble && ((CanvasHistogramDouble) _canvas).getNumOfData() != 0) {
            CanvasHistogramDouble thCanvas = (CanvasHistogramDouble) _canvas;

            //the vertical position of the scale text
            int verTextPos = (thCanvas.getCanvasHeight()-thCanvas.getBottomGap()+10);
            
            String labelsString = "'" + thCanvas.getText(0) + "'";
            for (int i = 1; i < thCanvas.getNumOfData(); i++) {
                labelsString = labelsString + ", '" + thCanvas.getText(i) + "'";
            }           
            
            _out.writeln("  var labels = [" + labelsString + "];");
            _out.writeln("  var numOfData = " + thCanvas.getNumOfData() + ";");
            _out.writeln("  var barWidth = 4/5*" + thCanvas.getChartWidth() + " /numOfData;");
            _out.writeln("  var barGap = 1/5*" + thCanvas.getChartWidth() + "/(numOfData+1);");
            
            _out.writeln("");
            //write text for the bars the x-axis
            _out.writeln("  myContext.save();");
            _out.writeln("  myContext.textAlign = 'center';");
            _out.writeln("  myContext.textBaseline = 'top';");
            _out.writeln("  for (var i=0; numOfData>i; i++) {");

            _out.writeln("      var horTextPos = (i+1)*barGap + (i+0.5)*barWidth + " + thCanvas.getLeftGap() + ";");
            _out.writeln("      myContext.fillText(labels[i], horTextPos, " + verTextPos + ", barWidth);");

            _out.writeln("  }");
            _out.writeln("  myContext.restore();");         
        }		
		_out.writeln("}"); //function end
	}

	/**
	 * Writes the function to draw the TimeSeries.
	 */
	private void writeFuncDrawTimeSeries() {
		_out.writeln("");
		_out.writeln("function drawTimeSeries() {"); //function start
		
		if (_canvas instanceof CanvasTimeSeries) {
			this.writeData();
			CanvasTimeSeries tsCanvas = (CanvasTimeSeries) _canvas;

			
			String tsColorString = "'" + toHexString(tsCanvas.getDataColor(0)) + "'";		
			for(int i = 1; i < tsCanvas.getNumOfTimeSeries(); i++) {
				tsColorString = tsColorString + ", '" +
						this.toHexString(tsCanvas.getDataColor(i)) + "'";
			}
			
			_out.writeln("	var colors = [" + tsColorString + "];");
			_out.writeln("	var startXScale = " + tsCanvas.getStartXScale() + ";");
			_out.writeln("	var xScale = " + tsCanvas.getXScale() + ";");
			_out.writeln("	var numOfXScale = " + tsCanvas.getNumOfXScale() + ";");
			_out.writeln("	var yScale = " + tsCanvas.getYScale() + ";");
			_out.writeln("	var numOfYScale = " + tsCanvas.getNumOfYScale() + ";");		
			_out.writeln("	var chartHeight = " + tsCanvas.getChartHeight() + ";");
			_out.writeln("	var chartWidth = " + tsCanvas.getChartWidth() + ";");
					
			_out.writeln("");
			_out.writeln("	myContext.save();");
			_out.writeln("	for (var i=0; dataValues.length>i; i++) {");
			_out.writeln("		var data = dataValues[i];");
			_out.writeln("		var time = timeValues[i]");
			_out.writeln("		var xCoor = (time[0]-startXScale) * chartWidth/(xScale*numOfXScale);");
			_out.writeln("		var yCoor = data[0] * chartHeight/(yScale*numOfYScale);");
			
			_out.writeln("");
			_out.writeln("		myContext.strokeStyle = colors[i];");
			_out.writeln("		myContext.beginPath();");
			_out.writeln("		myContext.moveTo(xToCanvasCoordinate(xCoor), yToCanvasCoordinate(yCoor));");
			
			_out.writeln("		for (var j=1; data.length>j; j++) {");
			_out.writeln("			var newXCoor = (time[j] - startXScale) * chartWidth/(xScale*numOfXScale);");
			_out.writeln("			var newYCoor = data[j] * chartHeight/(yScale*numOfYScale);");
			_out.writeln("			myContext.lineTo(xToCanvasCoordinate(newXCoor), yToCanvasCoordinate(yCoor));");
			_out.writeln("			myContext.lineTo(xToCanvasCoordinate(newXCoor), yToCanvasCoordinate(newYCoor));");
			_out.writeln("			xCoor = newXCoor;");
			_out.writeln("			yCoor = newYCoor;");
			_out.writeln("		}");
			_out.writeln("		myContext.stroke();");
			_out.writeln("		myContext.closePath();");
			_out.writeln("	}");
			_out.writeln("	myContext.restore();");
			
			//this function only works if the functions to get the canvas-coordinate
			//are also written in the script
			_functionsToWrite.add("toCanvCoor");
			
		} //end if
		_out.writeln("}"); //function end
	}

	/**
	 * Write the code to draw vertical bars.<br>
	 * This method should be used after the codes to draw the scales on the y-axis are written.
	 */
	private void writeFuncDrawVerticalBars() {
		_out.writeln("");
		_out.writeln("function drawVBars() {"); //function start
		
		if (_canvas instanceof CanvasCoordinateChart) {
			this.writeData();
			CanvasCoordinateChart<?> canvas = (CanvasCoordinateChart<?>) _canvas;
			
			String barColorString = "'" + toHexString(canvas.getDataColor(0)) + "'";		
			for(int i = 1; i < canvas.getNumOfData(); i++) {
				barColorString = barColorString + ", '" +
						this.toHexString(canvas.getDataColor(i)) + "'";
			}
			
			_out.writeln("	var colors = [" + barColorString + "];");
			_out.writeln("	var yScale = " + canvas.getYScale() + ";");
			_out.writeln("	var numOfYScale = " + canvas.getNumOfYScale() + ";");
			_out.writeln("	var chartHeight = " + canvas.getChartHeight() + ";");
			_out.writeln("	var chartWidth = " + canvas.getChartWidth() + ";");
			_out.writeln("	var xScale = " + canvas.getXScale() + ";");
			_out.writeln("	var numOfXScale = " + canvas.getNumOfXScale() + ";");
			_out.writeln("	var startXScale = " + canvas.getStartXScale() + ";");
			
			if (canvas instanceof CanvasCoordinateChartInterval) {
				_out.writeln("");
				//draw bars
				_out.writeln("	myContext.save();");
				_out.writeln("	for (var i=0; dataValues.length>i; i++) {");
				_out.writeln("		var startX = (lowerLimits[i]-startXScale)/(xScale*numOfXScale) * chartWidth");
				_out.writeln("		var endX = (upperLimits[i]-startXScale)/(xScale*numOfXScale) * chartWidth");
				_out.writeln("		var barWidth = endX-startX");
				_out.writeln("		var barHeight = dataValues[i]/(yScale*numOfYScale) * chartHeight;");
				_out.writeln("		myContext.fillStyle = colors[i];");
				_out.writeln("		myContext.fillRect(xToCanvasCoordinate(startX), yToCanvasCoordinate(0), barWidth, 0-barHeight);");
				_out.writeln("	}");
				_out.writeln("	myContext.restore();");
				
				//this function only works if the functions to get the canvas-coordinate
				//are also written in the script
				_functionsToWrite.add("toCanvCoor");
				
			} else {
				if (canvas instanceof CanvasHistogramDouble || canvas instanceof CanvasHistogramLong) {
					_out.writeln("	var barWidth = 4/5*" + canvas.getChartWidth() + " /dataValues.length;");
					_out.writeln("	var barGap = 1/5*" + canvas.getChartWidth() + "/(dataValues.length+1);");					

					_out.writeln("");
					//draw bars
					_out.writeln("	myContext.save();");
					_out.writeln("	for (var i=0; dataValues.length>i; i++) {");
					_out.writeln("		var startX = (i+1)*barGap + i*barWidth;");
					_out.writeln("		var barHeight = dataValues[i]/(yScale*numOfYScale) * chartHeight;");
					_out.writeln("		myContext.fillStyle = colors[i];");
					_out.writeln("		myContext.fillRect(xToCanvasCoordinate(startX), yToCanvasCoordinate(0), barWidth, 0-barHeight);");
					_out.writeln("	}");
					_out.writeln("	myContext.restore();");
					
					//this function only works if the functions to get the canvas-coordinate
					//are also written in the script
					_functionsToWrite.add("toCanvCoor");
					
				}// end if				
			} //end else		
		} //end if
		
		_out.writeln("}"); //function end		
	}
	
	/**
	 * Writes the function to draw the x-Axis at the bottom of the chart area.
	 */
	private void writeFuncDrawXAxis() {
		_out.writeln("");
		_out.writeln("function drawXAxis() {"); //function start
		
		if (_canvas instanceof CanvasCoordinateChart) {
			CanvasCoordinateChart<?> canvas = (CanvasCoordinateChart<?>) _canvas;
			
			//draw x-axis
			_out.writeln("	myContext.beginPath();");
			_out.writeln("	myContext.moveTo(" + canvas.getLeftGap() + ", myCanvas.height-" + canvas.getBottomGap() + ");");
			_out.writeln("	myContext.lineTo(myCanvas.width-" + canvas.getRightGap() +
					", myCanvas.height-" + canvas.getBottomGap() + ");");
			_out.writeln("	myContext.stroke();");
			_out.writeln("	myContext.closePath();");
			
			String xAxisTitle = canvas.getXAxisTitle();
			int horTitlePos = canvas.getLeftGap() + canvas.getChartWidth()/2;//The horizontal position of the title.
			int verTitlePos = canvas.getCanvasHeight() - 10;//The vertical position of the title.
			_out.writeln("");
			_out.writeln("	myContext.save();");
			_out.writeln("	myContext.textAlign = 'center';");
			_out.writeln("	myContext.textBaseline = 'bottom';");
			_out.writeln("	myContext.font = 'bold 8pt Georgia';");			
			_out.writeln("	myContext.fillText('"+ xAxisTitle + "', " + horTitlePos + ", " + verTitlePos + ");");
			_out.writeln("	myContext.restore();");        
		} //end if
		
		_out.writeln("}"); //function end	
	}
	
	/**
	 * Writes the function to draw the scales on the x-axis
	 * if it is a CanvasCoordinateChart and the number of scales aren't 0.
	 */
	private void writeFuncDrawXScale() {
		_out.writeln("");
		_out.writeln("function drawXScale() {");  //function start
		
		if(_canvas instanceof CanvasCoordinateChart) {
			
			CanvasCoordinateChart<?> coorCanvas = (CanvasCoordinateChart<?>) _canvas;
			
			//the number for the first scale on the x-axis
			double startXScale = coorCanvas.getStartXScale().doubleValue();
			//the difference between each scale on the x-axis
			double xScale = coorCanvas.getXScale().doubleValue();
			//the number of scales on the x-axis
			long numOfXScale = coorCanvas.getNumOfXScale();
			//the height of the chart (not the canvas)
			int chartHeight = coorCanvas.getChartHeight();
			//the width of the chart (not the canvas)
			int chartWidth = coorCanvas.getChartWidth();
			//the color for the scale
			String scaleColor = this.toHexString(coorCanvas.getScaleLineColor());
			//the vertical position of the scale text
			int verTextPos = (coorCanvas.getCanvasHeight()-coorCanvas.getBottomGap()+10);
			
			if (numOfXScale > 0) {
				_out.writeln("	var startXScale = " + startXScale + ";");
				_out.writeln("	var xScale = " + xScale + ";");
				_out.writeln("	var numOfXScale = " + numOfXScale + ";");
				
				_out.writeln("	var chartHeight = " + chartHeight + ";");
				_out.writeln("	var chartWidth = " + chartWidth + ";");
				
				_out.writeln("");
				//draw xScale
				_out.writeln("	myContext.save();");
				_out.writeln("	myContext.strokeStyle = '"+ scaleColor + "';");
				_out.writeln("	for (var i=0; numOfXScale>=i; i++) {");
				_out.writeln("		myContext.beginPath();");
				_out.writeln("		myContext.moveTo(xToCanvasCoordinate(i*chartWidth/numOfXScale), yToCanvasCoordinate(0-5));");
				_out.writeln("		myContext.lineTo(xToCanvasCoordinate(i*chartWidth/numOfXScale),  yToCanvasCoordinate(chartHeight));");
				_out.writeln("		myContext.stroke();");
				_out.writeln("		myContext.closePath();");
				_out.writeln("	}");
				_out.writeln("	myContext.restore();");
				
				_out.writeln("");
				//text for the scales on the x-axis
				_out.writeln("	myContext.save();");
				_out.writeln("	myContext.textAlign = 'center';");
				_out.writeln("	myContext.textBaseline = 'top';");
				_out.writeln("	for (var i=0; numOfXScale>=i; i++) {");
				_out.writeln("		var scaleText = i*xScale + startXScale;");
				_out.writeln("		myContext.fillText(scaleText, xToCanvasCoordinate(i*chartWidth/numOfXScale), " + verTextPos + ");");
				_out.writeln("	}");
				_out.writeln("	myContext.restore();");
				
				//this function only works if the functions to get the canvas-coordinate
				//are also written in the script
				_functionsToWrite.add("toCanvCoor");
				
			}			
		}		
		
		_out.writeln("}");
	}
	/**
	 * Writes the function to draw the y-Axis at the left of the chart area.
	 */
	private void writeFuncDrawYAxis() {
		_out.writeln("");
		_out.writeln("function drawYAxis() {"); //function start
		
		if (_canvas instanceof CanvasCoordinateChart) {			
			CanvasCoordinateChart<?> canvas = (CanvasCoordinateChart<?>) _canvas;
			
			//draw y-axis
			_out.writeln("	myContext.beginPath();");
			_out.writeln("	myContext.moveTo(" + canvas.getLeftGap() + ", " + canvas.getTopGap() + ");");
			_out.writeln("	myContext.lineTo(" + canvas.getLeftGap() + ", myCanvas.height-" + canvas.getBottomGap() + ");");
			_out.writeln("	myContext.stroke();");
			_out.writeln("	myContext.closePath();");
			
			String yAxisTitle = canvas.getYAxisTitle();
			int horTitlePos = canvas.getBottomGap() + canvas.getChartHeight()/2;//The horizontal position of the title.
			int verTitlePos = 10;//The vertical position of the title.
			_out.writeln("");
			_out.writeln("	myContext.save();");
			_out.writeln("	myContext.translate(0, myCanvas.height);");
			_out.writeln("	myContext.rotate(toRad(270));");
			_out.writeln("	myContext.textAlign = 'center';");
			_out.writeln("	myContext.textBaseline = 'top';");
			_out.writeln("	myContext.font = 'bold 8pt Georgia';");			
			_out.writeln("	myContext.fillText('"+ yAxisTitle + "', " + horTitlePos + ", " + verTitlePos + ");");
			_out.writeln("	myContext.restore();");			

			//this function only works if the functions toRad()
			//is also written in the script
			_functionsToWrite.add("toRad");
		} //end if
		
		_out.writeln("}"); //function end		
	}

	/**
	 * Writes the function to draw the scales on the y-axis
	 * if it is a CanvasCoordinateChart and the number of scales aren't 0.
	 */
	private void writeFuncDrawYScale() {
		_out.writeln("");
		_out.writeln("function drawYScale() {"); //function start
		
		if (_canvas instanceof CanvasCoordinateChart) {
			CanvasCoordinateChart<?> canvas = (CanvasCoordinateChart<?>) _canvas;
			
			if (canvas.getNumOfYScale() != 0) {
				_out.writeln("	var yScale = " + canvas.getYScale() + ";");
				_out.writeln("	var numOfYScale = " + canvas.getNumOfYScale() + ";");
				
				_out.writeln("	var chartHeight = " + canvas.getChartHeight() + ";");
				_out.writeln("	var chartWidth = " + canvas.getChartWidth() + ";");
						
				_out.writeln("");
				//draw yScale
				_out.writeln("	myContext.save();");
				_out.writeln("	myContext.strokeStyle = '"+ this.toHexString(canvas.getScaleLineColor()) + "';");
				_out.writeln("	for (var i=1; numOfYScale>=i; i++) {");
				_out.writeln("		myContext.beginPath();");
				_out.writeln("		myContext.moveTo(xToCanvasCoordinate(0-5), yToCanvasCoordinate(i*chartHeight/numOfYScale));");
				_out.writeln("		myContext.lineTo(xToCanvasCoordinate(chartWidth),  yToCanvasCoordinate(i*chartHeight/numOfYScale));");
				_out.writeln("		myContext.stroke();");
				_out.writeln("		myContext.closePath();");
				_out.writeln("	}");
				_out.writeln("	myContext.restore();");
			
				_out.writeln("");
				//text for the scales on the y-axis
				_out.writeln("	myContext.save();");
				_out.writeln("	myContext.textAlign = 'right';");
				_out.writeln("	myContext.textBaseline = 'middle';");
				_out.writeln("	for (var i=0; numOfYScale>=i; i++) {");
				_out.writeln("		var scaleText = i*yScale;");
				_out.writeln("		myContext.fillText(scaleText, " + (canvas.getLeftGap()-10) + ", yToCanvasCoordinate(i*chartHeight/numOfYScale));");
				_out.writeln("	}");
				_out.writeln("	myContext.restore();");
				
				//this function only works if the functions to get the canvas-coordinate
				//are also written in the script
				_functionsToWrite.add("toCanvCoor");				
			} // end if
		} //end if
		
		_out.writeln("}"); //function end		
	}

	/**
	 * Writes the functions needed to display the chart in the canvas.
	 */
	private void writeFunctions() {
		HashSet<String> writtenSet = new HashSet<String>();
		while (!writtenSet.equals(_functionsToWrite)) {

			HashSet<String> buffFunctionsToWrite = new HashSet<String>();
			for (String func: _functionsToWrite) {
				buffFunctionsToWrite.add(func);
			}
			
			for (String func: buffFunctionsToWrite) {
				if (!writtenSet.contains(func)) {
					switch (func) {
					case "drawVBars":
						this.writeFuncDrawVerticalBars();
						break;
					case "drawXAxis":
						this.writeFuncDrawXAxis();
						break;
					case "drawYAxis":
						this.writeFuncDrawYAxis();
						break;
					case "drawXScale":
						this.writeFuncDrawXScale();
						break;
					case "drawYScale":
						this.writeFuncDrawYScale();
						break;
					case "toCanvCoor":
						this.writeFuncToGetCanvasCoordinate();
						break;
					case "toRad":
						this.writeFuncToRad();
						break;
					case "drawTextLabels":
						this.writeFuncDrawTextLabels();
						break;
					case "drawTimeSeries":
						this.writeFuncDrawTimeSeries();
						break;
					default:
						break;
					} //end switch
					writtenSet.add(func);
				}// end if				
			} //end for		
		} //end while		
	}
	
	/**
	 * Write the functions to get the canvas-coordinate from
	 * a given x-coordinate or y-coordinate into the JavaScript. 
	 */
	private void writeFuncToGetCanvasCoordinate() {
		if (_canvas instanceof CanvasCoordinateChart) {
			CanvasCoordinateChart<?> canvas = (CanvasCoordinateChart<?>) _canvas;
			
			_out.writeln("");
			_out.writeln("function xToCanvasCoordinate(xCoordinate) {");
			_out.writeln("	return "+ canvas.getLeftGap() +" + xCoordinate;");
			_out.writeln("}");
			
			_out.writeln("");
			_out.writeln("function yToCanvasCoordinate(yCoordinate) {");
			_out.writeln("	return myCanvas.height-" + canvas.getBottomGap() + " - yCoordinate;");
			_out.writeln("}");
		} else {
			_out.writeln("");
			_out.writeln("function xToCanvasCoordinate(xCoordinate) {");
			_out.writeln("}");
			
			_out.writeln("");
			_out.writeln("function yToCanvasCoordinate(yCoordinate) {");
			_out.writeln("}");
			
		}
	}
	
	/**
	 * Writes the function to calculate the radiant from a given angular degree.
	 */
	private void writeFuncToRad() {
		_out.writeln("");
		_out.writeln("function toRad(x) {");
		_out.writeln("	return (x*Math.PI)/180;");
		_out.writeln("};");
	}

}
