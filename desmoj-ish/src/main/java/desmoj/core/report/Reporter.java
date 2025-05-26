package desmoj.core.report;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.Reportable;
import desmoj.core.simulator.Schedulable;

/**
 * Declares the basic methods needed for reporter to be able to print reports about a model component. Since individual
 * components need individual reports each component has to specify a special reporter to print its data. Derive from
 * this class to implement the reporter for the specific
 * <code>Reportable</code> class.
 *
 * @author Tim Lechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @see desmoj.core.simulator.Reportable
 */

public abstract class Reporter {

    /**
     * The column headings of this reporter. Reporters of the same group always have the same column headings. Entries
     * should contain in the elements in the same order as the <code>entries[]</code>.
     */
    protected String[] columns;
    /**
     * The data entries of this reporter. Reporters of the same group always have the same number of entries. Entries
     * should contain in the data elements in the same order as defined in the <code>columns[]</code> array.
     */
    protected String[] entries;
    /**
     * The group's heading of this class of reporters. The String containe here is used as a table heading. All
     * reporters of the same group must have the same group heading.
     */
    protected String groupHeading;
    /**
     * The group-id of the reporters of this class. This ID resembles group-identification and ordering information
     * inside a group. The HTML output classes order <code>Reporters</code> by groups of hundred ID numbers. So i.e. all
     * <code>Distributions</code> have <code>Reporters</code> with GroupID's in the range between 100 and 199. Highest
     * numbers are always listed first. If in a sequence of <code>Reporters</code> its GroupID is in another century
     * range, the HTML output will automatically insert a horizontal ruler and a new heading to indicate the new group
     * of <code>Reportable ModelComponents</code>.
     * <p>
     * <DIV align=center> <TABLE BORDER > <CAPTION>Reserved GroupID ranges
     * </CAPTION>
     * <TR>
     * <TH>GroupID range</TH>
     * <TH>Reportables</TH>
     * </TR>
     * <TR>
     * <TD>2147483647</TD>
     * <TD>ModelReporter. Highest number possible, so always first in order. Ensures
     * that SubModelReporter will be printed before other Reportables in the main model report.</TD>
     * </TR>
     * <TR>
     * <TD>2000000000</TD>
     * <TD>SimulationrunReporter, including Experiment duration and other important
     * information referring to the experiment as whole.</TD>
     * </TR>
     * <TR>
     * <TD>1800 +</TD>
     * <TD>Free for additional Reporters of new constructs that will be listed
     * before the <code>StatisticObjects</code> used in a <code>Model</code>.</TD>
     * </TR>
     * <TR>
     * <TD>1300 - 1799</TD>
     * <TD>Reporters for <code>StatisticObjects</code> as the
     * <code>Accumulate, Tally, TextHistogram, Histogram, Regression</code> and
     * <code>Count</code>, which will be listed before the process synchronisation
     * constructs used in the <code>Model</code>.</TD>
     * </TR>
     * <TR>
     * <TD>400 - 1299</TD>
     * <TD>Additional Reporters for process synchronisation constructs as the
     * <code>Bin, Stock, Entrepot, Res, CondQueue, WaitQueue, Transporter</code> and
     * <code>WorkStation</code>, which will be listed before the <code>Queues</code>
     * used in a <code>Model</code>.</TD>
     * </TR>
     * <TR>
     * <TD>300 - 399</TD>
     * <TD>Free for additional Reporters of new constructs that will be listed
     * before the <code>Queues</code> used in a <code>Model</code>.</TD>
     * </TR>
     * <TR>
     * <TD>200 - 299</TD>
     * <TD>Queues. Queue will be sorted first, ProcessQueue last</TD>
     * </TR>
     * <TR>
     * <TD>100 - 199</TD>
     * <TD>Distributions. The generic <code>DistributionReporter</code> will be last
     * with its GroupID of 100.</TD>
     * </TR>
     * <TR>
     * <TD>1-99</TD>
     * <TD>Free for additional Reporters of new constructs that will be listed after
     * the <code>Queues</code> used in a <code>Model</code>.</TD>
     * </TR>
     * </TABLE>
     * </DIV>
     */
    protected int groupID;
    /**
     * The number of columns this reporter produces.
     */
    protected int numColumns;
    /**
     * The Reportable that this reporter generates the report about
     */
    protected Reportable source;

    /**
     * Creates a reporter for the given reportable information source.
     *
     * @param informationSource desmoj.core.simulator.Reportable : The source of information to report about
     */
    public Reporter(Reportable informationSource) {

        this.source = informationSource;

    }

    /**
     * Creates a reporter for the given Schedulable information source. This Schedulable must have a corresponding
     * Reportable assigned via
     * <code>setCorrespondingReportable(Reportable)</code>.
     *
     * @param Schedulable desmoj.core.simulator.Schedulable : The Schedulable to report about
     */
    public Reporter(Schedulable source) {

        this.source = source.getCorrespondingReportable();

    }

    /**
     * Creates a reporter, preliminarily without information source. Internal framework use only, intended for
     * refinement in subclasses.
     */
    protected Reporter() {

        // nothing to do

    }

    /**
     * Compares the ID's of the two given reporters and returns <code>true</code> if both have the same ID,
     * <code>false</code> if their ID's are different.
     *
     * @param a desmoj.report.Reporter : comparand a
     * @param b desmoj.report.Reporter : comparand b
     * @return boolean : Is <code>true</code> if both reporter's ID's are the same,
     * <code>false</code> if not
     */
    public static boolean isEqual(Reporter a, Reporter b) {

        return (a.getGroupID() == b.getGroupID());

    }

    /**
     * Compares the ID's of the two given reporters and returns <code>true</code> if reporter 'a' has a higher ID than
     * reporter 'b', <code>false</code> if not.
     *
     * @param a desmoj.report.Reporter : comparand a
     * @param b desmoj.report.Reporter : comparand b
     * @return boolean : Is <code>true</code> if reporter 'a' has a higher ID than reporter 'b', <code>false</code> if
     * not
     */
    public static boolean isLarger(Reporter a, Reporter b) {

        return (a.getGroupID() > b.getGroupID());

    }

    /**
     * Compares the ID's of the two given reporters and returns <code>true</code> if reporter 'a' belongs to a different
     * group of ID's than reporter 'b',
     * <code>false</code> if not.
     *
     * @param a desmoj.report.Reporter : comparand a
     * @param b desmoj.report.Reporter : comparand b
     * @return boolean : Is <code>true</code> if reporter 'a' has an ID belonging to a different group than reporter
     * 'b', <code>false</code> if not
     */
    public static boolean isOtherGroup(Reporter a, Reporter b) {

        return !isSameGroup(a, b);

    }

    /**
     * Compares the ID's of the two given reporters and returns <code>true</code> if reporter 'a' belongs to the same
     * group of ID's as reporter 'b',
     * <code>false</code> if not.
     *
     * @param a desmoj.report.Reporter : comparand a
     * @param b desmoj.report.Reporter : comparand b
     * @return boolean : Is <code>true</code> if reporter 'a' has an ID belonging to the same group as reporter 'b',
     * <code>false</code> if not
     */
    public static boolean isSameGroup(Reporter a, Reporter b) {

        // Different submodels always to be written into different groups
        if (a.getModel() != b.getModel()) {
            return false;
        }

        // Otherwise, consider group IDs
        int groupStep = 100;

        // Since 2.3.0: To provide additional groups for new
        // statistics objects (BooleanStatistic, ConfidenceCalculator...),
        // groups between 1300 and 1799 are considered different based on
        // steps of 50 instead of 100 (default).
        if (a.groupID >= 1300 && a.groupID < 1800 && b.groupID >= 1300 && b.groupID < 1800) {
            groupStep = 50;
        }

        return (a.getGroupID() / groupStep == b.getGroupID() / groupStep);
    }

    /**
     * Returns an array of Strings each containing the title for the corresponding entry in array
     * <code>entries[]</code>.
     *
     * @return java.lang.String[] : Array containing column titles
     */
    public String[] getColumnTitles() {

        return columns.clone();

    }

    /**
     * Get an optional description of this Reporter's Reportable to be included in the report. Default is null.
     *
     * @return String : Description
     */
    public String getDescription() {
        return this.source.getDescription();
    }

    /**
     * Returns an array of strings each containing the data for the corresponding column in array
     * <code>columns[]</code>. Implement this method the an array of the same length as the columntitles is produced
     * containing the data at the point of time this method is called by someone else to produce up-to-date
     * information.
     *
     * @return java.lang.String[] : Array containing the data for reporting
     */
    public abstract String[] getEntries();

    /**
     * Returns the ID of the group this reporter belongs to. The group-ID is used to give reporters a key to be ordered
     * by. This allows reporters of the same group to be printed together in one table in a report file.
     *
     * @return int : The reporter's group ID
     */
    public int getGroupID() {

        return groupID;

    }

    /**
     * Returns the heading for the group this reporter belongs to. This can easily be used as a heading in the reporter
     * to introduce a new group of reporters.
     *
     * @return java.lang.String : The group heading for this group of reporters
     */
    public String getHeading() {

        return groupHeading;

    }

    /**
     * Returns the Model that the Reportable belongs to that this reporter produces a report about.
     *
     * @return Model : The Model the reporter's reportable belongs to
     */
    public Model getModel() {

        return source.getModel();

    }

    /* @TODO: Comment */
    public int getNumOfSlaveQueues() {
        return 0;
    }

    /**
     * Returns the reportable object that this reporter contains informations about.
     *
     * @return desmoj.core.simulator.Reportable : The reportable this reporter carries information about
     */
    public Reportable getReportable() {

        return source;

    }

    /* @TODO: Comment */
    public boolean isContinuingReporter() {
        return false;
    }

    /* @TODO: Comment */
    public boolean isTwoRowReporter() {
        return false;
    }

    /* @TODO: Comment */
    public boolean makeAdditionalColorEntryIfHTMLColorChartIsGenerated() {
        return false;
    }

    /**
     * Returns the number of columns or data this reporter is containing.
     *
     * @return int : The number of columns/data this reporter is containing
     */
    public int numColumns() {

        return numColumns;

    }
}
