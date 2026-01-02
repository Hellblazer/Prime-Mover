/*
 * Copyright (C) 2026 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover IntelliJ Plugin.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package com.hellblazer.primemover.intellij.jps;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;

import java.util.Collections;
import java.util.List;

/**
 * JPS BuilderService that registers the Prime Mover class instrumenter.
 * <p>
 * This service is discovered via META-INF/services and provides
 * the ModuleLevelBuilder that performs bytecode transformation
 * after Java compilation.
 */
public class PrimeMoverBuilderService extends BuilderService {

    @Override
    public @NotNull List<? extends ModuleLevelBuilder> createModuleLevelBuilders() {
        return Collections.singletonList(new PrimeMoverClassInstrumenter());
    }
}
