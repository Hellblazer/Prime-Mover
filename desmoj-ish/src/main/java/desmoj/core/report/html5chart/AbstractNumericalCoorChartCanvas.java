package desmoj.core.report.html5chart;

import java.awt.Color;

/**
 * A Chart with a numerical scale (only) on the y-axis.
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
public abstract class AbstractNumericalCoorChartCanvas<N extends Number> extends AbstractNumericalChartCanvas<N>
                                                      implements CanvasCoordinateChart<N> {

    private static Color getColor(int index) {

        Color color = null;
        switch (index) {
        case 0:
            color = Color.black;
            break;
        case 1:
            color = Color.red;
            break;
        case 2:
            color = Color.blue;
            break;
        case 3:
            color = Color.green;
            break;
        case 4:
            color = Color.pink;
            break;
        case 5:
            color = Color.darkGray;
            break;
        case 6:
            color = Color.orange;
            break;
        case 7:
            color = Color.magenta;
            break;
        case 8:
            color = Color.yellow;
            break;
        case 9:
            color = Color.cyan;
            break;
        case 10:
            color = Color.lightGray;
            break;
        case 11:
            color = new Color(120, 150, 210);
            break;
        case 12:
            color = new Color(106, 50, 154);
            break;
        case 13:
            color = new Color(94, 55, 41);
            break;
        case 14:
            color = new Color(104, 0, 31);
            break;
        case 15:
            color = new Color(180, 144, 90);
            break;
        case 16:
            color = new Color(120, 88, 54);
            break;
        case 17:
            color = new Color(238, 180, 159);
            break;
        case 18:
            color = new Color(184, 135, 100);
            break;
        case 19:
            color = new Color(43, 23, 71);
            break;
        case 20:
            color = new Color(140, 152, 248);
            break;
        case 21:
            color = new Color(255, 255, 128);
            break;
        case 22:
            color = new Color(70, 10, 20);
            break;
        case 23:
            color = new Color(42, 79, 140);
            break;
        default:
            color = Color.black;
            break;
        }
        return color;
    }

    /**
     * An array containing the color that represents each data. The order of the
     * entries in this array should be the same as those in chartData.
     */
    protected Color[] _dataColors;

    /** Number of scale on the y-axis. */
    protected long _numOfYScale;

    /** The difference between each scale on the y-axis. */
    protected N _yScale;

    /** The chart data displayed in this canvas. */
    protected AbstractChartData<N> chartData;

    /**
     * Create a canvas.
     *
     * @param canvasID     The ID of this canvas.
     * @param canvasHeight The height of this canvas.<br>
     *                     Should be larger then
     *                     <code>this.getTopGap() + this.getBottomGap() + 100</code>.
     * @param canvasWidth  The width of this canvas.<br>
     *                     Should be larger then
     *                     <code>this.getLeftGap() + this.getRightGap() + 100</code>.
     *
     */
    public AbstractNumericalCoorChartCanvas(String canvasID, int canvasHeight, int canvasWidth,
                                            AbstractChartData<N> data) {
        super(canvasID, canvasHeight, canvasWidth);

        this.chartData = data;
        _dataColors = new Color[this.chartData.getNumOfData()];
        determineDataColors();
        determineYScale();
    }

    /**
     * Returns the color, that represents the data in cell n.
     *
     * @param index the index of the data, which color is asked for
     *
     * @return java.awt.Color : The color of the data at index n.
     */
    @Override
    public Color getDataColor(int index) {
        if (index < 0 || index >= this._dataColors.length) {
            // Attempt to get the color of a not known bar. "Color.WHITE will be returned!"
            return Color.WHITE;
        }

        return _dataColors[index];
    }

    /**
     *
     * @return
     */
    @Override
    public int getNumOfData() {
        return this.chartData.getNumOfData();
    }

    /**
     * Returns the number of scales to be shown on the y-axis.
     *
     * @return
     */
    @Override
    public long getNumOfYScale() {
        return _numOfYScale;
    }

    /**
     * Returns the difference between each scale on the y-axis.
     *
     * @return
     */
    @Override
    public N getYScale() {
        return _yScale;
    }

    abstract void determineYScale();

    /**
     * Returns the data at a specified index.
     *
     * @param index The index of the data.
     *
     * @return N :
     */
    abstract N getDataValue(int index);

    /**
     * Determines the colors, representing each data.
     */
    private void determineDataColors() {
        for (int i = 0; i < _dataColors.length; i++) {
            _dataColors[i] = getColor(i);
        }
    }
}
