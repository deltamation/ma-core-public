/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.view.conversion;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.util.EngineeringUnits;

/**
 * @author Matthew Lohbihler
 * 
 */
public class Conversions {
    private static final Map<ConversionType, Conversion> availableConversions = new HashMap<ConversionType, Conversion>();

    private static final LinearConversion DEGREES_CELSIUS_TO_DEGREES_FAHRENHEIT = new LinearConversion(1.8, 32);
    private static final LinearConversion DEGREES_FAHRENHEIT_TO_DEGREES_CELSIUS = DEGREES_CELSIUS_TO_DEGREES_FAHRENHEIT
            .getInverse();
    
    private static final LinearConversion TIMES_BY_ONE_THOUSAND = new LinearConversion(1000, 0);
    private static final LinearConversion DIVIDE_BY_ONE_THOUSAND = TIMES_BY_ONE_THOUSAND.getInverse();
    private static final LinearConversion TIMES_BY_ONE_MILLION = new LinearConversion(1000000, 0);
    private static final LinearConversion DIVIDE_BY_ONE_MILLION = TIMES_BY_ONE_MILLION.getInverse();
    private static final LinearConversion TIMES_BY_ONE_BILLION = new LinearConversion(1000000000, 0);
    private static final LinearConversion DIVIDE_BY_ONE_BILLION = TIMES_BY_ONE_BILLION.getInverse();
    
    private static final LinearConversion MINUTES_TO_SECONDS = new LinearConversion(60, 0);
    private static final LinearConversion SECONDS_TO_MINUTES = MINUTES_TO_SECONDS.getInverse();
    private static final LinearConversion HOURS_TO_SECONDS = new LinearConversion(3600, 0);
    private static final LinearConversion SECONDS_TO_HOURS = HOURS_TO_SECONDS.getInverse();
    
    private static final LinearConversion PER_MINUTE_TO_PER_SECOND = SECONDS_TO_MINUTES;
    private static final LinearConversion PER_HOUR_TO_PER_SECOND = SECONDS_TO_HOURS;

    static {
        availableConversions.put(
                new ConversionType(EngineeringUnits.degreesFahrenheit, EngineeringUnits.degreesCelsius),
                DEGREES_FAHRENHEIT_TO_DEGREES_CELSIUS);
        availableConversions.put(
                new ConversionType(EngineeringUnits.degreesCelsius, EngineeringUnits.degreesFahrenheit),
                DEGREES_CELSIUS_TO_DEGREES_FAHRENHEIT);
        availableConversions.put(
                new ConversionType(EngineeringUnits.milliamperes, EngineeringUnits.amperes),
                DIVIDE_BY_ONE_THOUSAND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.milliwatts, EngineeringUnits.watts),
                DIVIDE_BY_ONE_THOUSAND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.kilowatts, EngineeringUnits.watts),
                TIMES_BY_ONE_THOUSAND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.megawatts, EngineeringUnits.watts),
                TIMES_BY_ONE_BILLION);
        availableConversions.put(
                new ConversionType(EngineeringUnits.gramsPerMinute, EngineeringUnits.gramsPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.kilogramsPerMinute, EngineeringUnits.kilogramsPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.kilogramsPerHour, EngineeringUnits.kilogramsPerSecond),
                PER_HOUR_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.tonnesPerMinute, EngineeringUnits.tonnesPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.tonnesPerHour, EngineeringUnits.tonnesPerSecond),
                PER_HOUR_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.poundsMassPerMinute, EngineeringUnits.poundsMassPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.poundsMassPerHour, EngineeringUnits.poundsMassPerSecond),
                PER_HOUR_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.millimetersPerMinute, EngineeringUnits.millimetersPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.metersPerMinute, EngineeringUnits.metersPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.metersPerHour, EngineeringUnits.metersPerSecond),
                PER_HOUR_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.cubicFeetPerMinute, EngineeringUnits.cubicFeetPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.cubicMetersPerMinute, EngineeringUnits.cubicMetersPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.cubicMetersPerHour, EngineeringUnits.cubicMetersPerSecond),
                PER_HOUR_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.litersPerMinute, EngineeringUnits.litersPerSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.litersPerHour, EngineeringUnits.litersPerSecond),
                PER_HOUR_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.litersPerSecond, EngineeringUnits.cubicMetersPerSecond),
                DIVIDE_BY_ONE_THOUSAND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.litersPerMinute, EngineeringUnits.cubicMetersPerMinute),
                DIVIDE_BY_ONE_THOUSAND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.litersPerHour, EngineeringUnits.cubicMetersPerHour),
                DIVIDE_BY_ONE_THOUSAND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.perMinute, EngineeringUnits.perSecond),
                PER_MINUTE_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.perHour, EngineeringUnits.perSecond),
                PER_HOUR_TO_PER_SECOND);
        availableConversions.put(
                new ConversionType(EngineeringUnits.kilometersPerHour, EngineeringUnits.kilometersPerSecond),
                PER_HOUR_TO_PER_SECOND);
    }

    public static Conversion getConversion(EngineeringUnits from, EngineeringUnits to) {
        return getConversion(from.getValue(), to.getValue());
    }

    public static Conversion getConversion(Integer from, Integer to) {
        return availableConversions.get(new ConversionType(from, to));
    }

    public static DataValue convert(EngineeringUnits from, EngineeringUnits to, DataValue value) {
        return convert(from.getValue(), to.getValue(), value);
    }

    public static DataValue convert(Integer from, Integer to, DataValue value) {
        double d = convert(from, to, value.getDoubleValue());
        return new NumericValue(d);
    }

    public static double convert(EngineeringUnits from, EngineeringUnits to, double value) {
        return convert(from.getValue(), to.getValue(), value);
    }

    public static Double convert(Integer from, Integer to, double value) {
        if (from.equals(to)) {
            return value;
        }
        
        Conversion conversion = getConversion(from, to);
        if (conversion == null)
            return null;
        return conversion.convert(value);
    }

    public static double fahrenheitToCelsius(double fahrenheit) {
        return DEGREES_FAHRENHEIT_TO_DEGREES_CELSIUS.convert(fahrenheit);
    }

    public static double celsiusToFahrenheit(double celsius) {
        return DEGREES_CELSIUS_TO_DEGREES_FAHRENHEIT.convert(celsius);
    }

    static class ConversionType {
        private final Integer from;
        private final Integer to;

        public ConversionType(EngineeringUnits from, EngineeringUnits to) {
            this(from.getValue(), to.getValue());
        }

        public ConversionType(Integer from, Integer to) {
            this.from = from;
            this.to = to;
        }

        public Integer getFrom() {
            return from;
        }

        public Integer getTo() {
            return to;
        }
        
        @Override
        public int hashCode() {
            int result = 1;
            result = 37 * result + from;
            result = 37 * result + to;
            return result;
        }
        
        @Override
        public boolean equals(Object that) {
            ConversionType b = (ConversionType) that;
            return (this.from.equals(b.from) && this.to.equals(b.to));
        }
    }

    private Conversions() {
        // Static methods only
    }
}
