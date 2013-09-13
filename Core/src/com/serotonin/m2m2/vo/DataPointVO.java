/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.InvalidArgumentException;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.util.ChangeComparable;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.chart.ChartRenderer;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.view.text.ConvertingRenderer;
import com.serotonin.m2m2.view.text.NoneRenderer;
import com.serotonin.m2m2.view.text.PlainRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;
import com.serotonin.m2m2.vo.event.PointEventDetectorVO;
import com.serotonin.util.ColorUtils;
import com.serotonin.util.SerializationHelper;
import com.serotonin.validation.StringValidation;
import javax.measure.unit.SI;

public class DataPointVO implements Serializable, Cloneable, JsonSerializable, ChangeComparable<DataPointVO>,
        IDataPoint {
    private static final long serialVersionUID = -1;
    public static final String XID_PREFIX = "DP_";

    public interface LoggingTypes {
        int ON_CHANGE = 1;
        int ALL = 2;
        int NONE = 3;
        int INTERVAL = 4;
        int ON_TS_CHANGE = 5;
    }

    private static final ExportCodes LOGGING_TYPE_CODES = new ExportCodes();
    static {
        LOGGING_TYPE_CODES.addElement(LoggingTypes.ON_CHANGE, "ON_CHANGE", "pointEdit.logging.type.change");
        LOGGING_TYPE_CODES.addElement(LoggingTypes.ALL, "ALL", "pointEdit.logging.type.all");
        LOGGING_TYPE_CODES.addElement(LoggingTypes.NONE, "NONE", "pointEdit.logging.type.never");
        LOGGING_TYPE_CODES.addElement(LoggingTypes.INTERVAL, "INTERVAL", "pointEdit.logging.type.interval");
        LOGGING_TYPE_CODES.addElement(LoggingTypes.ON_TS_CHANGE, "ON_TS_CHANGE", "pointEdit.logging.type.tsChange");
    }

    public interface PurgeTypes {
        int DAYS = Common.TimePeriods.DAYS;
        int WEEKS = Common.TimePeriods.WEEKS;
        int MONTHS = Common.TimePeriods.MONTHS;
        int YEARS = Common.TimePeriods.YEARS;
    }

    public interface IntervalLoggingTypes {
        int INSTANT = 1;
        int MAXIMUM = 2;
        int MINIMUM = 3;
        int AVERAGE = 4;
    }

    public static final ExportCodes INTERVAL_LOGGING_TYPE_CODES = new ExportCodes();
    static {
        INTERVAL_LOGGING_TYPE_CODES.addElement(IntervalLoggingTypes.INSTANT, "INSTANT",
                "pointEdit.logging.valueType.instant");
        INTERVAL_LOGGING_TYPE_CODES.addElement(IntervalLoggingTypes.MAXIMUM, "MAXIMUM",
                "pointEdit.logging.valueType.maximum");
        INTERVAL_LOGGING_TYPE_CODES.addElement(IntervalLoggingTypes.MINIMUM, "MINIMUM",
                "pointEdit.logging.valueType.minimum");
        INTERVAL_LOGGING_TYPE_CODES.addElement(IntervalLoggingTypes.AVERAGE, "AVERAGE",
                "pointEdit.logging.valueType.average");
    }

    public interface PlotTypes {
        int STEP = 1;
        int LINE = 2;
        int SPLINE = 3;
    }

    private static final ExportCodes PLOT_TYPE_CODES = new ExportCodes();
    static {
        PLOT_TYPE_CODES.addElement(PlotTypes.STEP, "STEP", "pointEdit.plotType.step");
        PLOT_TYPE_CODES.addElement(PlotTypes.LINE, "LINE", "pointEdit.plotType.line");
        PLOT_TYPE_CODES.addElement(PlotTypes.SPLINE, "SPLINE", "pointEdit.plotType.spline");
    }

    public TranslatableMessage getDataTypeMessage() {
        return pointLocator.getDataTypeMessage();
    }

    public TranslatableMessage getConfigurationDescription() {
        return pointLocator.getConfigurationDescription();
    }

    public boolean isNew() {
        return id == Common.NEW_ID;
    }

    //
    //
    // Properties
    //
    private int id = Common.NEW_ID;
    @JsonProperty(read = false)
    private String xid;
    @JsonProperty
    private String name;
    private int dataSourceId;
    @JsonProperty
    private String deviceName;
    @JsonProperty
    private boolean enabled;
    private int pointFolderId;
    private int loggingType = LoggingTypes.ON_CHANGE;
    private int intervalLoggingPeriodType = Common.TimePeriods.MINUTES;
    @JsonProperty
    private int intervalLoggingPeriod = 15;
    private int intervalLoggingType = IntervalLoggingTypes.INSTANT;
    @JsonProperty
    private double tolerance = 0;
    @JsonProperty
    private boolean purgeOverride = false;
    private int purgeType = Common.TimePeriods.YEARS;
    @JsonProperty
    private int purgePeriod = 1;
    @JsonProperty
    private TextRenderer textRenderer;
    @JsonProperty
    private ChartRenderer chartRenderer;
    private List<PointEventDetectorVO> eventDetectors;
    private List<UserComment> comments;
    @JsonProperty
    private int defaultCacheSize = 1;
    @JsonProperty
    private boolean discardExtremeValues = false;
    @JsonProperty
    private double discardLowLimit = -Double.MAX_VALUE;
    @JsonProperty
    private double discardHighLimit = Double.MAX_VALUE;
    /**
     * @deprecated
     * Use unit instead
     */
    @Deprecated
    private int engineeringUnits = com.serotonin.m2m2.util.EngineeringUnits.noUnits;
    @JsonProperty
    private String chartColour;

    // replaces engineeringUnits
    Unit<?> unit = defaultUnit();
    // replaces integralEngUnits
    Unit<?> integralUnit = defaultIntegralUnit();
    // unit used for rendering if the renderer supports it
    Unit<?> renderedUnit = defaultUnit();
    
    boolean useIntegralUnit = false;
    boolean useRenderedUnit = false;

    private int plotType = PlotTypes.STEP;

    private PointLocatorVO pointLocator;

    //
    //
    // Convenience data from data source
    //
    private String dataSourceTypeName;
    private String dataSourceName;

    //
    //
    // Required for importing
    //
    @JsonProperty
    private String dataSourceXid;

    //
    //
    // Runtime data
    //
    /*
     * This is used by the watch list and graphic views to cache the last known value for a point to determine if the
     * browser side needs to be refreshed. Initially set to this value so that point views will update (since null
     * values in this case do in fact equal each other).
     */
    private PointValueTime lastValue = new PointValueTime((DataValue) null, -1);

    public void resetLastValue() {
        lastValue = new PointValueTime((DataValue) null, -1);
    }

    public PointValueTime lastValue() {
        return lastValue;
    }

    public void updateLastValue(PointValueTime pvt) {
        lastValue = pvt;
    }

    @Override
    public String getExtendedName() {
        return getExtendedName(this);
    }

    public static String getExtendedName(IDataPoint dp) {
        return dp.getDeviceName() + " - " + dp.getName();
    }

    public void defaultTextRenderer() {
        if (pointLocator == null)
            setTextRenderer(new PlainRenderer("", false));
        else {
            switch (pointLocator.getDataTypeId()) {
            case DataTypes.IMAGE:
                setTextRenderer(new NoneRenderer());
                break;
            case DataTypes.NUMERIC:
                setTextRenderer(new PlainRenderer("", true));
                break;
            default:
                setTextRenderer(new PlainRenderer("", false));
            }
        }
    }

    /*
     * This value is used by the watchlists. It is set when the watchlist is loaded to determine if the user is allowed
     * to set the point or not based upon various conditions.
     */
    private boolean settable;

    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    @Override
    public String getTypeKey() {
        return "event.audit.dataPoint";
    }

    @Override
    public void addProperties(List<TranslatableMessage> list) {
        AuditEventType.addPropertyMessage(list, "common.xid", xid);
        AuditEventType.addPropertyMessage(list, "dsEdit.points.name", name);
        AuditEventType.addPropertyMessage(list, "common.enabled", enabled);
        AuditEventType.addExportCodeMessage(list, "pointEdit.logging.type", LOGGING_TYPE_CODES, loggingType);
        AuditEventType.addPeriodMessage(list, "pointEdit.logging.period", intervalLoggingPeriodType,
                intervalLoggingPeriod);
        AuditEventType.addExportCodeMessage(list, "pointEdit.logging.valueType", INTERVAL_LOGGING_TYPE_CODES,
                intervalLoggingType);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.tolerance", tolerance);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.purgeOverride", purgeOverride);
        AuditEventType.addPeriodMessage(list, "pointEdit.logging.purge", purgeType, purgePeriod);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.defaultCache", defaultCacheSize);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.discard", discardExtremeValues);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.discardLow", discardLowLimit);
        AuditEventType.addPropertyMessage(list, "pointEdit.props.engineeringUnits", engineeringUnits);
        AuditEventType.addPropertyMessage(list, "pointEdit.props.chartColour", chartColour);
        AuditEventType.addExportCodeMessage(list, "pointEdit.plotType", PLOT_TYPE_CODES, plotType);

        pointLocator.addProperties(list);
    }

    @Override
    public void addPropertyChanges(List<TranslatableMessage> list, DataPointVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.points.name", from.name, name);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.enabled", from.enabled, enabled);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "pointEdit.logging.type", LOGGING_TYPE_CODES,
                from.loggingType, loggingType);
        AuditEventType.maybeAddPeriodChangeMessage(list, "pointEdit.logging.period", from.intervalLoggingPeriodType,
                from.intervalLoggingPeriod, intervalLoggingPeriodType, intervalLoggingPeriod);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "pointEdit.logging.valueType",
                INTERVAL_LOGGING_TYPE_CODES, from.intervalLoggingType, intervalLoggingType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.tolerance", from.tolerance, tolerance);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.purgeOverride", from.purgeOverride,
                purgeOverride);
        AuditEventType.maybeAddPeriodChangeMessage(list, "pointEdit.logging.purge", from.purgeType, from.purgePeriod,
                purgeType, purgePeriod);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.defaultCache", from.defaultCacheSize,
                defaultCacheSize);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.discard", from.discardExtremeValues,
                discardExtremeValues);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.discardLow", from.discardLowLimit,
                discardLowLimit);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.discardHigh", from.discardHighLimit,
                discardHighLimit);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.props.engineeringUnits", from.engineeringUnits,
                engineeringUnits);
        AuditEventType
                .maybeAddPropertyChangeMessage(list, "pointEdit.props.chartColour", from.chartColour, chartColour);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "pointEdit.plotType", PLOT_TYPE_CODES, from.plotType,
                plotType);

        pointLocator.addPropertyChanges(list, from.pointLocator);
    }

    @Override
    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getPointFolderId() {
        return pointFolderId;
    }

    public void setPointFolderId(int pointFolderId) {
        this.pointFolderId = pointFolderId;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public <T extends PointLocatorVO> T getPointLocator() {
        return (T) pointLocator;
    }

    public void setPointLocator(PointLocatorVO pointLocator) {
        // if data type changed from non numeric to numeric then use unit as suffix
        if (pointLocator.getDataTypeId() == DataTypes.NUMERIC &&
                (this.pointLocator == null || this.pointLocator.getDataTypeId() != DataTypes.NUMERIC) &&
                textRenderer instanceof ConvertingRenderer) {
            ConvertingRenderer cr = (ConvertingRenderer) textRenderer;
            cr.setUseUnitAsSuffix(true);
        }
        this.pointLocator = pointLocator;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        if (deviceName == null)
            deviceName = dataSourceName;
    }

    public String getDataSourceXid() {
        return dataSourceXid;
    }

    public void setDataSourceXid(String dataSourceXid) {
        this.dataSourceXid = dataSourceXid;
    }

    public String getDataSourceTypeName() {
        return dataSourceTypeName;
    }

    public void setDataSourceTypeName(String dataSourceTypeName) {
        this.dataSourceTypeName = dataSourceTypeName;
    }

    public int getLoggingType() {
        return loggingType;
    }

    public void setLoggingType(int loggingType) {
        this.loggingType = loggingType;
    }

    public boolean isPurgeOverride() {
        return purgeOverride;
    }

    public void setPurgeOverride(boolean purgeOverride) {
        this.purgeOverride = purgeOverride;
    }

    public int getPurgePeriod() {
        return purgePeriod;
    }

    public void setPurgePeriod(int purgePeriod) {
        this.purgePeriod = purgePeriod;
    }

    public int getPurgeType() {
        return purgeType;
    }

    public void setPurgeType(int purgeType) {
        this.purgeType = purgeType;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
        setUnitsOnTextRenderer();
    }
    
    public TextRenderer createIntegralRenderer() {
        AnalogRenderer renderer = new AnalogRenderer();
        
        renderer.setUnit(defaultIntegralUnit());
        if (useIntegralUnit)
            renderer.setRenderedUnit(integralUnit);
        else
            renderer.setRenderedUnit(defaultIntegralUnit());
        
        renderer.setUseUnitAsSuffix(true);
        renderer.setFormat("0.0");
        return renderer;
    }

    public ChartRenderer getChartRenderer() {
        return chartRenderer;
    }

    public void setChartRenderer(ChartRenderer chartRenderer) {
        this.chartRenderer = chartRenderer;
    }

    public List<PointEventDetectorVO> getEventDetectors() {
        return eventDetectors;
    }

    public void setEventDetectors(List<PointEventDetectorVO> eventDetectors) {
        this.eventDetectors = eventDetectors;
    }

    public List<UserComment> getComments() {
        return comments;
    }

    public void setComments(List<UserComment> comments) {
        this.comments = comments;
    }

    public int getDefaultCacheSize() {
        return defaultCacheSize;
    }

    public void setDefaultCacheSize(int defaultCacheSize) {
        this.defaultCacheSize = defaultCacheSize;
    }

    public int getIntervalLoggingPeriodType() {
        return intervalLoggingPeriodType;
    }

    public void setIntervalLoggingPeriodType(int intervalLoggingPeriodType) {
        this.intervalLoggingPeriodType = intervalLoggingPeriodType;
    }

    public int getIntervalLoggingPeriod() {
        return intervalLoggingPeriod;
    }

    public void setIntervalLoggingPeriod(int intervalLoggingPeriod) {
        this.intervalLoggingPeriod = intervalLoggingPeriod;
    }

    public int getIntervalLoggingType() {
        return intervalLoggingType;
    }

    public void setIntervalLoggingType(int intervalLoggingType) {
        this.intervalLoggingType = intervalLoggingType;
    }

    public boolean isDiscardExtremeValues() {
        return discardExtremeValues;
    }

    public void setDiscardExtremeValues(boolean discardExtremeValues) {
        this.discardExtremeValues = discardExtremeValues;
    }

    public double getDiscardLowLimit() {
        return discardLowLimit;
    }

    public void setDiscardLowLimit(double discardLowLimit) {
        this.discardLowLimit = discardLowLimit;
    }

    public double getDiscardHighLimit() {
        return discardHighLimit;
    }

    public void setDiscardHighLimit(double discardHighLimit) {
        this.discardHighLimit = discardHighLimit;
    }

    /**
     * @deprecated
     * Use getUnit() instead
     * @return
     */
    @Deprecated
    public int getEngineeringUnits() {
        return engineeringUnits;
    }

    /**
     * @deprecated
     * Use setUnit() instead
     * @param engineeringUnits
     */
    @Deprecated
    public void setEngineeringUnits(int engineeringUnits) {
        this.engineeringUnits = engineeringUnits;
    }
    
    public Unit<?> getIntegralUnit() {
        return integralUnit;
    }

    public void setIntegralUnit(Unit<?> integralUnit) {
        this.integralUnit = integralUnit;
    }
    
    public Unit<?> getUnit() {
        return unit;
    }

    public void setUnit(Unit<?> unit) {
        this.unit = unit;
    }
    
    public Unit<?> getRenderedUnit() {
        return renderedUnit;
    }

    public void setRenderedUnit(Unit<?> renderedUnit) {
        this.renderedUnit = renderedUnit;
    }
    
    public boolean isUseIntegralUnit() {
        return useIntegralUnit;
    }

    public void setUseIntegralUnit(boolean useIntegralUnit) {
        this.useIntegralUnit = useIntegralUnit;
    }

    public boolean isUseRenderedUnit() {
        return useRenderedUnit;
    }

    public void setUseRenderedUnit(boolean useRenderedUnit) {
        this.useRenderedUnit = useRenderedUnit;
    }

    public String getChartColour() {
        return chartColour;
    }

    public void setChartColour(String chartColour) {
        this.chartColour = chartColour;
    }

    public int getPlotType() {
        return plotType;
    }

    public void setPlotType(int plotType) {
        this.plotType = plotType;
    }

    public DataPointVO copy() {
        try {
            return (DataPointVO) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    public void validate(ProcessResult response) {
        if (StringUtils.isBlank(xid))
            response.addContextualMessage("xid", "validate.required");
        else if (StringValidation.isLengthGreaterThan(xid, 50))
            response.addMessage("xid", new TranslatableMessage("validate.notLongerThan", 50));
        else if (!new DataPointDao().isXidUnique(xid, id))
            response.addContextualMessage("xid", "validate.xidUsed");

        if (StringUtils.isBlank(name))
            response.addContextualMessage("name", "validate.required");

        if (!LOGGING_TYPE_CODES.isValidId(loggingType))
            response.addContextualMessage("loggingType", "validate.invalidValue");
        if (loggingType == DataPointVO.LoggingTypes.ON_CHANGE && pointLocator.getDataTypeId() == DataTypes.NUMERIC) {
            if (tolerance < 0)
                response.addContextualMessage("tolerance", "validate.cannotBeNegative");
        }

        if (!Common.TIME_PERIOD_CODES.isValidId(intervalLoggingPeriodType))
            response.addContextualMessage("intervalLoggingPeriodType", "validate.invalidValue");
        if (intervalLoggingPeriod <= 0)
            response.addContextualMessage("intervalLoggingPeriod", "validate.greaterThanZero");
        if (!INTERVAL_LOGGING_TYPE_CODES.isValidId(intervalLoggingType))
            response.addContextualMessage("intervalLoggingType", "validate.invalidValue");

        if (purgeOverride) {
            if (!Common.TIME_PERIOD_CODES.isValidId(purgeType))
                response.addContextualMessage("purgeType", "validate.invalidValue");
            if (purgePeriod <= 0)
                response.addContextualMessage("purgePeriod", "validate.greaterThanZero");
        }

        if (textRenderer == null)
            response.addContextualMessage("textRenderer", "validate.required");

        if (defaultCacheSize < 0)
            response.addContextualMessage("defaultCacheSize", "validate.cannotBeNegative");

        if (discardExtremeValues && discardHighLimit <= discardLowLimit)
            response.addContextualMessage("discardHighLimit", "validate.greaterThanDiscardLow");

        if (!StringUtils.isBlank(chartColour)) {
            try {
                ColorUtils.toColor(chartColour);
            }
            catch (InvalidArgumentException e) {
                response.addContextualMessage("chartColour", "validate.invalidValue");
            }
        }

        pointLocator.validate(response, this);

        // Check text renderer type
        if (textRenderer != null && !textRenderer.getDef().supports(pointLocator.getDataTypeId()))
            response.addGenericMessage("validate.text.incompatible");

        // Check chart renderer type
        if (chartRenderer != null && !chartRenderer.getDef().supports(pointLocator.getDataTypeId()))
            response.addGenericMessage("validate.chart.incompatible");

        // Check the plot type
        if (!PLOT_TYPE_CODES.isValidId(plotType))
            response.addContextualMessage("plotType", "validate.invalidValue");
        if (plotType != PlotTypes.STEP && pointLocator.getDataTypeId() != DataTypes.NUMERIC)
            response.addContextualMessage("plotType", "validate.invalidValue");
        
        if (!validateIntegralUnit()) {
            response.addContextualMessage("integralUnit", "validate.unitNotCompatible");
        }
        
        if (!validateRenderedUnit()) {
            response.addContextualMessage("renderedUnit", "validate.unitNotCompatible");
        }
    }
    
    public boolean validateIntegralUnit() {
        if (!useIntegralUnit) {
            integralUnit = defaultIntegralUnit();
            return true;
        }
        
        // integral unit should have same dimensions as the default integrated unit
        if (integralUnit == null)
            return false;
        return integralUnit.isCompatible(defaultIntegralUnit());
    }
    
    public boolean validateRenderedUnit() {
        if (!useRenderedUnit) {
            renderedUnit = unit;
            return true;
        }
        
        // integral unit should have same dimensions as the default integrated unit
        if (renderedUnit == null)
            return false;
        return renderedUnit.isCompatible(unit);
    }
    
    // default unit is ONE ie no units
    private Unit<?> defaultUnit() {
        return Unit.ONE;
    }
    
    // default integrated unit is the base unit times seconds
    // as we are integrating over time
    private Unit<?> defaultIntegralUnit() {
        return unit.times(SI.SECOND);
    }
    
    public UnitConverter getIntegralConverter() {
        return defaultIntegralUnit().getConverterTo(integralUnit);
    }
    
    @Override
    public String toString() {
        return "DataPointVO [id=" + id + ", xid=" + xid + ", name=" + name + ", dataSourceId=" + dataSourceId
                + ", deviceName=" + deviceName + ", enabled=" + enabled + ", pointFolderId=" + pointFolderId
                + ", loggingType=" + loggingType + ", intervalLoggingPeriodType=" + intervalLoggingPeriodType
                + ", intervalLoggingPeriod=" + intervalLoggingPeriod + ", intervalLoggingType=" + intervalLoggingType
                + ", tolerance=" + tolerance + ", purgeOverride=" + purgeOverride + ", purgeType=" + purgeType
                + ", purgePeriod=" + purgePeriod + ", textRenderer=" + textRenderer + ", chartRenderer="
                + chartRenderer + ", eventDetectors=" + eventDetectors + ", comments=" + comments
                + ", defaultCacheSize=" + defaultCacheSize + ", discardExtremeValues=" + discardExtremeValues
                + ", discardLowLimit=" + discardLowLimit + ", discardHighLimit=" + discardHighLimit
                + ", unit=" + unit + ", integralUnit=" + integralUnit + ", renderedUnit=" + renderedUnit
                + ", useIntegralUnit=" + useIntegralUnit + ", useRenderedUnit=" + useRenderedUnit
                + ", chartColour=" + chartColour + ", plotType=" + plotType
                + ", pointLocator=" + pointLocator + ", dataSourceTypeName=" + dataSourceTypeName + ", dataSourceName="
                + dataSourceName + ", dataSourceXid=" + dataSourceXid + ", lastValue=" + lastValue + ", settable="
                + settable + "]";
    }

    //
    //
    // Serialization
    //
    private static final int version = 7;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeObject(textRenderer);
        out.writeObject(chartRenderer);
        out.writeObject(pointLocator);
        out.writeDouble(discardLowLimit);
        out.writeDouble(discardHighLimit);
        SerializationHelper.writeSafeUTF(out, chartColour);
        out.writeInt(plotType);
        out.writeObject(unit);
        out.writeObject(integralUnit);
        out.writeObject(renderedUnit);
        out.writeBoolean(useIntegralUnit);
        out.writeBoolean(useRenderedUnit);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeOverride = true;
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            unit = UnitUtil.convertToUnit(in.readInt());
            chartColour = null;
            plotType = PlotTypes.STEP;
            integralUnit = defaultIntegralUnit();
        }
        else if (ver == 2) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeOverride = true;
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            unit = UnitUtil.convertToUnit(in.readInt());
            chartColour = SerializationHelper.readSafeUTF(in);
            plotType = PlotTypes.STEP;
            integralUnit = defaultIntegralUnit();
        }
        else if (ver == 3) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = SerializationHelper.readSafeUTF(in);
            enabled = in.readBoolean();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeOverride = true;
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            unit = UnitUtil.convertToUnit(in.readInt());
            chartColour = SerializationHelper.readSafeUTF(in);
            plotType = PlotTypes.STEP;
            integralUnit = defaultIntegralUnit();
        }
        else if (ver == 4) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = SerializationHelper.readSafeUTF(in);
            enabled = in.readBoolean();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeOverride = true;
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            unit = UnitUtil.convertToUnit(in.readInt());
            chartColour = SerializationHelper.readSafeUTF(in);
            plotType = in.readInt();
            integralUnit = defaultIntegralUnit();
        }
        else if (ver == 5) {
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            chartColour = SerializationHelper.readSafeUTF(in);
            plotType = in.readInt();
            unit = null; // calcUnit() is called from Dao
            integralUnit = null; // calcIntegralUnit() is called from Dao
        }
        else if (ver == 6) {
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            chartColour = SerializationHelper.readSafeUTF(in);
            plotType = in.readInt();
            unit = null; // calcUnit() is called from Dao
            integralUnit = UnitUtil.convertToUnit(in.readInt()); // legacy integralEngUnits
        }
        else if (ver == 7) {
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            chartColour = SerializationHelper.readSafeUTF(in);
            plotType = in.readInt();
            unit = (Unit<?>) in.readObject();
            integralUnit = (Unit<?>) in.readObject();
            renderedUnit = (Unit<?>) in.readObject();
            useIntegralUnit = in.readBoolean();
            useRenderedUnit = in.readBoolean();
        }
        
        // Check the purge type. Weird how this could have been set to 0.
        if (purgeType == 0)
            purgeType = Common.TimePeriods.YEARS;
        // Ditto for purge period
        if (purgePeriod == 0)
            purgePeriod = 1;
    }
    
    private void setUnitsOnTextRenderer() {
        if (textRenderer instanceof ConvertingRenderer) {
            ConvertingRenderer cr = (ConvertingRenderer) textRenderer;
            cr.setUnit(unit);
            if (useRenderedUnit) {
                cr.setRenderedUnit(renderedUnit);
            }
            else {
                cr.setRenderedUnit(unit);
            }
        }
    }
    
    public void ensureUnitsCorrect() {
        if (unit == null) {
            unit = UnitUtil.convertToUnit(engineeringUnits);
        }
        
        if (integralUnit == null || !validateIntegralUnit()) {
            integralUnit = defaultIntegralUnit();
        }
        
        setUnitsOnTextRenderer();
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("loggingType", LOGGING_TYPE_CODES.getCode(loggingType));
        writer.writeEntry("intervalLoggingPeriodType", Common.TIME_PERIOD_CODES.getCode(intervalLoggingPeriodType));
        writer.writeEntry("intervalLoggingType", INTERVAL_LOGGING_TYPE_CODES.getCode(intervalLoggingType));
        writer.writeEntry("purgeType", Common.TIME_PERIOD_CODES.getCode(purgeType));
        writer.writeEntry("pointLocator", pointLocator);
        writer.writeEntry("eventDetectors", eventDetectors);
        writer.writeEntry("plotType", PLOT_TYPE_CODES.getCode(plotType));
        writer.writeEntry("unit", UnitUtil.formatUcum(unit));
        if (useIntegralUnit)
            writer.writeEntry("integralUnit", UnitUtil.formatUcum(integralUnit));
        if (useRenderedUnit)
            writer.writeEntry("renderedUnit", UnitUtil.formatUcum(renderedUnit));
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String text = jsonObject.getString("loggingType");
        if (text != null) {
            loggingType = LOGGING_TYPE_CODES.getId(text);
            if (loggingType == -1)
                throw new TranslatableJsonException("emport.error.invalid", "loggingType", text,
                        LOGGING_TYPE_CODES.getCodeList());
        }

        text = jsonObject.getString("intervalLoggingPeriodType");
        if (text != null) {
            intervalLoggingPeriodType = Common.TIME_PERIOD_CODES.getId(text);
            if (intervalLoggingPeriodType == -1)
                throw new TranslatableJsonException("emport.error.invalid", "intervalLoggingPeriodType", text,
                        Common.TIME_PERIOD_CODES.getCodeList());
        }

        text = jsonObject.getString("intervalLoggingType");
        if (text != null) {
            intervalLoggingType = INTERVAL_LOGGING_TYPE_CODES.getId(text);
            if (intervalLoggingType == -1)
                throw new TranslatableJsonException("emport.error.invalid", "intervalLoggingType", text,
                        INTERVAL_LOGGING_TYPE_CODES.getCodeList());
        }

        text = jsonObject.getString("purgeType");
        if (text != null) {
            purgeType = Common.TIME_PERIOD_CODES.getId(text);
            if (purgeType == -1)
                throw new TranslatableJsonException("emport.error.invalid", "purgeType", text,
                        Common.TIME_PERIOD_CODES.getCodeList());
        }

        JsonObject locatorJson = jsonObject.getJsonObject("pointLocator");
        if (locatorJson != null)
            reader.readInto(pointLocator, locatorJson);

        JsonArray pedArray = jsonObject.getJsonArray("eventDetectors");
        if (pedArray != null) {
            for (JsonValue jv : pedArray) {
                JsonObject pedObject = jv.toJsonObject();

                String pedXid = pedObject.getString("xid");
                if (StringUtils.isBlank(pedXid))
                    throw new TranslatableJsonException("emport.error.ped.missingAttr", "xid");

                // Use the ped xid to lookup an existing ped.
                PointEventDetectorVO ped = null;
                for (PointEventDetectorVO existing : eventDetectors) {
                    if (StringUtils.equals(pedXid, existing.getXid())) {
                        ped = existing;
                        break;
                    }
                }

                if (ped == null) {
                    // Create a new one
                    ped = new PointEventDetectorVO();
                    ped.setId(Common.NEW_ID);
                    ped.setXid(pedXid);
                    ped.njbSetDataPoint(this);
                    eventDetectors.add(ped);
                }

                reader.readInto(ped, pedObject);
            }
        }

        text = jsonObject.getString("unit");
        if (text != null) {
            unit = parseUnitString(text, "unit");
        }
        
        text = jsonObject.getString("integralUnit");
        if (text != null) {
            useIntegralUnit = true;
            integralUnit = parseUnitString(text, "integralUnit");
        }
        
        text = jsonObject.getString("renderedUnit");
        if (text != null) {
            useRenderedUnit = true;
            renderedUnit = parseUnitString(text, "renderedUnit");
        }
        
        text = jsonObject.getString("plotType");
        if (text != null) {
            plotType = PLOT_TYPE_CODES.getId(text);
            if (plotType == -1)
                throw new TranslatableJsonException("emport.error.invalid", "plotType", text,
                        PLOT_TYPE_CODES.getCodeList());
        }
    }
    
    private Unit<?> parseUnitString(String string, String item) throws TranslatableJsonException {
        try {
            return UnitUtil.parseUcum(string);
        }
        catch (Exception e) {
            throw new TranslatableJsonException("emport.error.parseError", item);
        }
    }
}
