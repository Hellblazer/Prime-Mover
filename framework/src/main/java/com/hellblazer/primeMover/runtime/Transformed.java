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

package com.hellblazer.primeMover.runtime;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a simulation transform has been applied to the
 * element. When used, the value element must have the name of the simulation
 * transformer. The convention is to use the fully qualified name of the
 * transformer in the value field. For example: com.company.package.classname.
 * <p>
 * The date element is used to indicate the date the element was transformed.
 * The date element must follow the ISO 8601 standard. For example the date
 * element would have the following value 2001-07-04T12:08:56.235-0700 which
 * represents 2001-07-04 12:08:56 local time in the U.S. Pacific Time time zone.
 * <p.>
 * The comment element is a place holder for any comments that the transformer
 * may want to include in the generated code.
 * <p>
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
@Retention(RUNTIME)
@Target({ METHOD, TYPE, CONSTRUCTOR })
public @interface Transformed {
    /**
     * A place holder for any comments that the transformer may want to include
     * in the transformed element.
     */
    String comment() default "";

    /**
     * Date when the element was transformed.
     */
    String date();

    /**
     * This is used by the code generator to mark the transformed classes,
     * methods and constructors
     */
    String value();
}
