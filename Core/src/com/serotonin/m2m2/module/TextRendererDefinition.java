/*
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */

package com.serotonin.m2m2.module;

import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.m2m2.view.text.TextRenderer;

/**
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */
public abstract class TextRendererDefinition extends ModuleElementDefinition {
    public abstract ImplDefinition getTextRendererDefinition();
    public abstract Class<? extends TextRenderer> getTextRendererClass();
}
