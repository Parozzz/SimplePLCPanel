package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.serialize.parsers.SuppliedJSONObjectParser;

public class ParsableAttributeProperty<T> extends AttributeProperty<T>
{
    private final SuppliedJSONObjectParser<T> parser;
    public ParsableAttributeProperty(String key, T defaultValue,
            SuppliedJSONObjectParser<T> parser)
    {
        super(key, defaultValue);

        this.parser = parser;
    }
/*
    @Override
    public void serializeInto(Property<T> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        if(value != null)
        {
            jsonDataMap.set(super.key, parser.serialize(value));
        }
    }

    @Override
    public void deserializeFrom(Property<T> property, JSONDataMap jsonDataMap)
    {
        var parsedObject = jsonDataMap.getParsable(parser, super.key);
        super.setValue(property, parsedObject);
    }
*/
    @Override
    public Data<T> createData(Attribute attribute)
    {
        return new ParsableData();
    }

    public class ParsableData extends AttributeProperty.Data<T>
    {
        protected ParsableData()
        {
            super(ParsableAttributeProperty.this);
        }

        @Override
        public void serializeInto(JSONDataMap jsonDataMap)
        {
            var value = property.getValue();
            if(value != null)
            {
                jsonDataMap.set(key, parser.serialize(value));
            }
        }

        @Override
        public void deserializeFrom(JSONDataMap jsonDataMap)
        {
            var parsedObject = jsonDataMap.getParsable(parser, key);
            super.setValue(parsedObject);
        }
    }
}
