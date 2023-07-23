/*
 * Copyright (C) 2023 Hal Hildebrand. All rights reserved.
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
package com.hellblazer.primeMover.asm.testClasses;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.Framework;

/**
 * @author hal.hildebrand
 */
@Entity(Foo.class)
public class Template implements Foo, EntityReference {

    private final Devi __controller = Framework.getController();

    @Override
    public void __bindTo(Devi controller) {

    }

    @Override
    public Object __invoke(int event, Object[] arguments) throws Throwable {
        switch (event) {
        case 0: {
            original_bar();
            return null;
        }
        case 1: {
            return original_myMy();
        }
        case 2: {
            return original_someArgs((String) arguments[0], arguments[1]);
        }
        default:
            throw new IllegalArgumentException("Unknown event: " + event);
        }
    }

    @Override
    public String __signatureFor(int event) {
        switch (event) {
        case 0: {
            return "A";
        }
        case 1: {
            return "B";
        }
        case 2: {
            return "C";
        }
        default:
            throw new IllegalArgumentException("unknown event key");
        }
    }

    @Override
    public void bar() {
        __controller.postEvent(this, 0);
    }

    @Override
    @Blocking
    public String myMy() {
        try {
            return (String) __controller.postContinuingEvent(this, 1);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String[] someArgs(String arg1, Object arg2) throws RuntimeException {
        try {
            return (String[]) __controller.postContinuingEvent(this, 2, arg1, arg2);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private void original_bar() {
        System.out.println("Hello");
    }

    private String original_myMy() {
        return "bar";
    }

    private String[] original_someArgs(String arg1, Object arg2) throws RuntimeException {
        return new String[] { "hello", "world" };
    }

}
