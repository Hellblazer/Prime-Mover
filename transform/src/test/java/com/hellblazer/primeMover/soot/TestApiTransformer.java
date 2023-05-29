package com.hellblazer.primeMover.soot;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.Framework;

import soot.G;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

@SuppressWarnings("deprecation")
public class TestApiTransformer {

    static class MyMockController extends MockController {
        boolean continuationPosted = false;

        @Override
        public Object postContinuingEvent(EntityReference entity, int event, Object... arguments) throws Throwable {
            assertEquals("com.hellblazer.primeMover.runtime.BlockingSleep", entity.getClass().getCanonicalName());
            assertEquals(0, event);
            assertNotNull(arguments);
            assertEquals(1, arguments.length);
            continuationPosted = true;
            return null;
        }
    }

//    @Test
    public void testTransform() throws Exception {
        G.reset();
        SimulationTransform.setStandardClassPath();
        Options.v().set_keep_line_number(true);
        PhaseOptions.v().setPhaseOption("tag.ln", "on");
        SootClass apiUserClass = Scene.v().loadClassAndSupport(ApiUserImpl.class.getCanonicalName());
        ApiTransformer transformer = new ApiTransformer();
        for (SootMethod method : apiUserClass.getMethods()) {
            transformer.transform(method.retrieveActiveBody());
        }

        Scene.v().loadNecessaryClasses();
        LocalLoader loader = new LocalLoader(asList(apiUserClass));
        Class<?> clazz = loader.loadClass(ApiUserImpl.class.getCanonicalName());
        assertNotSame(ApiUserImpl.class, clazz);

        MyMockController controller = new MyMockController();
        ApiUser apiUser = (ApiUser) clazz.newInstance();
        Framework.setController(controller);

        apiUser.blockingSleep();
        assertTrue(controller.continuationPosted);
    }
}
