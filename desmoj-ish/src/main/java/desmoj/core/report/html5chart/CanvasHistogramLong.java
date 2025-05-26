package desmoj.core.report.html5chart;

/**
 * A canvas to display the data from a Histogram.
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
public class CanvasHistogramLong extends AbstractNumericalCoorChartCanvasLong {

    /** The title for the y-Axis */
    private final String _yAxisTtile = "n";
    /** The title for the x-Axis */
    private String _xAxisTitle = "";

    /**
     * Create a Canvas for TextHistogram.
     *
     * @param canvasName   The name of the canvas.
     * @param canvasHeight The height of the canvas.
     * @param canvasWidth  The width of the canvas.
     * @param data         The data of the TextHistogram to be displayed in this canvas.
     * @param data         Text at the x-axis
     */
    public CanvasHistogramLong(String canvasName, int canvasHeight, int canvasWidth,
                               ChartDataHistogramLong textHistData, String xText) {
        super(canvasName, canvasHeight, canvasWidth, textHistData);
        _xAxisTitle = xText;
    }

    /**
     * Returns the value of the data at a specified index.
     *
     * @param index The index of the data.
     * @return Long :
     */
    @Override
    public Long getDataValue(int index) {
        if (index < 0 || index >= this.getNumOfData()) {
            // Attempt to get an unknown data of a not known bar. "0 will be returned!"
            return 0L;
        }

        return this.chartData.getDataValue(index);
    }

    @Override
    public long getNumOfXScale() {
        return 0;
    }

    @Override
    public Long getStartXScale() {
        return 0l;
    }

    /**
     * Returns the observed text at the given index.
     *
     * @param index int : The index of the String asked for
     * @return String : The observed text at the given index
     */
    public String getText(int index) {
        ChartDataHistogramLong thCartData = ((ChartDataHistogramLong) chartData);
        if (index < 0 || index >= this.getNumOfData()) {
            return "";
        } else {
            return thCartData.getObservedString(index);
        }
    }

    @Override
    public String getXAxisTitle() {
        return _xAxisTitle;
    }

    @Override
    public Long getXScale() {
        return 0l;
    }

    @Override
    public String getYAxisTitle() {
        return _yAxisTtile;
    }
}
