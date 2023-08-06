package desmoj.core.report.html5chart;

import java.awt.Color;

/**
 * A canvas to display charts in a coordinate system.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Johanna Djimandjaja
 *
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 */
public interface CanvasCoordinateChart<N extends Number> extends Canvas {
    /**
     * Returns the gap between the bottom border of the canvas and the chart.
     *
     * @return int : Gap between the bottom border of the canvas and the chart.
     */
    public int getBottomGap();

    /**
     * Returns the gap between the left border of the canvas and the chart.
     *
     * @return int : Gap between the left border of the canvas and the chart.
     */
    public int getLeftGap();

    /**
     * Returns the gap between the right border of the canvas and the chart.
     *
     * @return int : Gap between the right border of the canvas and the chart.
     */
    public int getRightGap();

    /**
     * Returns the gap between the top border of the canvas and the chart.
     *
     * @return int : Gap between the top border of the canvas and the chart.
     */
    public int getTopGap();

    /**
     * Returns the height of the chart area.
     *
     * @return
     */
    int getChartHeight();

    /**
     * Returns the width of the chart area.
     *
     * @return
     */
    int getChartWidth();

    /**
     * Returns the Color to represent the data at the given index.
     *
     * @param i
     * @return
     */
    Color getDataColor(int i);

    /**
     * Returns the number of data.
     *
     * @return
     */
    int getNumOfData();

    /**
     * Returns the number of scales to be shown on the x-axis.
     *
     * @return
     */
    long getNumOfXScale();

    /**
     * Returns the number of scales to be shown on the y-axis.
     *
     * @return
     */
    long getNumOfYScale();

    /**
     * Returns the color for the scales in the y-axis.
     *
     * @return java.awt.Color : The color for the scales in the y-axis.
     */
    Color getScaleLineColor();

    /**
     * Returns the value for the first scale on the x-axis (the scale most left of
     * the chart).
     *
     * @return
     */
    N getStartXScale();

    /**
     * Returns the title for the x-axis.
     *
     * @return
     */
    String getXAxisTitle();

    /**
     * Returns the difference between each scale on the x-axis.
     *
     * @return
     */
    N getXScale();

    /**
     * Returns the title for the y-axis.
     *
     * @return
     */
    String getYAxisTitle();

    /**
     * Returns the difference between each scale on the y-axis.
     *
     * @return
     */
    N getYScale();

}
