package parozzz.github.com.hmi.attribute.impl.address;

import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.impl.address.data.AddressDataType;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.FunctionAttributeProperty;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;

public abstract class AddressAttribute extends Attribute
{
    public static final AttributeProperty<AddressDataType> DATA_TYPE = new FunctionAttributeProperty<>("DataType", AddressDataType.NONE,
            AddressDataType::getName,
            (jsonDataMap, key) -> AddressDataType.getByName(jsonDataMap.getString(key))
    );

    private boolean deserializing;
    public AddressAttribute(String name)
    {
        super(name);

        var attributePropertyManager = super.getAttributePropertyManager();
        attributePropertyManager.addAll(DATA_TYPE);

        var dataTypeProperty = super.getAttributePropertyManager().getByAttributeProperty(DATA_TYPE);
        dataTypeProperty.addListener((observableValue, oldValue, newValue) ->
        {
            if(deserializing) //This is because it would throw a ConcurrentModificationException while deserializing.
            {
                return;
            }

            if(oldValue != newValue)
            {
                if(oldValue != null)
                {
                    oldValue.removeAttributesFrom(attributePropertyManager);
                }

                if(newValue != null)
                {
                    newValue.addAttributesTo(attributePropertyManager);
                }
            }
        });
        dataTypeProperty.setValue(AddressDataType.NONE);
    }

    @Override
    public void updateInternals()
    {

    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        //First deserialize the data type and then all the other stuff
        var dataTypeProperty = super.getProperty(DATA_TYPE);
        DATA_TYPE.deserializeFrom(dataTypeProperty, jsonDataMap);

        deserializing = true;

        //Here it will deserialize the same value again but it shouldn't be
        //much of a hassle since is just a one time thing
        super.deserialize(jsonDataMap);

        deserializing = false;
    }

    @Override
    public abstract AddressAttribute cloneEmpty();
}
