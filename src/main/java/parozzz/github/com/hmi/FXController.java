package parozzz.github.com.hmi;

import parozzz.github.com.hmi.serialize.JSONSerializable;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.util.Validate;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FXController extends FXObject
{
    private final Set<FXObjectWrapper> childSet;
    public FXController()
    {
        this.childSet = new HashSet<>();
    }

    public FXController(String name)
    {
        super(name);

        this.childSet = new HashSet<>();
    }

    public FXController addFXChild(FXObject child)
    {
        return addFXChild(child, true);
    }

    public FXController addFXChild(FXObject child, boolean allowSerialization)
    {
        Validate.needFalse("Cannot add itself as a child", child == this);

        var added = childSet.add(new FXObjectWrapper(child, allowSerialization));
        Validate.needTrue("Trying to add a child twice. Class: " + child.getClass().getSimpleName(), added);

        child.controller = this;

        return this;
    }

    public void removeFXChild(FXObject child)
    {
        if(childSet.removeIf(fxObjectWrapper -> fxObjectWrapper.fxObject == child))
        {
            child.controller = null;
        }
    }

    @Override
    public void setup()
    {
        super.setup();

        childSet.stream().map(FXObjectWrapper::getFxObject)
                .filter(Predicate.not(FXObject::isDisabled))
                .forEach(FXObject::setup);
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        childSet.stream().map(FXObjectWrapper::getFxObject)
                .filter(Predicate.not(FXObject::isDisabled))
                .forEach(FXObject::setDefault);
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        childSet.stream().map(FXObjectWrapper::getFxObject)
                .filter(Predicate.not(FXObject::isDisabled))
                .forEach(FXObject::setupComplete);
    }

    @Override
    public void loop()
    {
        super.loop();
        //Avoid streams here, is a method called very often better be safe than sorry!
        for (var fxObjectWrapper : childSet)
        {
            var fxObject = fxObjectWrapper.getFxObject();
            if(!fxObject.isDisabled())
            {
                fxObject.loop();
            }
        }
    }


    public JSONDataMap serialize()
    {
        var jsonDataMap = super.serialize();
        for (var fxObjectWrapper : childSet)
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

        for (var fxObjectWrapper : childSet)
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
                child.setDefault();
                continue;
            }

            try
            {
                child.deserialize(childJSONDataMap);
            } catch (Exception exception)
            {
                child.setDefault();
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
