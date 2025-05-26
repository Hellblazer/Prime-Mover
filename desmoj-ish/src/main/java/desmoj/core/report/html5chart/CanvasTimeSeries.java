package desmoj.core.report.html5chart;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A canvas to display data from multiple TimeSeries.
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
public class CanvasTimeSeries extends AbstractNumericalChartCanvas<Double> {

    private static int nextColorAvailable = 0;
    /** The title for the x-Axis */
    private final String _xAxisTitle;
    /** The title for the y-Axis */
    private final String _yAxisTitle;
    /**
     * An List containing the color that represents each TimeSeries. The order of the entries in this List should be the
     * same as those in _timeSeries.
     */
    private List<Color> _dataColors;
    /** The number of scales on the x-axis. */
    private long _numOfXScale;
    /** Number of scale on the y-axis. */
    private long _numOfYScale;
    /** The number for the first scale on the x-axis. */
    private double _startXScale;
    /** A list of TimeSeries to be displayed in this canvas. */
    private List<ChartDataTimeSeries> _timeSeries;
    /** The difference between each scale on the x-axis. */
    private double _xScale;
    /** The difference between each scale on the y-axis. */
    private double _yScale;

    public CanvasTimeSeries(String name, int canvasHeight, int canvasWidth, ChartDataTimeSeries timeSeriesData,
                            String axis_x, String axis_y) {
        super(name, canvasHeight, canvasWidth);
        _timeSeries = new ArrayList<>();
        _dataColors = new ArrayList<>();
        _timeSeries.add(timeSeriesData);
        _dataColors.add(getColor(nextColorAvailable++));
        _numOfXScale = Long.MIN_VALUE;
        _numOfYScale = Long.MIN_VALUE;
        _startXScale = Double.NaN;
        _xScale = Double.NaN;
        _yScale = Double.NaN;
        _xAxisTitle = axis_x;
        _yAxisTitle = axis_y;

    }

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
     * Add a chart data of a TimeSeries to this canvas. The color will be defined to represent this TimeSeries in the
     * canvas
     *
     * @param timeSeries ChartDataTimeSeries: the chart data of a TimeSeries to be added to this canvas
     * @return Color: the color defined to represent this TimeSeries in the canvas
     */
    public Color addTimeSeries(ChartDataTimeSeries timeSeries) {
        _timeSeries.add(timeSeries);
        Color color = getColor(nextColorAvailable++);
        this._dataColors.add(color);
        return color;
    }

    /**
     * Returns the color, that represents the TimeSEries at index i.
     *
     * @param i
     * @return
     */
    @Override
    public Color getDataColor(int i) {
        if (i < 0 || i >= _timeSeries.size()) {
            return Color.WHITE;
        }
        return _dataColors.get(i);
    }

    /**
     * Returns the data value of the TimeSeriesData at index i.
     *
     * @param i
     * @return
     */
    public Double[] getDataValues(int i) {
        if (i < 0 || i >= this.getNumOfTimeSeries()) {
            return new Double[0];
        } else {
            return _timeSeries.get(i).getDataValues();
        }
    }

    /**
     * Returns the number of TimeSeries displayed by this canvas.
     */
    @Override
    public int getNumOfData() {
        return _timeSeries.size();
    }

    public int getNumOfTimeSeries() {
        return _timeSeries.size();
    }

    /**
     * Returns the number of scales to be shown on the x-axis.
     *
     * @return
     */
    @Override
    public long getNumOfXScale() {
        if (_numOfXScale == Long.MIN_VALUE) {
            determineXScale();
        }
        return _numOfXScale;
    }

    /**
     * Returns the number of scales to be shown on the y-axis.
     *
     * @return
     */
    @Override
    public long getNumOfYScale() {
        if (_numOfYScale == Long.MIN_VALUE) {
            determineYScale();
        }
        return _numOfYScale;
    }

    /**
     * Returns the number for the first xScale.
     *
     * @return
     */
    @Override
    public Double getStartXScale() {
        if (Double.isNaN(_startXScale)) {
            determineXScale();
        }
        return _startXScale;
    }

    /**
     * Returns the time value of the TimeSeriesData at index i.
     *
     * @param i
     * @return
     */
    public Double[] getTimeValues(int i) {
        if (i < 0 || i >= this.getNumOfTimeSeries()) {
            return new Double[0];
        } else {
            return _timeSeries.get(i).getTimeValues();
        }
    }

    @Override
    public String getXAxisTitle() {
        return _xAxisTitle;
    }

    /**
     * Returns the difference between each scale on the x-axis.
     *
     * @return
     */
    @Override
    public Double getXScale() {
        if (Double.isNaN(_xScale)) {
            determineXScale();
        }
        return _xScale;
    }

    @Override
    public String getYAxisTitle() {
        return _yAxisTitle;
    }

    /**
     * Returns the difference between each scale on the y-axis.
     *
     * @return
     */
    @Override
    public Double getYScale() {
        if (Double.isNaN(_yScale)) {
            determineYScale();
        }
        return _yScale;
    }

    /**
     * Determines the difference between each scale on the y-axis.
     */
    void determineYScale() {

        double maxEntry = this.getMaxDataValue();

        if (maxEntry <= 1) {
            _numOfYScale = 1;
            _yScale = 1;
            return;
        }

        double[] hight_candidates = { 1.2, 1.4, 1.5, 1.8, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0 };
        int[] hight_candidates_ticks = { 6, 7, 6, 9, 10, 10, 6, 7, 8, 9, 10, 6, 7, 8, 9, 10 };

        int power = (int) Math.log10(maxEntry);
        int candidate = 0;

        for (candidate = 0; candidate < hight_candidates.length; candidate++) {
            if (Math.pow(10, power) * hight_candidates[candidate] > maxEntry) {
                break;
            }
        }

        _numOfYScale = hight_candidates_ticks[candidate];
        _yScale = Math.pow(10, power) * hight_candidates[candidate] / hight_candidates_ticks[candidate];
    }

    private void determineXScale() {

        // case: there is no TimeSeries chart data
        if (_timeSeries.isEmpty()) {
            _xScale = 1;
            _numOfXScale = 10;
            _startXScale = 0;

            // otherwise use their maximum intervals
        } else {

            double left = this.getMinTimeValue();
            double right = this.getMaxTimeValue();
            long ticks = 10;

            _xScale = (right - left) / ticks;
            _numOfXScale = ticks;
            _startXScale = left;
        }
    }

    /**
     * Returns the maximum data value of all TimeSeries.<br> If there're no TimeSeries chart data for this canvas,
     * <code>0</code> will be returned.
     *
     * @return
     */
    private double getMaxDataValue() {
        double max = 0;
        for (ChartDataTimeSeries timeSeriesData : _timeSeries) {
            if (max < timeSeriesData.getHighestDataValue()) {
                max = timeSeriesData.getHighestDataValue();
            }
        }
        return max;
    }

    /**
     * Returns the maximum time value of all TimeSeries.<br> If there're no TimeSeries chart data for this canvas,
     * <code>0</code> will be returned.
     *
     * @return
     */
    private double getMaxTimeValue() {
        double max = 0;
        for (ChartDataTimeSeries timeSeriesData : _timeSeries) {
            if (timeSeriesData.getValidTo() > max) {
                max = timeSeriesData.getValidTo();
            }
        }
        return max;
    }

    /**
     * Returns the minimum time value of all TimeSeries.<br> If there're no TimeSeries chart data for this canvas,
     * <code>0</code> will be returned.
     *
     * @return
     */
    private double getMinTimeValue() {
        if (_timeSeries.isEmpty()) {
            return 0;
        } else {
            double min = Double.MAX_VALUE;
            for (ChartDataTimeSeries timeSeriesData : _timeSeries) {
                if (timeSeriesData.getValidFrom() < min) {
                    min = timeSeriesData.getValidFrom();
                }
            }
            return min;
        }
    }
}
