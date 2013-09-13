/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.view.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.spi.TypeResolver;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.TextRendererDefinition;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.ImplDefinition;

abstract public class BaseTextRenderer implements TextRenderer, JsonSerializable {
    static List<ImplDefinition> definitions;
    static Map<String, Class<? extends TextRenderer>> classMap;
    static List<TextRendererDefinition> moduleDefs;

    static void ensureDefinitions() {
        if (definitions == null) {
            List<ImplDefinition> d = new ArrayList<ImplDefinition>();
            Map<String, Class<? extends TextRenderer>> c = new HashMap<String, Class<? extends TextRenderer>>();
            
            d.add(AnalogRenderer.getDefinition());
            c.put(AnalogRenderer.getDefinition().getExportName(), AnalogRenderer.class);
            d.add(BinaryTextRenderer.getDefinition());
            c.put(BinaryTextRenderer.getDefinition().getExportName(), BinaryTextRenderer.class);
            d.add(MultistateRenderer.getDefinition());
            c.put(MultistateRenderer.getDefinition().getExportName(), MultistateRenderer.class);
            d.add(NoneRenderer.getDefinition());
            c.put(NoneRenderer.getDefinition().getExportName(), NoneRenderer.class);
            d.add(PlainRenderer.getDefinition());
            c.put(PlainRenderer.getDefinition().getExportName(), PlainRenderer.class);
            d.add(RangeRenderer.getDefinition());
            c.put(RangeRenderer.getDefinition().getExportName(), RangeRenderer.class);
            d.add(TimeRenderer.getDefinition());
            c.put(TimeRenderer.getDefinition().getExportName(), TimeRenderer.class);
            
            moduleDefs = ModuleRegistry.getDefinitions(TextRendererDefinition.class);
            for (TextRendererDefinition moduleDef : moduleDefs) {
                ImplDefinition textRenderDef = moduleDef.getTextRendererDefinition();
                d.add(textRenderDef);
                c.put(textRenderDef.getExportName(), moduleDef.getTextRendererClass());
            }
            
            definitions = d;
            classMap = c;
        }
    }

    public static List<ImplDefinition> getImplementation(int dataType) {
        ensureDefinitions();
        List<ImplDefinition> impls = new ArrayList<ImplDefinition>(definitions.size());
        for (ImplDefinition def : definitions) {
            if (def.supports(dataType))
                impls.add(def);
        }
        return impls;
    }
    
    public static List<TextRendererDefinition> getModuleDefinitions() {
        ensureDefinitions();
        return moduleDefs;
    }

    public static List<String> getExportTypes() {
        ensureDefinitions();
        List<String> result = new ArrayList<String>(classMap.size());
        result.addAll(classMap.keySet());
        return result;
    }

    @Override
    public String getText(int hint) {
        if ((hint & HINT_RAW) != 0)
            return "";
        return UNKNOWN_VALUE;
    }

    @Override
    public String getText(PointValueTime valueTime, int hint) {
        if (valueTime == null)
            return getText(hint);
        return getText(valueTime.getValue(), hint);
    }

    @Override
    public String getText(DataValue value, int hint) {
        if (value == null)
            return getText(hint);
        return getTextImpl(value, hint);
    }

    abstract protected String getTextImpl(DataValue value, int hint);

    @Override
    public String getText(double value, int hint) {
        return Double.toString(value);
    }

    @Override
    public String getText(int value, int hint) {
        return Integer.toString(value);
    }

    @Override
    public String getText(boolean value, int hint) {
        return value ? "1" : "0";
    }

    @Override
    public String getText(String value, int hint) {
        return value;
    }

    @Override
    public String getMetaText() {
        return null;
    }

    //
    // Colours
    @Override
    public String getColour() {
        return null;
    }

    @Override
    public String getColour(PointValueTime valueTime) {
        if (valueTime == null)
            return getColour();
        return getColour(valueTime.getValue());
    }

    @Override
    public String getColour(DataValue value) {
        if (value == null)
            return getColour();
        return getColourImpl(value);
    }

    abstract protected String getColourImpl(DataValue value);

    @Override
    public String getColour(double value) {
        return null;
    }

    @Override
    public String getColour(int value) {
        return null;
    }

    @Override
    public String getColour(boolean value) {
        return null;
    }

    @Override
    public String getColour(String value) {
        return null;
    }

    //
    // Parse
    @Override
    public DataValue parseText(String s, int dataType) {
        return DataValue.stringToValue(s, dataType);
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        in.readInt(); // Read the version. Value is currently not used.
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("type", getDef().getExportName());
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        // no op. The type value is used by the factory.
    }

    public static class Resolver implements TypeResolver {
        @Override
        public Type resolve(JsonValue jsonValue) throws JsonException {
            JsonObject json = jsonValue.toJsonObject();

            String type = json.getString("type").toUpperCase();
            if (type == null)
                throw new TranslatableJsonException("emport.error.text.missing", "type", getExportTypes());

            ensureDefinitions();
            
            Class<? extends TextRenderer> clazz = null;
            if (classMap.containsKey(type)) {
                clazz = classMap.get(type);
            }
            else {
                throw new TranslatableJsonException("emport.error.text.invalid", "type", type, getExportTypes());
            }

            return clazz;
        }
    }
}
