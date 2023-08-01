package desmoj.core.report;

import java.util.ArrayList;
import java.util.List;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.Reportable;

/**
 * The Reporter for a model. Carries references to all Reportables and submodels
 * of the Model it reports about. This enables it to produce a vector containing
 * the reportable's reporters ordered by group-ID. It also takes care to
 * retrieve the submodel's reporters recursively.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
 *
 */
public class ModelReporter extends desmoj.core.report.Reporter {

	/**
	 * The list containing the reportable's reporters ordered by group-ID.
	 */
	private ArrayList<Reporter> _reporters;

	/**
	 * Creates a reporter for the given model. This special reporter retrieves
	 * all other reporters associated to the reportable objects registered at
	 * the model. These are collected in a vector, ordered by group-ID to be
	 * sent to the report output when a report is required.
	 * 
	 * @param model
	 *            Model : The model to report about.
	 */
	public ModelReporter(Model model) {

		super(model);
		List<Reportable> reportables = model.getReportables();

		_reporters = new ArrayList<Reporter>();
		groupID = 2147483647; // highest groupID possible, so always first in
		// order
		groupHeading = "Model " + source.getName();
		numColumns = 1;
		columns = new String[numColumns];
		columns[0] = "Description";
		entries = new String[numColumns];

		if (reportables == null)
			return; // nothing to insert
		if (reportables.isEmpty())
			return; // still nothing to insert

		// insert all reportable's reporters in order of group-id
		// buffer frequent used variables for faster access
		Reporter repoBuff; // buffer variable for Reporter

		for (Reportable r : reportables) {

			if (r != null) {

				//repoBuff = r.createDefaultReporter();
				repoBuff = r.getReporter();
				
				if (repoBuff != null) {

					// insert according to group-ID
					if (_reporters.isEmpty()) {
						_reporters.add(repoBuff);
					} else { // now sort until pos. found

						for (int k = 0; k < _reporters.size(); k++) {

							if (Reporter.isLarger(_reporters.get(k), repoBuff)) {
								_reporters.add(k + 1, repoBuff);
								break;
							} // endif pos. found

						} // endfor search position

					} // endelse special care for first reporter

				} // endif repoBuff == null

			} // endif ableBuff == null

		} // endfor

	}

	/**
	 * The ModelReporter returns the description of the model.
	 * 
	 * @return java.lang.String[] : Array containing the data for reporting
	 */
	public String[] getEntries() {
	    
	    Model m = (Model) source;
		entries[0] = m.description();  

		return entries;

	}

	/**
	 * Returns a list of view of all reportable's reporters ordered by
	 * group-ID.
	 * 
	 * @return java.util.List<Reporter> : The vector containing the reporters associated
	 *         to the modelreporter
	 */
	public List<Reporter> getReporters() {

		return new ArrayList<Reporter>(_reporters);

	}
}