/**
 * (C) Copyright 2008 Chiral Behaviors, LLC. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.chiralbehaviors.janus.testClasses;

import com.chiralbehaviors.janus.Facet;
import com.chiralbehaviors.janus.This;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class MixIn1Impl implements MixIn1 {
    @This
    Composite1 composite;

    @Facet
    MixIn2 friend;

    @Override
    public Composite1 getComposite() {
        return composite;
    }

    @Override
    public MixIn2 getFriend1() {
        return friend;
    }

    @Override
    public String m11() {
        return "MixIn1-Method1";
    }

    @Override
    public String m12() {
        return "MixIn1-Method2";
    }

    @Override
    public String m13(String arg1, String arg2, String arg3) {
        return arg2;
    }
}
