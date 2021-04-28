package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives;

import javafx.beans.property.Property;
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
    public void serializeInto(Property<N> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        if (value != null)
        {
            jsonDataMap.set(super.key, value);
        }
    }

    @Override
    public void deserializeFrom(Property<N> property, JSONDataMap jsonDataMap)
    {
        var number = jsonDataMap.getNumber(super.key);
        super.setValue(property, number == null
                ? super.defaultValue
                : function.apply(number));
    }
}
