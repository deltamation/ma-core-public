/*
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */

package com.serotonin.propertyEditor;

import java.beans.PropertyEditorSupport;

import javax.measure.unit.Unit;

import com.serotonin.m2m2.util.UnitUtil;

/**
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */
public class UnitEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(UnitUtil.parseLocal(text));
    }
    
    @Override
    public String getAsText() {
        Object value = getValue();
        if (value instanceof Unit<?>) {
            Unit<?> unit = (Unit<?>) value;
            return UnitUtil.formatLocal(unit);
        }
        return "";
    }
}
