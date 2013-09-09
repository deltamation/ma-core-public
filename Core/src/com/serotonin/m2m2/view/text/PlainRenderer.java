/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.view.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.measure.unit.Unit;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.util.SerializationHelper;

public class PlainRenderer extends BaseTextRenderer implements SuffixRenderer, PointDependentRenderer, ConvertingRenderer {
    private static ImplDefinition definition = new ImplDefinition("textRendererPlain", "PLAIN", "textRenderer.plain",
            new int[] { DataTypes.BINARY, DataTypes.ALPHANUMERIC, DataTypes.MULTISTATE, DataTypes.NUMERIC });

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
    private String suffix;
    
    // not persisted
    DataPointVO point;

    public PlainRenderer() {
        // no op
    }
    
    /**
     * @param suffix
     * @param point
     */
    public PlainRenderer(String suffix) {
        this.suffix = suffix;
    }

    /**
     * @param suffix
     * @param point
     * @param useUnitAsSuffix
     */
    public PlainRenderer(String suffix, boolean useUnitAsSuffix) {
        this.suffix = suffix;
        this.useUnitAsSuffix = useUnitAsSuffix;
    }

    @Override
    public String getMetaText() {
        return suffix;
    }

    @Override
    protected String getTextImpl(DataValue value, int hint) {
        Unit<?> unit = point.getUnit();
        
        String raw;
        if (value instanceof BinaryValue) {
            if (value.getBooleanValue())
                raw = "1";
            raw = "0";
        }
        else if (value instanceof NumericValue) {
            double dblValue = value.getDoubleValue();
            if (point.isUseRenderedUnit()) {
                Unit<?> renderedUnit = point.getRenderedUnit();
                dblValue = unit.getConverterTo(renderedUnit).convert(dblValue);
                unit = renderedUnit; // so correct suffix is applied
            }
            raw = Double.toString(dblValue);
        }
        else {
            raw = value.toString();
        }

        if (useUnitAsSuffix)
            suffix = " " + UnitUtil.formatLocal(unit);
        
        if (hint == HINT_RAW || suffix == null)
            return raw;
        
        return raw + suffix;
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
    protected String getColourImpl(DataValue value) {
        return null;
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
        SerializationHelper.writeSafeUTF(out, suffix);
        out.writeBoolean(useUnitAsSuffix);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            suffix = SerializationHelper.readSafeUTF(in);
        }
        else if (ver == 2) {
            suffix = SerializationHelper.readSafeUTF(in);
            useUnitAsSuffix = in.readBoolean();
        }
    }
    
    @JsonProperty
    boolean useUnitAsSuffix = true;

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

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.view.text.PointDependentRenderer#getPoint()
     */
    @Override
    public DataPointVO getPoint() {
        return point;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.view.text.PointDependentRenderer#setPoint(com.serotonin.m2m2.vo.DataPointVO)
     */
    @Override
    public void setPoint(DataPointVO point) {
        this.point = point;
    }
}
