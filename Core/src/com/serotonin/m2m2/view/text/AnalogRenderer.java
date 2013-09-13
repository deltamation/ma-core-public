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

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.util.SerializationHelper;

public class AnalogRenderer extends ConvertingRenderer {
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
    protected String suffix;
    
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
        if (doConversion)
            value = unit.getConverterTo(renderedUnit).convert(value);
        
//        if (useUnitAsSuffix) {
//            if (unit.equals(NonSI.DEGREE_ANGLE) || unit.equals(NonSI.MINUTE_ANGLE) || unit.equals(NonSI.SECOND_ANGLE))
//                suffix = unit.toString();
//            else
//                suffix = " " + UnitUtil.formatLocal(unit);
//        }
        
        if (useUnitAsSuffix)
            suffix = " " + UnitUtil.formatLocal(renderedUnit);
        
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

    public String getSuffix() {
        return suffix;
    }

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
    private static final int version = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, format);
        SerializationHelper.writeSafeUTF(out, suffix);
        out.writeBoolean(useUnitAsSuffix);
        out.writeObject(unit);
        out.writeObject(renderedUnit);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
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
        else if (ver == 3) {
            format = SerializationHelper.readSafeUTF(in);
            suffix = SerializationHelper.readSafeUTF(in);
            useUnitAsSuffix = in.readBoolean();
            unit = (Unit<?>) in.readObject();
            renderedUnit = (Unit<?>) in.readObject();
        }
    }
    
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        
        if (!useUnitAsSuffix)
            writer.writeEntry("suffix", suffix);
    }
    
    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        
        if (useUnitAsSuffix) {
            suffix = "";
        } else {
            String text = jsonObject.getString("suffix");
            if (text != null) {
                suffix = text;
            }
            else {
                suffix = "";
            }
        }
    }
}
