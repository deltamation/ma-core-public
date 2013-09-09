/*
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */

package com.serotonin.m2m2.view.text;

import java.text.DecimalFormat;

import javax.measure.unit.Unit;

import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */
public class IntegralRenderer extends AnalogRenderer {

    /**
     * @param format
     * @param point
     */
    public IntegralRenderer(String format, DataPointVO point) {
        this.format = format;
        this.point = point;
        useUnitAsSuffix = true;
    }

    @Override
    public String getText(double value, int hint) {
        Unit<?> unit = point.getIntegralUnit();
        
        if (useUnitAsSuffix)
            suffix = UnitUtil.formatLocal(unit);
        
        String raw = new DecimalFormat(format).format(value);
        if (hint == HINT_RAW || suffix == null)
            return raw;
        
        return raw + suffix;
    }
}
