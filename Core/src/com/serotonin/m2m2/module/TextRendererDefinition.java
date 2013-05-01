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
    /**
     * Returns the text renderer implementation definition
     * @return
     */
    public abstract ImplDefinition getTextRendererDefinition();
    
    
    /**
     * Returns the modules TextRenderer subclass
     * 
     * It is essential to also register the TextRenderer subclass as a DWR conversion
     * i.e. extend DwrConversionDefinition and addConversion(YourTextRenderer.class, "beanWithJs")
     * 
     * @return
     */
    public abstract Class<? extends TextRenderer> getTextRendererClass();
    
    
    /**
     * Returns the JSP include path for the form items used to 
     * set the text renderer's properties
     * @return
     */
    public abstract String getIncludePath();
    
    /**
     * Gets the Javascript function name used for saving the text renderer
     * The function should be on the JSP page included using getIncludePath()
     * @return
     */
    public abstract String getSaveAction();
    
    /**
     * Gets the text renderer name
     * @return
     */
    public String getName() {
        return getTextRendererDefinition().getName();
    }
}
