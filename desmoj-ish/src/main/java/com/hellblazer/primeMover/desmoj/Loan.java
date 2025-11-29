/**
 * Copyright (C) 2024 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.desmoj;

/**
 * AutoCloseable wrapper for try-with-resources pattern.
 * Automatically releases resources when closed.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class Loan implements AutoCloseable {
    private final ResourceToken token;
    private boolean released = false;
    
    public Loan(ResourceToken token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        this.token = token;
    }
    
    @Override
    public void close() {
        if (!released) {
            token.resource().release(token);
            released = true;
        }
    }
    
    public ResourceToken token() {
        return token;
    }
}
