package desmoj.core.report.html5chart;

import desmoj.core.simulator.NamedObject;

import java.awt.*;

/**
 * A general Chart environment with defined gaps between the canvas border and the actual chart.
 *
 * @author Johanna Djimandjaja
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public abstract class AbstractNumericalChartCanvas<N extends Number> extends NamedObject
implements CanvasCoordinateChart<N> {

    /**
     * Color used for drawing the borders and the axis.
     */
    private final Color _black = new Color(0, 0, 0);

    /**
     * An array containing the gaps between the canvas border and the actual chart. [top gap, right gap, bottom gap,
     * left gap]
     */
    private final int[] _borderGap = { 20, 20, 50, 60 };
    /**
     * Color used for drawing the scales.
     */
    private final Color _lightGrey = new Color(190, 190, 190);
    /**
     * The height of the canvas.
     */
    private int _canvasHeight;
    /**
     * The width of the canvas.
     */
    private int _canvasWidth;

    /**
     * Create a canvas.
     *
     * @param canvasID     The ID of this canvas.
     * @param canvasHeight The height of this canvas.<br> Should be larger then
     *                     <code>this.getTopGap() + this.getBottomGap() + 100</code>.
     * @param canvasWidth  The width of this canvas.<br> Should be larger then
     *                     <code>this.getLeftGap() + this.getRightGap() + 100</code>.
     */
    public AbstractNumericalChartCanvas(String canvasID, int canvasHeight, int canvasWidth) {
        super(canvasID);

        if (canvasHeight > 270) {
            _canvasHeight = canvasHeight;
        } else {
            _canvasHeight = 270;
        }

        if (canvasWidth > 400) {
            _canvasWidth = canvasWidth;
        } else {
            _canvasWidth = 400;
        }
    }

    /**
     * Returns the gap between the bottom border of the canvas and the chart.
     *
     * @return int : Gap between the bottom border of the canvas and the chart.
     */
    @Override
    public int getBottomGap() {
        return _borderGap[2];
    }

    /**
     * Gets the height of the canvas.
     *
     * @return int : The height of the canvas.
     */
    @Override
    public int getCanvasHeight() {
        return _canvasHeight;
    }

    @Override
    public String getCanvasID() {
        return super.getName();
    }

    /**
     * Gets the width of the canvas.
     *
     * @return int : The width of the canvas.
     */
    @Override
    public int getCanvasWidth() {
        return _canvasWidth;
    }

    @Override
    public int getChartHeight() {
        return _canvasHeight - this.getTopGap() - this.getBottomGap();
    }

    @Override
    public int getChartWidth() {
        return _canvasWidth - this.getLeftGap() - this.getRightGap();
    }

    /**
     * Returns the color for the canvas border and the axis.
     *
     * @return
     */
    @Override
    public Color getDefaultColor() {
        return _black;
    }

    /**
     * Returns the gap between the left border of the canvas and the chart.
     *
     * @return int : Gap between the left border of the canvas and the chart.
     */
    @Override
    public int getLeftGap() {
        return _borderGap[3];
    }

    /**
     * Returns the gap between the right border of the canvas and the chart.
     *
     * @return int : Gap between the right border of the canvas and the chart.
     */
    @Override
    public int getRightGap() {
        return _borderGap[1];
    }

    /**
     * Returns the color for the scales in the y-axis.
     *
     * @return java.awt.Color : The color for the scales in the y-axis.
     */
    @Override
    public Color getScaleLineColor() {
        return _lightGrey;
    }

    /**
     * Returns the gap between the top border of the canvas and the chart.
     *
     * @return int : Gap between the top border of the canvas and the chart.
     */
    @Override
    public int getTopGap() {
        return _borderGap[0];
    }
}
