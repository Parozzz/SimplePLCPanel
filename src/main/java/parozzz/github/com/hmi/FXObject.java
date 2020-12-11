package parozzz.github.com.hmi;

import parozzz.github.com.hmi.serialize.JSONSerializable;
import parozzz.github.com.hmi.serialize.SerializableDataSet;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.util.Validate;

public abstract class FXObject implements JSONSerializable
{
    private String name;
    private boolean setupDone = false;
    protected final SerializableDataSet serializableDataSet;
    private boolean disabled;

    FXController controller;

    public FXObject()
    {
        this.serializableDataSet = new SerializableDataSet();
        this.name = this.getClass().getSimpleName();
    }

    public FXObject(String name)
    {
        this();

        this.name = name;
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

    public boolean isSetupDone()
    {
        return setupDone;
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
