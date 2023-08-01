package desmoj.core.simulator;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableRunnable;

/**
 * {@link SimStrandFactory} that uses normal threads.
 * All threads are created from the same thread group.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tobias Baum
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
class ThreadSimStrandFactory extends SimStrandFactory {

    /**
     * A thread subclass that saves the {@link SimProcess} for the thread, so that it can be unparked during cleanup.
     */
    private static final class ThreadWithSimProcess extends Thread {

        private final SimProcess simProcess;

        public ThreadWithSimProcess(ThreadGroup threadGroup, Runnable runnable, SimProcess process) {
            super(threadGroup, runnable, process.getName());
            this.simProcess = process;
        }

    }

    private final ThreadGroup threadGroup;

    public ThreadSimStrandFactory(String experimentName) {
        this.threadGroup = new ThreadGroup(experimentName);
    }

    /**
     * Unpark all threads that somehow survived the experiment's end.
     */
    public void cleanUp() {
        final Thread[] survivors = new Thread[this.threadGroup.activeCount()];
        this.threadGroup.enumerate(survivors);

        for (final Thread survivor : survivors) {

            // print existing threads for controlling purposes only
            // System.out.println(survivors[i]);

            // if we get the enumeration of survivors, some of them
            // might not have made it until here and die in between
            // so an occasional NullPointerException is perfectly
            // alright and no reason to worry -> we just dump it.
            if (survivor instanceof ThreadWithSimProcess) {
                try             {
                    ((ThreadWithSimProcess) survivor).simProcess.unpark();
                } catch (final NullPointerException e) {
                    ; // forget it anyway...
                } catch (SuspendExecution e) {
                    throw new RuntimeException("should not happen because we are in thread mode", e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}.
     */
    public Strand create(SimProcess process, SuspendableRunnable runnable) {
        return Strand.of(new ThreadWithSimProcess(this.threadGroup, Strand.toRunnable(runnable), process));
    }
    
    /**
     * {@inheritDoc}.
     */
    @Override
    public Strand create(String name, SuspendableRunnable runnable) {
        return Strand.of(new Thread(this.threadGroup, Strand.toRunnable(runnable), name));        
    }

}
