package desmoj.core.util;

/**
 * An interface for listeners interested in status changes during an experiment
 * run. An ExperimentRunListener can register with an ExperimentRunner and
 * becomes notified when the assigned simulation experiment is (Re)started,
 * paused or finally stopped.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Nicolas Knaak
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

public interface ExperimentListener extends SimRunListener {

    /** Should react on the experiment being temporarily paused */
    public void experimentPaused(SimRunEvent e);

    /** Should react on the experiment being (re)started */
    public void experimentRunning(SimRunEvent e);

    /** Should react on the experiment being finally stopped */
    public void experimentStopped(SimRunEvent e);
}
