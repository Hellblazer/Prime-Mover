package desmoj.core.advancedModellingFeatures.report;

/**
 * Captures all relevant information about the <code>WaitQueue</code>. That
 * means from the master and the slave queue.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * @author edited by Lorna Slawski (added removed processes)
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

public class WaitQueueReporter extends desmoj.core.report.Reporter {

    // ****** methods ******

    /**
     * Constructor for a new WaitQueueReporter. Note that although any
     * Reportable is accepted you should make sure that only subtypes of
     * WaitQueue are passed to this constructor. Otherwise the number of column
     * titles and their individual headings will differ from the actual content
     * collected by this reporter.
     * 
     * @param informationSource
     *            desmoj.core.simulator.Reportable : The WaitQueue to report about
     */
    public WaitQueueReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource); // make a Reporter

        groupHeading = "Wait-Queues";
        groupID = 611; // see Reporter for more information about groupID

        numColumns = 13;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "Qorder";
        columns[2] = "(Re)set";
        columns[3] = "CoopCompl";
        columns[4] = "QLimit";
        columns[5] = "Qmax";
        columns[6] = "Qnow";
        columns[7] = "Qavg.";
        columns[8] = "Zeros";
        columns[9] = "avg.Wait";
        columns[10] = "avg.Coop";
        columns[11] = "refused";
        columns[12] = "removed";

        entries = new String[numColumns * 2]; // entries of master and slave
        // queue plus a potential description
    }

    /**
     * Returns an array of Strings each containing the data for the
     * corresponding column in array <code>columns[]</code>. This method is
     * fetching the data from the master and the slave WaitQueue. Implement this
     * method in a way, that an array of the same length as the columntitles is
     * produced containing the data at the point of time this method is called
     * by someone else to produce up-to-date information.
     * 
     * @return java.lang.String[] : Array containing the data for reporting
     */
    public String[] getEntries() {
        
        if (source instanceof desmoj.core.advancedModellingFeatures.WaitQueue) {
            // the WaitQueue we report about (source = informationsource)
            desmoj.core.advancedModellingFeatures.WaitQueue wq = (desmoj.core.advancedModellingFeatures.WaitQueue) source;
            
            // *** entries of the master queue
            // Title
            entries[0] = wq.getName();
            // mQorder
            entries[1] = wq.getMQueueStrategy();
            // (Re)set
            entries[2] = wq.resetAt().toString();
            // Completed
            entries[3] = Long.toString(wq.getCooperationsCompleted());
            // Qlimit
            entries[4] = Long.toString(wq.getQueueLimit());
            if (wq.getQueueLimit() == Integer.MAX_VALUE) {
                entries[4] = "unlimit.";
            }
            // Qmax
            entries[5] = Long.toString(wq.mMaxLength());
            // Qnow
            entries[6] = Long.toString(wq.mLength());
            // Qavg.
            entries[7] = Double.toString(wq.mAverageLength());
            // Zeros
            entries[8] = Long.toString(wq.mZeroWaits());
            // avg.Wait
            entries[9] = wq.mAverageWaitTime().toString();
            // avg.Coop
            entries[10] = wq.mAverageCoopTime().toString();
            // refused
            entries[11] = Long.toString(wq.getMRefused());
            // removed          
            entries[12] = Long.toString(wq.getMRemoved());

            // *** entries of the slave queue
            // Title
            entries[13] = wq.getSlaveQueue().getName();
            // sQorder
            entries[14] = wq.getSQueueStrategy();
            // (Re)set
            entries[15] = wq.getSlaveQueue().resetAt().toString();
            // Completed
            entries[16] = Long.toString(wq.getCooperationsCompleted());
            // Qlimit
            entries[17] = Long.toString(wq.getSlaveQueue().getQueueLimit());
            if (wq.getSlaveQueue().getQueueLimit() == Integer.MAX_VALUE) {
                entries[17] = "unlimit.";
            }
            // Qmax
            entries[18] = Long.toString(wq.sMaxLength());
            // Qnow
            entries[19] = Long.toString(wq.sLength());
            // Qavg.
            entries[20] = Double.toString(wq.sAverageLength());
            // Zeros
            entries[21] = Long.toString(wq.sZeroWaits());
            // avg.Wait
            entries[22] = wq.sAverageWaitTime().toString();
            // avg.Coop
            entries[23] = wq.mAverageCoopTime().toString();
            // refused
            entries[24] = Long.toString(wq.getSRefused());
            // removed          
            entries[25] = Long.toString(wq.getSRemoved());           

        } else {
            for (int i = 0; i < numColumns * 2; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }
    
    /* @TODO: Comment */
    public boolean isTwoRowReporter() {
        return true;
    }
} // end class WaitQueueReporter