/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package testClasses;

/**
 * A class to test the transformation which substitutes creation of generated proxies instances for the creation of
 * simulation entity instances.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class CreateEntityImpl implements CreateEntity {

    public static PrototypicalEntityImpl ENTITY_A = new PrototypicalEntityImpl();
    public static PrototypicalEntityImpl ENTITY_B;

    static {
        ENTITY_B = new PrototypicalEntityImpl();
    }

    public PrototypicalEntityImpl entityA = new PrototypicalEntityImpl();
    public PrototypicalEntityImpl entityB;

    public CreateEntityImpl() {
        entityB = new PrototypicalEntityImpl();
    }

    @Override
    public PrototypicalEntityImpl getENTITY_A() {
        return ENTITY_A;
    }

    @Override
    public PrototypicalEntityImpl getENTITY_B() {
        return ENTITY_B;
    }

    @Override
    public PrototypicalEntityImpl getEntityA() {
        return entityA;
    }

    @Override
    public PrototypicalEntityImpl getEntityB() {
        return entityB;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.soot.CreateEntity#testMe()
     */
    @Override
    public PrototypicalEntityImpl testMe() {
        return new PrototypicalEntityImpl();
    }

}
