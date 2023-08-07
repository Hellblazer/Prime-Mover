/**
 * (C) Copyright 2008 Chiral Behaviors, LLC. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chiralbehaviors.janus.testClasses;

import com.chiralbehaviors.janus.Facet;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class MixIn2Impl implements MixIn2 {
    public static String RESULT = null;

    @Facet
    MixIn1               friend;

    @Override
    public MixIn1 getFriend2() {
        return friend;
    }

    @Override
    public String m21() {
        return "MixIn2-Method1";
    }

    @Override
    public String m22() {
        return "MixIn2-Method2";
    }

    @Override
    public void m23(String arg) {
        RESULT = arg;
    }

    @Override
    public int m24() {
        return 0;
    }
}
