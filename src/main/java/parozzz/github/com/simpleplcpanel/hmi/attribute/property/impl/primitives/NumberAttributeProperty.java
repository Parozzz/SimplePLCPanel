package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

import java.util.function.Function;

public class NumberAttributeProperty<N extends Number> extends AttributeProperty<N>
{
    private final Function<Number, N> function;

    public NumberAttributeProperty(String key, Function<Number, N> function)
    {
        this(key, function.apply(0), function);
    }

    public NumberAttributeProperty(String key, N startValue, Function<Number, N> function)
    {
        super(key, startValue);

        this.function = function;
    }

    @Override
    public Data<N> createData(Attribute attribute)
    {
        return new NumberData();
    }

    public class NumberData extends AttributeProperty.Data<N>
    {
        protected NumberData()
        {
            super(NumberAttributeProperty.this);
        }

        @Override
        public void serializeInto(JSONDataMap jsonDataMap)
        {
            var value = property.getValue();
            if(value != null)
            {
                jsonDataMap.set(key, value);
            }
        }

        @Override
        public void deserializeFrom(JSONDataMap jsonDataMap)
        {
            var number = jsonDataMap.getNumber(key);
            super.setValue(number == null
                           ? defaultValue
                           : function.apply(number));
        }
    }
}
