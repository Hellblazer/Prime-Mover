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

import java.util.Map;

/**
 * A class loader to facilitate the loading of transformed classes.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class LocalLoader extends ClassLoader {

    public Map<String, byte[]> classBits;

    public LocalLoader(Map<String, byte[]> classBits) {
        this.classBits = classBits;
    }

    public boolean transformed(String name) {
        return classBits.containsKey(name.replace('.', '/'));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loaded = findLoadedClass(name);
        if (loaded != null) {
            return loaded;
        }
        byte[] bits = classBits.get(name.replace('.', '/'));
        if (bits != null) {
            return defineClass(name, bits, 0, bits.length);
        } else {
            return super.loadClass(name, resolve);
        }
    }
}
