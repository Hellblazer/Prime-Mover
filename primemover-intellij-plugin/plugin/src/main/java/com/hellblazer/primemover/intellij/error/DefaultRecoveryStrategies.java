package com.hellblazer.primemover.intellij.error;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Default recovery strategies for common Prime Mover errors.
 */
public class DefaultRecoveryStrategies {

    /**
     * Strategy for annotation scanning failures.
     */
    public static class AnnotationScanningStrategy implements ErrorRecoveryStrategy {
        @Override
        public boolean canHandle(@NotNull PrimeMoverError error) {
            return error.getCategory() == ErrorCategory.ANNOTATION_SCANNING;
        }

        @Override
        public boolean attemptRecovery(@NotNull PrimeMoverError error) {
            // Annotation scanning errors typically require manual fixes
            return false;
        }

        @Override
        public @NotNull List<RecoveryAction> getRecoveryActions(@NotNull PrimeMoverError error) {
            return List.of(
                RecoveryAction.builder("Verify @Entity annotations are present and properly imported")
                    .technicalNote("Check for com.hellblazer.primeMover.annotations.Entity")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Ensure api module is in project dependencies")
                    .technicalNote("Check pom.xml or build.gradle for primemover-api dependency")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Run 'Invalidate Caches and Restart' from IDE")
                    .technicalNote("File > Invalidate Caches")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build()
            );
        }
    }

    /**
     * Strategy for transformation failures.
     */
    public static class TransformationStrategy implements ErrorRecoveryStrategy {
        @Override
        public boolean canHandle(@NotNull PrimeMoverError error) {
            return error.getCategory() == ErrorCategory.ENTITY_TRANSFORMATION;
        }

        @Override
        public boolean attemptRecovery(@NotNull PrimeMoverError error) {
            // Transformation errors need source code fixes
            return false;
        }

        @Override
        public @NotNull List<RecoveryAction> getRecoveryActions(@NotNull PrimeMoverError error) {
            return List.of(
                RecoveryAction.builder("Check that class is a valid entity (non-abstract, has accessible constructor)")
                    .technicalNote("@Entity classes must be concrete with public or package-private constructor")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Review method signatures for unsupported patterns")
                    .technicalNote("Avoid native methods, synchronized keyword in @Entity classes")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Enable Prime Mover debug logging")
                    .technicalNote("Settings > Prime Mover > Enable Debug Logging")
                    .type(RecoveryAction.RecoveryActionType.CONFIGURATION)
                    .build(),
                RecoveryAction.builder("Rebuild project with 'Build > Rebuild Project'")
                    .technicalNote("Forces full transformation cycle")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build()
            );
        }
    }

    /**
     * Strategy for ClassFile API errors.
     */
    public static class ClassFileApiStrategy implements ErrorRecoveryStrategy {
        @Override
        public boolean canHandle(@NotNull PrimeMoverError error) {
            return error.getCategory() == ErrorCategory.CLASSFILE_API;
        }

        @Override
        public boolean attemptRecovery(@NotNull PrimeMoverError error) {
            return false;
        }

        @Override
        public @NotNull List<RecoveryAction> getRecoveryActions(@NotNull PrimeMoverError error) {
            return List.of(
                RecoveryAction.builder("Verify Java 25+ is being used for compilation")
                    .technicalNote("ClassFile API requires Java 25 or newer")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Check class file format version compatibility")
                    .technicalNote("Source must be compiled with compatible bytecode version")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Report issue with malformed class file details")
                    .technicalNote("File GitHub issue with stack trace and class file")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build()
            );
        }
    }

    /**
     * Strategy for compilation errors in generated code.
     */
    public static class CompilationStrategy implements ErrorRecoveryStrategy {
        @Override
        public boolean canHandle(@NotNull PrimeMoverError error) {
            return error.getCategory() == ErrorCategory.COMPILATION;
        }

        @Override
        public boolean attemptRecovery(@NotNull PrimeMoverError error) {
            return false;
        }

        @Override
        public @NotNull List<RecoveryAction> getRecoveryActions(@NotNull PrimeMoverError error) {
            return List.of(
                RecoveryAction.builder("Check for missing dependencies in project configuration")
                    .technicalNote("Ensure runtime module is in dependencies")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Verify generated EntityReference interfaces compile")
                    .technicalNote("Check build output for generated source issues")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Clean and rebuild project")
                    .technicalNote("Build > Clean Project, then Build > Rebuild Project")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build()
            );
        }
    }

    /**
     * Strategy for sandbox violations.
     */
    public static class SandboxViolationStrategy implements ErrorRecoveryStrategy {
        @Override
        public boolean canHandle(@NotNull PrimeMoverError error) {
            return error.getCategory() == ErrorCategory.SANDBOX_VIOLATION;
        }

        @Override
        public boolean attemptRecovery(@NotNull PrimeMoverError error) {
            return false;
        }

        @Override
        public @NotNull List<RecoveryAction> getRecoveryActions(@NotNull PrimeMoverError error) {
            return List.of(
                RecoveryAction.builder("Check IDE file access permissions")
                    .technicalNote("Verify plugin can write to build output directories")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Review IDE sandbox settings")
                    .technicalNote("Settings > Advanced Settings > IDE Security")
                    .type(RecoveryAction.RecoveryActionType.CONFIGURATION)
                    .build(),
                RecoveryAction.builder("Try disabling other bytecode manipulation plugins")
                    .technicalNote("Conflicts with Lombok, AspectJ, or other instrumentation plugins")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build()
            );
        }
    }

    /**
     * Strategy for performance issues.
     */
    public static class PerformanceStrategy implements ErrorRecoveryStrategy {
        @Override
        public boolean canHandle(@NotNull PrimeMoverError error) {
            return error.getCategory() == ErrorCategory.PERFORMANCE;
        }

        @Override
        public boolean attemptRecovery(@NotNull PrimeMoverError error) {
            return false;
        }

        @Override
        public @NotNull List<RecoveryAction> getRecoveryActions(@NotNull PrimeMoverError error) {
            return List.of(
                RecoveryAction.builder("Increase IDE memory allocation")
                    .technicalNote("Help > Edit Custom VM Options, increase -Xmx value")
                    .type(RecoveryAction.RecoveryActionType.CONFIGURATION)
                    .build(),
                RecoveryAction.builder("Reduce scope of transformation (annotate fewer classes)")
                    .technicalNote("Remove @Entity from large classes or refactor into smaller entities")
                    .type(RecoveryAction.RecoveryActionType.MANUAL)
                    .build(),
                RecoveryAction.builder("Disable real-time transformation if enabled")
                    .technicalNote("Settings > Prime Mover > Disable Background Processing")
                    .type(RecoveryAction.RecoveryActionType.CONFIGURATION)
                    .build()
            );
        }
    }

    /**
     * Register all default strategies.
     */
    public static void registerAll(@NotNull ErrorRecoveryStrategy.Registry registry) {
        registry.register(new AnnotationScanningStrategy());
        registry.register(new TransformationStrategy());
        registry.register(new ClassFileApiStrategy());
        registry.register(new CompilationStrategy());
        registry.register(new SandboxViolationStrategy());
        registry.register(new PerformanceStrategy());
    }
}
