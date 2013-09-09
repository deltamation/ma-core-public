/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.view.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;

import javax.measure.unit.Unit;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.util.SerializationHelper;

public class AnalogRenderer extends BaseTextRenderer implements SuffixRenderer, PointDependentRenderer, ConvertingRenderer {
    private static ImplDefinition definition = new ImplDefinition("textRendererAnalog", "ANALOG",
            "textRenderer.analog", new int[] { DataTypes.NUMERIC });

    public static ImplDefinition getDefinition() {
        return definition;
    }

    @Override
    public String getTypeName() {
        return definition.getName();
    }

    @Override
    public ImplDefinition getDef() {
        return definition;
    }

    @JsonProperty
    protected String format;
    @JsonProperty
    protected String suffix;
    
    // not persisted
    DataPointVO point;

    public AnalogRenderer() {
        // no op
    }
    
    /**
     * @param format
     * @param suffix
     * @param point
     */
    public AnalogRenderer(String format, String suffix) {
        this.format = format;
        this.suffix = suffix;
    }

    /**
     * @param format
     * @param suffix
     * @param point
     * @param useUnitAsSuffix
     */
    public AnalogRenderer(String format, String suffix, boolean useUnitAsSuffix) {
        this.format = format;
        this.suffix = suffix;
        this.useUnitAsSuffix = useUnitAsSuffix;
    }
    
    @Override
    public String getMetaText() {
        return suffix;
    }

    @Override
    protected String getTextImpl(DataValue value, int hint) {
        if (!(value instanceof NumericValue))
            return null;
        return getText(value.getDoubleValue(), hint);
    }

    @Override
    public String getText(double value, int hint) {
        Unit<?> unit = point.getUnit();
        
        if (point.isUseRenderedUnit()) {
            Unit<?> renderedUnit = point.getRenderedUnit();
            value = unit.getConverterTo(renderedUnit).convert(value);
            unit = renderedUnit; // so correct suffix is applied
        }
        
//        if (useUnitAsSuffix) {
//            if (unit.equals(NonSI.DEGREE_ANGLE) || unit.equals(NonSI.MINUTE_ANGLE) || unit.equals(NonSI.SECOND_ANGLE))
//                suffix = unit.toString();
//            else
//                suffix = " " + UnitUtil.formatLocal(unit);
//        }
        if (useUnitAsSuffix)
            suffix = " " + UnitUtil.formatLocal(unit);
        
        String raw = new DecimalFormat(format).format(value);
        if (hint == HINT_RAW || suffix == null)
            return raw;
        
        return raw + suffix;
    }

    @Override
    protected String getColourImpl(DataValue value) {
        return null;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String getChangeSnippetFilename() {
        return "changeContentText.jsp";
    }

    @Override
    public String getSetPointSnippetFilename() {
        return "setPointContentText.jsp";
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, format);
        SerializationHelper.writeSafeUTF(out, suffix);
        out.writeBoolean(useUnitAsSuffix);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            format = SerializationHelper.readSafeUTF(in);
            suffix = SerializationHelper.readSafeUTF(in);
        }
        else if (ver == 2) {
            format = SerializationHelper.readSafeUTF(in);
            suffix = SerializationHelper.readSafeUTF(in);
            useUnitAsSuffix = in.readBoolean();
        }
    }
    
    @JsonProperty
    protected boolean useUnitAsSuffix = true;

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.view.text.SuffixRenderer#isUseUnit()
     */
    @Override
    public boolean isUseUnitAsSuffix() {
        return useUnitAsSuffix;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.view.text.SuffixRenderer#setUseUnit()
     */
    @Override
    public void setUseUnitAsSuffix(boolean useUnit) {
        this.useUnitAsSuffix = useUnit;
    }
    
    @Override
    public DataPointVO getPoint() {
        return point;
    }
    
    @Override
    public void setPoint(DataPointVO point) {
        this.point = point;
    }
}
