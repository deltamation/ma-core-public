/*
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */

package com.serotonin.m2m2.view.text;

/**
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */
public interface SuffixRenderer {
    public abstract String getSuffix();
    public abstract void setSuffix(String suffix);
    public abstract boolean isUseUnitAsSuffix();
    public abstract void setUseUnitAsSuffix(boolean useUnit);
}
