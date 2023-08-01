package desmoj.core.report;

import java.util.List;

import desmoj.core.report.html5chart.ChartDataTimeSeries;
import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the TimeSeries.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen based on ideas from Tim Lechler
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
 * 
 */
public class TimeSeriesReporter extends desmoj.core.report.Reporter {

	public TimeSeriesReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource);
		
		groupID = 1361; // see Reporter for more information about groupID
		
		numColumns = 10;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "(Re)set";
        columns[2] = "Start";
        columns[3] = "End";
		columns[4] = "Obs";
        columns[5] = "First";
        columns[6] = "Last";
        columns[7] = "Min";
        columns[8] = "Max";
        columns[9] = "Group";
		
		entries = new String[numColumns];
		if (source instanceof desmoj.core.statistic.TimeSeries) {
			desmoj.core.statistic.TimeSeries timeSeries 	= (desmoj.core.statistic.TimeSeries) source;
			entries[0] = timeSeries.getName();
			entries[1] = timeSeries.resetAt().toString();
            entries[2] = Double.toString(StatisticObject.round(timeSeries.get_start().getTimeAsDouble()));
            entries[3] = Double.toString(StatisticObject.round(timeSeries.get_end().getTimeAsDouble()));
			entries[4] = Long.toString(timeSeries.getObservations());
			List<Double> timeListBuff = timeSeries.getTimeValues();
			List<Double> dataListBuff = timeSeries.getDataValues();
			if (dataListBuff == null || dataListBuff.size() == 0) {
	            entries[5] = Double.toString(StatisticObject.UNDEFINED);
	            entries[6] = Double.toString(StatisticObject.UNDEFINED);
	            entries[7] = Double.toString(StatisticObject.UNDEFINED);
                entries[8] = Double.toString(StatisticObject.UNDEFINED);
			} else {
   	            entries[5] = Double.toString(dataListBuff.get(0));
	            entries[6] = Double.toString(dataListBuff.get(timeListBuff.size()-1));
			    double min = dataListBuff.get(0);
    			double max = dataListBuff.get(0);
    			for (int i = 1; i < dataListBuff.size(); i++) {
    			    if (dataListBuff.get(i) > max) max = dataListBuff.get(i);
    			    if (dataListBuff.get(i) < min) min = dataListBuff.get(i);
    			}
                entries[7] = Double.toString(min);
                entries[8] = Double.toString(max);
            }	
			entries[9] = timeSeries.getGroup();
		}
		groupHeading = "TimeSeries";
	}

	public ChartDataTimeSeries getChartData() {		
		// the Histogram we report about (source = informationSource)
		desmoj.core.statistic.TimeSeries timeSeries = (desmoj.core.statistic.TimeSeries) source;
		
		List<Double> dataValues = timeSeries.getDataValues();
		List<Double> timeValues = timeSeries.getTimeValues();
		return new ChartDataTimeSeries(timeSeries.getModel(), timeValues, dataValues, 
		        StatisticObject.round(timeSeries.get_start().getTimeAsDouble()), StatisticObject.round(timeSeries.get_end().getTimeAsDouble()),
		        timeSeries.getGroup());
	}
	
	@Override
	public String[] getEntries() {
		return entries;
	}
	
	/* @TODO: Comment */
    public boolean makeAdditionalColorEntryIfHTMLColorChartIsGenerated() {
        return true;
    }
}