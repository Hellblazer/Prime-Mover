package com.hellblazer.primeMover.soot.util;

import static java.util.Arrays.asList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.annotation.j5anno.AnnotationGenerator;
import soot.tagkit.AbstractHost;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationClassElem;
import soot.tagkit.AnnotationConstants;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

public class Utils {
    private static final String BLOCKING_CLASS_BINARY_NAME = "Lcom/hellblazer/primeMover/Blocking;";

    private static final String CONTINUABLE_CLASS_BINARY_NAME = "Lcom/hellblazer/primeMover/Continuable;";

    private static final String ENTITY_CLASS_BINARY_NAME = "Lcom/hellblazer/primeMover/Entity;";

    private static SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private static Logger log = Logger.getLogger(Utils.class.getCanonicalName());

    private static final String NON_EVENT_CLASS_BINARY_NAME = "Lcom/hellblazer/primeMover/NonEvent;";

    private static final String TRANSFORMED_CLASS_BINARY_NAME = "Lcom/hellblazer/primeMover/Transformed;";

    public static Collection<SootMethod> gatherImplementationsOf(SootMethod method, SootClass base) {
        ArrayList<SootMethod> implementations = new ArrayList<SootMethod>();
        SootClass current = base;
        String subSignature = method.getSubSignature();
        while (current != null) {
            SootMethod impl = current.getMethod(subSignature);
            if (impl != null) {
                implementations.add(impl);
            }
            current = current.hasSuperclass() ? current.getSuperclass() : null;
        }
        return implementations;
    }

    public static Collection<SootClass> getEntityInterfaces(SootClass base) {
        Set<SootClass> interfaces = new HashSet<SootClass>();
        SootClass current = base;
        while (current != null) {
            gatherEntityInterfacesOf(current, interfaces);
            current = current.hasSuperclass() ? current.getSuperclass() : null;
        }
        return interfaces;
    }

    public static boolean isEntity(SootClass base) {
        if (isMarkedEntity(base)) {
            return true;
        }
        if (base.getName().endsWith("$gen")) {
            return false;
        }
        if (inferEntity(base)) {
            log.info(String.format("Inferred Entity status of %1s.  Marking class as @Entity", base));
            markEntity(base);
            markTransformed(base, "EntityInference", "Entity Status Inferred");
            return true;
        }
        return false;
    }

    public static boolean isMarkedBlocking(SootMethod method) {
        for (Tag tag : method.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) tag;
                if (vTag.getVisibility() == AnnotationConstants.RUNTIME_VISIBLE) {
                    for (AnnotationTag aTag : vTag.getAnnotations()) {
                        if (aTag.getType().equals(BLOCKING_CLASS_BINARY_NAME)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isMarkedContinuable(SootMethod method) {
        for (Tag tag : method.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) tag;
                if (vTag.getVisibility() == AnnotationConstants.RUNTIME_VISIBLE) {
                    for (AnnotationTag aTag : vTag.getAnnotations()) {
                        if (aTag.getType().equals(CONTINUABLE_CLASS_BINARY_NAME)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isMarkedNonEvent(SootMethod method) {
        for (Tag tag : method.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) tag;
                if (vTag.getVisibility() == AnnotationConstants.RUNTIME_VISIBLE) {
                    for (AnnotationTag aTag : vTag.getAnnotations()) {
                        if (aTag.getType().equals(NON_EVENT_CLASS_BINARY_NAME)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static void markBlocking(SootMethod method) {
        AnnotationGenerator.v()
                           .annotate(method, BLOCKING_CLASS_BINARY_NAME, AnnotationConstants.RUNTIME_VISIBLE,
                                     Collections.EMPTY_LIST);
    }

    @SuppressWarnings("unchecked")
    public static void markContinuable(SootMethod method) {
        AnnotationGenerator.v()
                           .annotate(method, CONTINUABLE_CLASS_BINARY_NAME, AnnotationConstants.RUNTIME_VISIBLE,
                                     Collections.EMPTY_LIST);
    }

    @SuppressWarnings("unchecked")
    public static void markEntity(SootClass clazz) {
        AnnotationGenerator.v()
                           .annotate(clazz, ENTITY_CLASS_BINARY_NAME, AnnotationConstants.RUNTIME_VISIBLE,
                                     Collections.EMPTY_LIST);
    }

    public static void markTransformed(AbstractHost host, Object generator) {
        markTransformed(host, generator, "");
    }

    public static void markTransformed(AbstractHost host, Object generator, String comment) {
        markTransformed(host, generator.getClass().getCanonicalName(), comment);
    }

    public static void markTransformed(AbstractHost host, String transformer, String comment) {
        List<AnnotationElem> elems = new ArrayList<AnnotationElem>(asList(new AnnotationStringElem(transformer, 's',
                                                                                                   "value"),
                                                                          new AnnotationStringElem(comment, 's',
                                                                                                   "comment"),
                                                                          new AnnotationStringElem(ISO8601FORMAT.format(new Date()),
                                                                                                   's', "date")));
        AnnotationGenerator.v()
                           .annotate(host, TRANSFORMED_CLASS_BINARY_NAME, AnnotationConstants.RUNTIME_VISIBLE, elems);
    }

    public static boolean willContinue(SootMethod method) {
        for (Tag tag : method.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) tag;
                if (vTag.getVisibility() == AnnotationConstants.RUNTIME_VISIBLE) {
                    for (AnnotationTag aTag : vTag.getAnnotations()) {
                        String type = aTag.getType();
                        if (type.equals(CONTINUABLE_CLASS_BINARY_NAME) || type.equals(BLOCKING_CLASS_BINARY_NAME)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean inferEntity(SootClass base) {
        if (base == null) {
            return false;
        }
        if (base.equals(Scene.v().loadClass(Object.class.getCanonicalName(), SootClass.SIGNATURES))) {
            return false;
        }
        if (base.equals(Scene.v()
                             .loadClass("com.hellblazer.primeMover.runtime.EntityReference", SootClass.SIGNATURES))) {
            return false;
        }
        if (isMarkedEntity(base)) {
            return true;
        }
        for (SootClass iFace : base.getInterfaces()) {
            if (inferEntity(iFace)) {
                return true;
            }
        }
        return base.hasSuperclass() ? inferEntity(base.getSuperclass()) : false;
    }

    static boolean isMarkedEntity(SootClass base) {
        for (Tag tag : base.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) tag;
                if (vTag.getVisibility() == AnnotationConstants.RUNTIME_VISIBLE) {
                    for (AnnotationTag aTag : vTag.getAnnotations()) {
                        if (aTag.getType().equals(ENTITY_CLASS_BINARY_NAME)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void gatherEntityInterfacesOf(SootClass base, Set<SootClass> interfaces) {
        for (Tag tag : base.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) tag;
                if (vTag.getVisibility() == AnnotationConstants.RUNTIME_VISIBLE) {
                    for (AnnotationTag aTag : vTag.getAnnotations()) {
                        if (aTag.getType().equals(ENTITY_CLASS_BINARY_NAME)) {
                            if (aTag.getElems().size() != 0) {
                                AnnotationArrayElem values = (AnnotationArrayElem) new ArrayList<>(aTag.getElems()).get(0);
                                for (AnnotationElem element : values.getValues()) {
                                    AnnotationClassElem classElement = (AnnotationClassElem) element;
                                    String desc = classElement.getDesc();
                                    String className = desc.substring(1, desc.length() - 1).replace('/', '.');
                                    SootClass iFace = Scene.v().loadClassAndSupport(className);
                                    if (interfaces.add(iFace)) {
                                        gatherInterfaceExtensions(iFace, interfaces);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void gatherInterfaceExtensions(SootClass base, Set<SootClass> interfaces) {
        for (SootClass extension : base.getInterfaces()) {
            if (interfaces.add(extension)) {
                gatherInterfaceExtensions(extension, interfaces);
            }
        }
    }
}
