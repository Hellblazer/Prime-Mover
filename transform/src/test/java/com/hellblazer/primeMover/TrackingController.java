/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.EventImpl;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class TrackingController extends ControllerImpl {

    public List<String>          blockingEvents = new CopyOnWriteArrayList<String>();
    public List<String>          events     = new CopyOnWriteArrayList<String>();
    public List<EntityReference> references = new CopyOnWriteArrayList<EntityReference>();

    @Override
    public Object postContinuingEvent(EntityReference entity, int event, Object... arguments) throws Throwable {
        blockingEvents.add(entity.__signatureFor(event));
        references.add(entity);
        return super.postContinuingEvent(entity, event, arguments);
    }

    @Override
    protected void post(EventImpl event) {
        events.add(event.getSignature());
        references.add(event.getReference());
        super.post(event);
    }
}
