/*
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */

package com.serotonin.m2m2.util;

import java.text.ParseException;
import java.text.ParsePosition;

import javax.measure.quantity.Energy;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import com.serotonin.m2m2.Common;

/**
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */
public class UnitUtil {
    static final UnitFormat localFormat = UnitFormat.getInstance(Common.getLocale());
    static final UnitFormat ucumFormat = UnitFormat.getUCUMInstance();
    
    public static final Unit<Energy> BTU = SI.JOULE.times(1055.05585262D);
    public static final Unit<Energy> THERM = BTU.times(100000);
    public static final Unit<?> PSI = NonSI.POUND_FORCE.divide(NonSI.INCH.pow(2));
    
    static {
        // register some labels
        localFormat.label(BTU, "btu");
        localFormat.label(THERM, "thm");
        localFormat.label(PSI, "psi");
        localFormat.label(NonSI.REVOLUTION.divide(NonSI.MINUTE), "rpm");
        localFormat.label(Unit.ONE.divide(1000000), "ppm");
        localFormat.label(Unit.ONE.divide(1000000000), "ppb");
    }
    
    @SuppressWarnings("deprecation")
    public static Unit<?> convertToUnit(int engineeringUnit) {
        Unit<?> unit = EngineeringUnits.conversionMap.get(engineeringUnit);
        return (unit == null) ? Unit.ONE : unit;
    }
    
    public static String formatLocal(Unit<?> unit) {
        return localFormat.format(unit);
    }
    
    public static Unit<?> parseLocal(String unit) {
        try {
            return localFormat.parseProductUnit(unit, new ParsePosition(0));
        } catch (ParseException e) {
            try {
                return ucumFormat.parseProductUnit(unit, new ParsePosition(0));
            }
            catch (ParseException e2) {
                throw new IllegalArgumentException(e);
            }
        }
    }
    
    public static String formatUcum(Unit<?> unit) {
        return ucumFormat.format(unit);
    }
    
    public static Unit<?> parseUcum(String unit) {
        try {
            return ucumFormat.parseProductUnit(unit, new ParsePosition(0));
        } catch (ParseException e) {
            try {
                return localFormat.parseProductUnit(unit, new ParsePosition(0));
            }
            catch (ParseException e2) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
