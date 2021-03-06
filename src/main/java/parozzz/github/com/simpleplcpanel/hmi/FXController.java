package parozzz.github.com.simpleplcpanel.hmi;

import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializable;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.util.Validate;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FXController extends FXObject
{
    //I DO care about the order in which objects are added, especially for deserializing!
    private final List<FXObjectWrapper> childList;
    public FXController()
    {
        this.childList = new LinkedList<>();
    }

    public FXController(String name)
    {
        super(name);

        this.childList = new LinkedList<>();
    }

    public FXController addMultipleFXChild(FXObject... children)
    {
        for(var child : children)
        {
            this.addFXChild(child);
        }
        return this;
    }

    public FXController addFXChild(FXObject child)
    {
        return addFXChild(child, true);
    }

    public FXController addFXChild(FXObject child, boolean allowSerialization)
    {
        Validate.needFalse("Cannot add itself as a child", child == this);

        var added = childList.add(new FXObjectWrapper(child, allowSerialization));
        Validate.needTrue("Trying to add a child twice. Class: " + child.getClass().getSimpleName(), added);

        child.controller = this;

        return this;
    }

    public void removeFXChild(FXObject child)
    {
        if(childList.removeIf(fxObjectWrapper -> fxObjectWrapper.fxObject == child))
        {
            child.controller = null;
        }
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        childList.stream().map(FXObjectWrapper::getFxObject)
                .filter(Predicate.not(FXObject::isDisabled))
                .forEach(FXObject::onSetup);
    }

    @Override
    public void onSetDefault()
    {
        super.onSetDefault();

        childList.stream().map(FXObjectWrapper::getFxObject)
                .filter(Predicate.not(FXObject::isDisabled))
                .forEach(FXObject::onSetDefault);
    }

    @Override
    public void onSetupComplete()
    {
        super.onSetupComplete();

        childList.stream().map(FXObjectWrapper::getFxObject)
                .filter(Predicate.not(FXObject::isDisabled))
                .forEach(FXObject::onSetupComplete);
    }

    @Override
    public void onLoop()
    {
        super.onLoop();
        //Avoid streams here, is a method called very often better be safe than sorry!
        for (var fxObjectWrapper : childList)
        {
            var fxObject = fxObjectWrapper.getFxObject();
            if(!fxObject.isDisabled())
            {
                fxObject.onLoop();
            }
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        childList.stream().map(FXObjectWrapper::getFxObject)
                .filter(Predicate.not(FXObject::isDisabled))
                .forEach(FXObject::onStop);
    }


    public JSONDataMap serialize()
    {
        var jsonDataMap = super.serialize();
        for (var fxObjectWrapper : childList)
        {
            if (!fxObjectWrapper.save)
            {
                continue;
            }

            var child = fxObjectWrapper.fxObject;
            try
            {
                var childJSONDataMap = child.serialize();
                if (childJSONDataMap == null)
                {
                    Logger.getLogger(FXController.class.getSimpleName()).log(Level.SEVERE,
                            "Child of " + this.getCompleteControllerLineup(child) +
                                    "has returned null while serializing.Class:" + child.getClass().getSimpleName());
                    continue;
                } else if (childJSONDataMap.isEmpty())
                {
                    continue;
                }

                jsonDataMap.set(child.getFXObjectName(), childJSONDataMap);
            } catch (Exception exception)
            {
                Logger.getLogger(FXController.class.getSimpleName()).log(Level.SEVERE,
                        "Child of " + this.getCompleteControllerLineup(child) +
                                " throw an exception while serializing. Class: " + child.getClass().getSimpleName(),
                        exception);
            }
        }

        return jsonDataMap;
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        super.deserialize(jsonDataMap);

        for (var fxObjectWrapper : childList)
        {
            if (!fxObjectWrapper.save)
            {
                continue;
            }

            var child = fxObjectWrapper.fxObject;

            //There is no point here logging a Sever log, since it can be easily because it was empty
            var childJSONDataMap = jsonDataMap.getMap(child.getFXObjectName());
            if (childJSONDataMap == null || childJSONDataMap.isEmpty())
            {
                child.onSetDefault();
                continue;
            }

            try
            {
                child.deserialize(childJSONDataMap);
            } catch (Exception exception)
            {
                child.onSetDefault();
                Logger.getLogger(FXController.class.getSimpleName()).log(Level.SEVERE,
                        "Child of" + this.getCompleteControllerLineup(child) +
                                " throw an exception while de-serializing. Class: " + child.getClass().getSimpleName(),
                        exception);
            }
        }
    }

    private String getCompleteControllerLineup(FXObject object)
    {
        return getControllerLineup(object, object.getFXObjectName());
    }

    private String getControllerLineup(FXObject object, String string)
    {
        if(object.controller == null)
        {
            return string;
        }
        else
        {
            var newString = object.controller.getFXObjectName() + "." + string;
            return this.getControllerLineup(object.controller, newString);
        }
    }

    private static class FXObjectWrapper implements JSONSerializable
    {
        private final FXObject fxObject;
        private final boolean save;

        public FXObjectWrapper(FXObject fxObject, boolean save)
        {
            this.fxObject = fxObject;
            this.save = save;
        }

        public FXObject getFxObject()
        {
            return fxObject;
        }

        @Override
        public JSONDataMap serialize()
        {
            return fxObject.serialize();
        }

        @Override
        public void deserialize(JSONDataMap jsonDataMap)
        {
            fxObject.deserialize(jsonDataMap);
        }
    }

}
