package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.serialize.parsers.SuppliedJSONObjectParser;

public class ParsableAttributeProperty<T> extends AttributeProperty<T>
{
    private final SuppliedJSONObjectParser<T> parser;
    public ParsableAttributeProperty(String key, T defaultValue,
            SuppliedJSONObjectParser<T> parser, boolean allowNullValues)
    {
        super(key, defaultValue, allowNullValues);

        this.parser = parser;
    }

    @Override
    public Data createData(Attribute attribute)
    {
        return new ParsableData();
    }

    public class ParsableData extends AttributeProperty<T>.Data
    {
        protected ParsableData()
        {
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
