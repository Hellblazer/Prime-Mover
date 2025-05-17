package com.hellblazer.primeMover.classfile;

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

public record ClassRemapper(Function<ClassDesc, ClassDesc> mapFunction) implements ClassTransform {

    public static <T, U> List<U> mappedList(List<? extends T> list, Function<T, U> mapper) {
        return new AbstractList<>() {
            @Override
            public U get(int index) {
                return mapper.apply(list.get(index));
            }

            @Override
            public int size() {
                return list.size();
            }
        };
    }

    @Override
    public void accept(ClassBuilder clb, ClassElement cle) {
        switch (cle) {
            case FieldModel fm -> clb.withField(fm.fieldName().stringValue(), map(fm.fieldTypeSymbol()),
                                                fb -> fb.transform(fm, asFieldTransform()));
            case MethodModel mm -> clb.withMethod(mm.methodName().stringValue(), mapMethodDesc(mm.methodTypeSymbol()),
                                                  mm.flags().flagsMask(), mb -> mb.transform(mm, asMethodTransform()));
            case Superclass sc -> clb.withSuperclass(map(sc.superclassEntry().asSymbol()));
            case Interfaces ins -> clb.withInterfaceSymbols(mappedList(ins.interfaces(), in -> map(in.asSymbol())));
            case SignatureAttribute sa -> clb.with(SignatureAttribute.of(mapClassSignature(sa.asClassSignature())));
            case InnerClassesAttribute ica -> clb.with(InnerClassesAttribute.of(ica.classes()
                                                                                   .stream()
                                                                                   .map(ici -> InnerClassInfo.of(
                                                                                   map(ici.innerClass().asSymbol()),
                                                                                   ici.outerClass()
                                                                                      .map(oc -> map(oc.asSymbol())),
                                                                                   ici.innerName()
                                                                                      .map(Utf8Entry::stringValue),
                                                                                   ici.flagsMask()))
                                                                                   .toList()));
            case EnclosingMethodAttribute ema -> clb.with(
            EnclosingMethodAttribute.of(map(ema.enclosingClass().asSymbol()),
                                        ema.enclosingMethodName().map(Utf8Entry::stringValue),
                                        ema.enclosingMethodTypeSymbol().map(this::mapMethodDesc)));
            case RecordAttribute ra -> clb.with(
            RecordAttribute.of(ra.components().stream().map(this::mapRecordComponent).toList()));
            case ModuleAttribute ma -> clb.with(
            ModuleAttribute.of(ma.moduleName(), ma.moduleFlagsMask(), ma.moduleVersion().orElse(null), ma.requires(),
                               ma.exports(), ma.opens(),
                               ma.uses().stream().map(ce -> clb.constantPool().classEntry(map(ce.asSymbol()))).toList(),
                               ma.provides()
                                 .stream()
                                 .map(mp -> ModuleProvideInfo.of(map(mp.provides().asSymbol()), mp.providesWith()
                                                                                                  .stream()
                                                                                                  .map(pw -> map(
                                                                                                  pw.asSymbol()))
                                                                                                  .toList()))
                                 .toList()));
            case NestHostAttribute nha -> clb.with(NestHostAttribute.of(map(nha.nestHost().asSymbol())));
            case NestMembersAttribute nma -> clb.with(
            NestMembersAttribute.ofSymbols(nma.nestMembers().stream().map(nm -> map(nm.asSymbol())).toList()));
            case PermittedSubclassesAttribute psa -> clb.with(PermittedSubclassesAttribute.ofSymbols(
            psa.permittedSubclasses().stream().map(ps -> map(ps.asSymbol())).toList()));
            case RuntimeVisibleAnnotationsAttribute aa -> clb.with(
            RuntimeVisibleAnnotationsAttribute.of(mapAnnotations(aa.annotations())));
            case RuntimeInvisibleAnnotationsAttribute aa -> clb.with(
            RuntimeInvisibleAnnotationsAttribute.of(mapAnnotations(aa.annotations())));
            case RuntimeVisibleTypeAnnotationsAttribute aa -> clb.with(
            RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(aa.annotations())));
            case RuntimeInvisibleTypeAnnotationsAttribute aa -> clb.with(
            RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(aa.annotations())));
            default -> clb.with(cle);
        }
    }

    public CodeTransform asCodeTransform() {
        return (CodeBuilder cob, CodeElement coe) -> {
            switch (coe) {
                case FieldInstruction fai -> cob.fieldAccess(fai.opcode(), map(fai.owner().asSymbol()),
                                                             fai.name().stringValue(), map(fai.typeSymbol()));
                case InvokeInstruction ii -> cob.invoke(ii.opcode(), map(ii.owner().asSymbol()),
                                                        ii.name().stringValue(), mapMethodDesc(ii.typeSymbol()),
                                                        ii.isInterface());
                case InvokeDynamicInstruction idi -> cob.invokedynamic(
                DynamicCallSiteDesc.of(mapDirectMethodHandle(idi.bootstrapMethod()), idi.name().stringValue(),
                                       mapMethodDesc(idi.typeSymbol()), idi.bootstrapArgs()
                                                                           .stream()
                                                                           .map(this::mapConstantValue)
                                                                           .toArray(ConstantDesc[]::new)));
                case NewObjectInstruction c -> cob.new_(map(c.className().asSymbol()));
                case NewReferenceArrayInstruction c -> cob.anewarray(map(c.componentType().asSymbol()));
                case NewMultiArrayInstruction c -> cob.multianewarray(map(c.arrayType().asSymbol()), c.dimensions());
                case TypeCheckInstruction c -> cob.with(TypeCheckInstruction.of(c.opcode(), map(c.type().asSymbol())));
                case ExceptionCatch c -> cob.exceptionCatch(c.tryStart(), c.tryEnd(), c.handler(), c.catchType()
                                                                                                    .map(
                                                                                                    d -> ConstantPoolBuilder.of()
                                                                                                                            .classEntry(
                                                                                                                            map(
                                                                                                                            d.asSymbol()))));
                case LocalVariable c -> cob.localVariable(c.slot(), c.name().stringValue(), map(c.typeSymbol()),
                                                          c.startScope(), c.endScope());
                case LocalVariableType c -> cob.localVariableType(c.slot(), c.name().stringValue(),
                                                                  mapSignature(c.signatureSymbol()), c.startScope(),
                                                                  c.endScope());
                case ConstantInstruction.LoadConstantInstruction ldc -> cob.ldc(mapConstantValue(ldc.constantValue()));
                case RuntimeVisibleTypeAnnotationsAttribute aa -> cob.with(
                RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(aa.annotations())));
                case RuntimeInvisibleTypeAnnotationsAttribute aa -> cob.with(
                RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(aa.annotations())));
                default -> cob.with(coe);
            }
        };
    }

    public FieldTransform asFieldTransform() {
        return (FieldBuilder fb, FieldElement fe) -> {
            switch (fe) {
                case SignatureAttribute sa -> fb.with(SignatureAttribute.of(mapSignature(sa.asTypeSignature())));
                case RuntimeVisibleAnnotationsAttribute aa -> fb.with(
                RuntimeVisibleAnnotationsAttribute.of(mapAnnotations(aa.annotations())));
                case RuntimeInvisibleAnnotationsAttribute aa -> fb.with(
                RuntimeInvisibleAnnotationsAttribute.of(mapAnnotations(aa.annotations())));
                case RuntimeVisibleTypeAnnotationsAttribute aa -> fb.with(
                RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(aa.annotations())));
                case RuntimeInvisibleTypeAnnotationsAttribute aa -> fb.with(
                RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(aa.annotations())));
                default -> fb.with(fe);
            }
        };
    }

    public MethodTransform asMethodTransform() {
        return (MethodBuilder mb, MethodElement me) -> {
            switch (me) {
                case AnnotationDefaultAttribute ada -> mb.with(
                AnnotationDefaultAttribute.of(mapAnnotationValue(ada.defaultValue())));
                case CodeModel com -> mb.transformCode(com, asCodeTransform());
                case ExceptionsAttribute ea -> mb.with(
                ExceptionsAttribute.ofSymbols(ea.exceptions().stream().map(ce -> map(ce.asSymbol())).toList()));
                case SignatureAttribute sa -> mb.with(
                SignatureAttribute.of(mapMethodSignature(sa.asMethodSignature())));
                case RuntimeVisibleAnnotationsAttribute aa -> mb.with(
                RuntimeVisibleAnnotationsAttribute.of(mapAnnotations(aa.annotations())));
                case RuntimeInvisibleAnnotationsAttribute aa -> mb.with(
                RuntimeInvisibleAnnotationsAttribute.of(mapAnnotations(aa.annotations())));
                case RuntimeVisibleParameterAnnotationsAttribute paa -> mb.with(
                RuntimeVisibleParameterAnnotationsAttribute.of(
                paa.parameterAnnotations().stream().map(this::mapAnnotations).toList()));
                case RuntimeInvisibleParameterAnnotationsAttribute paa -> mb.with(
                RuntimeInvisibleParameterAnnotationsAttribute.of(
                paa.parameterAnnotations().stream().map(this::mapAnnotations).toList()));
                case RuntimeVisibleTypeAnnotationsAttribute aa -> mb.with(
                RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(aa.annotations())));
                case RuntimeInvisibleTypeAnnotationsAttribute aa -> mb.with(
                RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(aa.annotations())));
                default -> mb.with(me);
            }
        };
    }

    public ClassDesc map(ClassDesc desc) {
        if (desc == null) {
            return null;
        }
        if (desc.isArray()) {
            return map(desc.componentType()).arrayType();
        }
        if (desc.isPrimitive()) {
            return desc;
        }
        return mapFunction.apply(desc);
    }

    Annotation mapAnnotation(Annotation a) {
        return Annotation.of(map(a.classSymbol()), a.elements()
                                                    .stream()
                                                    .map(el -> AnnotationElement.of(el.name(),
                                                                                    mapAnnotationValue(el.value())))
                                                    .toList());
    }

    AnnotationValue mapAnnotationValue(AnnotationValue val) {
        return switch (val) {
            case AnnotationValue.OfAnnotation oa -> AnnotationValue.ofAnnotation(mapAnnotation(oa.annotation()));
            case AnnotationValue.OfArray oa -> AnnotationValue.ofArray(
            oa.values().stream().map(this::mapAnnotationValue).toList());
            case AnnotationValue.OfConstant oc -> oc;
            case AnnotationValue.OfClass oc -> AnnotationValue.ofClass(map(oc.classSymbol()));
            case AnnotationValue.OfEnum oe -> AnnotationValue.ofEnum(map(oe.classSymbol()),
                                                                     oe.constantName().stringValue());
        };
    }

    List<Annotation> mapAnnotations(List<Annotation> annotations) {
        return annotations.stream().map(this::mapAnnotation).toList();
    }

    ClassSignature mapClassSignature(ClassSignature signature) {
        return ClassSignature.of(mapTypeParams(signature.typeParameters()),
                                 mapSignature(signature.superclassSignature()), signature.superinterfaceSignatures()
                                                                                         .stream()
                                                                                         .map(this::mapSignature)
                                                                                         .toArray(
                                                                                         Signature.ClassTypeSig[]::new));
    }

    ConstantDesc mapConstantValue(ConstantDesc value) {
        return switch (value) {
            case ClassDesc cd -> map(cd);
            case DynamicConstantDesc<?> dcd -> mapDynamicConstant(dcd);
            case DirectMethodHandleDesc dmhd -> mapDirectMethodHandle(dmhd);
            case MethodTypeDesc mtd -> mapMethodDesc(mtd);
            default -> value;
        };
    }

    DirectMethodHandleDesc mapDirectMethodHandle(DirectMethodHandleDesc dmhd) {
        return switch (dmhd.kind()) {
            case GETTER, SETTER, STATIC_GETTER, STATIC_SETTER -> MethodHandleDesc.ofField(dmhd.kind(),
                                                                                          map(dmhd.owner()),
                                                                                          dmhd.methodName(), map(
            ClassDesc.ofDescriptor(dmhd.lookupDescriptor())));
            default -> MethodHandleDesc.ofMethod(dmhd.kind(), map(dmhd.owner()), dmhd.methodName(),
                                                 mapMethodDesc(MethodTypeDesc.ofDescriptor(dmhd.lookupDescriptor())));
        };
    }

    DynamicConstantDesc<?> mapDynamicConstant(DynamicConstantDesc<?> dcd) {
        return DynamicConstantDesc.ofNamed(mapDirectMethodHandle(dcd.bootstrapMethod()), dcd.constantName(),
                                           map(dcd.constantType()), dcd.bootstrapArgsList()
                                                                       .stream()
                                                                       .map(this::mapConstantValue)
                                                                       .toArray(ConstantDesc[]::new));
    }

    MethodTypeDesc mapMethodDesc(MethodTypeDesc desc) {
        return MethodTypeDesc.of(map(desc.returnType()),
                                 desc.parameterList().stream().map(this::map).toArray(ClassDesc[]::new));
    }

    MethodSignature mapMethodSignature(MethodSignature signature) {
        return MethodSignature.of(mapTypeParams(signature.typeParameters()),
                                  signature.throwableSignatures().stream().map(this::mapSignature).toList(),
                                  mapSignature(signature.result()),
                                  signature.arguments().stream().map(this::mapSignature).toArray(Signature[]::new));
    }

    RecordComponentInfo mapRecordComponent(RecordComponentInfo component) {
        return RecordComponentInfo.of(component.name().stringValue(), map(component.descriptorSymbol()),
                                      component.attributes().stream().map(atr -> switch (atr) {
                                          case SignatureAttribute sa -> SignatureAttribute.of(
                                          mapSignature(sa.asTypeSignature()));
                                          case RuntimeVisibleAnnotationsAttribute aa ->
                                          RuntimeVisibleAnnotationsAttribute.of(mapAnnotations(aa.annotations()));
                                          case RuntimeInvisibleAnnotationsAttribute aa ->
                                          RuntimeInvisibleAnnotationsAttribute.of(mapAnnotations(aa.annotations()));
                                          case RuntimeVisibleTypeAnnotationsAttribute aa ->
                                          RuntimeVisibleTypeAnnotationsAttribute.of(
                                          mapTypeAnnotations(aa.annotations()));
                                          case RuntimeInvisibleTypeAnnotationsAttribute aa ->
                                          RuntimeInvisibleTypeAnnotationsAttribute.of(
                                          mapTypeAnnotations(aa.annotations()));
                                          default -> atr;
                                      }).toList());
    }

    @SuppressWarnings("unchecked")
    <S extends Signature> S mapSignature(S signature) {
        return (S) switch (signature) {
            case Signature.ArrayTypeSig ats -> Signature.ArrayTypeSig.of(mapSignature(ats.componentSignature()));
            case Signature.ClassTypeSig cts -> Signature.ClassTypeSig.of(
            cts.outerType().map(this::mapSignature).orElse(null), map(cts.classDesc()),
            cts.typeArgs().stream().map(ta -> switch (ta) {
                case Signature.TypeArg.Unbounded u -> u;
                case Signature.TypeArg.Bounded bta -> Signature.TypeArg.bounded(bta.wildcardIndicator(),
                                                                                mapSignature(bta.boundType()));
            }).toArray(Signature.TypeArg[]::new));
            default -> signature;
        };
    }

    List<TypeAnnotation> mapTypeAnnotations(List<TypeAnnotation> typeAnnotations) {
        return typeAnnotations.stream().map(
        a -> TypeAnnotation.of(a.targetInfo(), a.targetPath(), mapAnnotation(a.annotation()))).toList();
    }

    List<Signature.TypeParam> mapTypeParams(List<Signature.TypeParam> typeParams) {
        return typeParams.stream()
                         .map(tp -> Signature.TypeParam.of(tp.identifier(), tp.classBound().map(this::mapSignature),
                                                           tp.interfaceBounds()
                                                             .stream()
                                                             .map(this::mapSignature)
                                                             .toArray(Signature.RefTypeSig[]::new)))
                         .toList();
    }
}
