package desmoj.core.simulator;

import co.paralleluniverse.common.util.SameThreadExecutor;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableRunnable;

/**
 * {@link SimStrandFactory} that uses {@link Fiber}s.
 * All fibers are executed in the main thread.
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
class FiberSimStrandFactory extends SimStrandFactory {
    
    private static final FiberExecutorScheduler FIBER_SCHEDULER = new FiberExecutorScheduler("fiber executor", SameThreadExecutor.getExecutor());
    
    /**
     * Nothing to do on cleanup.
     */
    @Override
    public void cleanUp() {
    }
    
    /**
     * {@inheritDoc}.
     */
    @Override
    public Strand create(SimProcess process, SuspendableRunnable runnable) {
        return create(process.getName(), runnable);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public Strand create(String name, SuspendableRunnable runnable) {
        return new Fiber<Void>(name, FIBER_SCHEDULER, runnable);
    }

}
