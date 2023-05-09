/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
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

package com.hellblazer.primeMover.soot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.runtime.Framework;

import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

/**
 * Test the transformation which substitutes new instances of the generated
 * proxies for the instances of the Entity.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class TestEntityConstructionTransformer {

    @Test
    public void testSubstitution() throws Exception {
        // TODO Fix me.
        if (Boolean.parseBoolean("true")) {
            return;
        }
        G.reset();
        Options.v().set_debug_resolver(true);
        Options.v().set_debug(true);
        Options.v().set_verbose(true);

        ArrayList<SootClass> classes = new ArrayList<SootClass>();
        EntityGenerator generator = new EntityGenerator(classes,
                                                        Scene.v()
                                                             .loadClassAndSupport(PrototypicalEntityImpl.class.getCanonicalName()),
                                                        true);
        generator.generateEntity();
        EntityConstructionTransformer creationTransformer = new EntityConstructionTransformer(true);
        SootClass createEntity = Scene.v().loadClassAndSupport(CreateEntityImpl.class.getCanonicalName());
        for (SootMethod method : createEntity.getMethods()) {
            creationTransformer.transform(method.retrieveActiveBody());
        }

        classes.add(createEntity);
        Scene.v().loadNecessaryClasses();
        ClassLoader loader = new LocalLoader(classes);

        MockController controller = new MockController();
        Framework.setController(controller);

        Class<?> createEntityClass = loader.loadClass(CreateEntityImpl.class.getCanonicalName());
        assertNotSame(CreateEntityImpl.class, createEntityClass);

        @SuppressWarnings("deprecation")
        CreateEntity create = (CreateEntity) createEntityClass.newInstance();

        assertNotSame(PrototypicalEntityImpl.class, create.getEntityA().getClass());
        assertNotSame(PrototypicalEntityImpl.class, create.getEntityB().getClass());
        assertNotSame(PrototypicalEntityImpl.class, create.getENTITY_A().getClass());
        assertNotSame(PrototypicalEntityImpl.class, create.getENTITY_B().getClass());

        PrototypicalEntityImpl entity = create.testMe();
        assertNotNull(entity);
        assertNotSame(PrototypicalEntityImpl.class, entity.getClass());
    }
}
