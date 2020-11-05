package parozzz.github.com.hmi;

import parozzz.github.com.hmi.serialize.JSONSerializable;
import parozzz.github.com.hmi.serialize.SerializableDataSet;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.util.Validate;

public abstract class FXObject implements JSONSerializable
{
    private final String name;
    private boolean setupDone = false;
    protected final SerializableDataSet serializableDataSet;
    private boolean disabled;

    FXController controller;

    public FXObject(String name)
    {
        this.name = name;

        this.serializableDataSet = new SerializableDataSet();
    }

    public String getFXObjectName()
    {
        return name;
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public void setup()
    {
        Validate.needFalse("Trying to execute a setup for " + this.getFXObjectName() + " twice", setupDone);
        setupDone = true;
    }

    public void setDefault()
    {

    }

    public void loop()
    {

    }

    public void setupComplete()
    {

    }

    @Override
    public JSONDataMap serialize()
    {
        var jsonDataMap = new JSONDataMap();
        serializableDataSet.serializeInto(jsonDataMap);
        return jsonDataMap;
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        serializableDataSet.deserialize(jsonDataMap);
    }
}
