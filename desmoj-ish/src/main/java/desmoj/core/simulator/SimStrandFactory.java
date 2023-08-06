package desmoj.core.simulator;




/**
 * An abstract factory for "Strands". Strands are quasar's abstraction of threads and fibers. 
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
abstract class SimStrandFactory {

    /**
     * Allow the subclasses to perform some cleanup when an experiment ends.
     */
    public abstract void cleanUp();

    /**
     * Create a {@link Strand} for the given {@link SimProcess} that will
     * execute the given {@link SuspendableRunnable} when started.
     */
    public abstract Strand create(SimProcess process, SuspendableRunnable runnable);

    /**
     * Create a {@link Strand} with the given name that will
     * execute the given {@link SuspendableRunnable} when started.
     */
    public abstract Strand create(String name, SuspendableRunnable runnable);
    
}
