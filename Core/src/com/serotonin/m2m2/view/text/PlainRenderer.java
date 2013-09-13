/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.view.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.measure.unit.Unit;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.util.SerializationHelper;

public class PlainRenderer extends ConvertingRenderer {
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

    private String suffix;
    
    public PlainRenderer() {
        super();
    }
    
    /**
     * @param suffix
     */
    public PlainRenderer(String suffix) {
        super();
        this.suffix = suffix;
    }

    /**
     * @param suffix
     * @param useUnitAsSuffix
     */
    public PlainRenderer(String suffix, boolean useUnitAsSuffix) {
        super();
        this.suffix = suffix;
        this.useUnitAsSuffix = useUnitAsSuffix;
    }
    
    @Override
    protected void setDefaults() {
        super.setDefaults();
        suffix = "";
    }

    @Override
    public String getMetaText() {
        if (useUnitAsSuffix)
            return UnitUtil.formatLocal(renderedUnit);
        return suffix;
    }

    @Override
    protected String getTextImpl(DataValue value, int hint) {
        String raw;
        String suffix = this.suffix;
        
        if (value instanceof BinaryValue) {
            if (value.getBooleanValue())
                raw = "1";
            raw = "0";
        }
        else if (value instanceof NumericValue) {
            double dblValue = value.getDoubleValue();
            if ((hint & HINT_NO_CONVERT) == 0)
                dblValue = unit.getConverterTo(renderedUnit).convert(dblValue);
            raw = Double.toString(dblValue);
            if (useUnitAsSuffix)
                suffix = " " + UnitUtil.formatLocal(renderedUnit);
        }
        else {
            raw = value.toString();
        }

        if ((hint & HINT_RAW) != 0 || suffix == null)
            return raw;
        
        return raw + suffix;
    }

    public String getSuffix() {
        return suffix;
    }

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
    private static final int version = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, suffix);
        out.writeBoolean(useUnitAsSuffix);
        out.writeObject(unit);
        out.writeObject(renderedUnit);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();
        
        setDefaults();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            suffix = SerializationHelper.readSafeUTF(in);
        }
        else if (ver == 2) {
            suffix = SerializationHelper.readSafeUTF(in);
            useUnitAsSuffix = in.readBoolean();
        }
        else if (ver == 3) {
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
