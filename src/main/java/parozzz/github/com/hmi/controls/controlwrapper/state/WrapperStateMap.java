package parozzz.github.com.hmi.controls.controlwrapper.state;

import org.json.simple.JSONObject;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.util.Validate;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WrapperStateMap extends FXObject
{
    private final ControlWrapper<?> controlWrapper;

    private final List<WrapperState> wrapperStateList;
    private final WrapperState defaultWrapperState;
    private WrapperState currentWrapperState;

    private final Set<Consumer<WrapperState>> wrapperStateValueChangedConsumerSet;

    private boolean defaultWrapperStateInitialized = false;

    public WrapperStateMap(ControlWrapper<?> controlWrapper)
    {
        super("WrapperStateMap");

        this.controlWrapper = controlWrapper;

        this.wrapperStateList = new LinkedList<>();
        this.defaultWrapperState = new WrapperDefaultState();

        this.wrapperStateValueChangedConsumerSet = new HashSet<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        var externalValue = controlWrapper.getValue().getOutsideValue();
        externalValue.addNewValueRunnable(() ->
        {
            currentWrapperState = Objects.requireNonNull(this.getStateOf(externalValue.asInteger()),
                    "Trying to get a state but is returned null and not default?");

            controlWrapper.applyAttributes(currentWrapperState.getAttributeMap(), this);
            //currentWrapperState.getAttributeMap().setAttributesToControlWrapper();

            //This needs to be here after the #setAttributesToControlWrapper because stuff
            //can depend on attributes to be already set
            wrapperStateValueChangedConsumerSet.forEach(consumer -> consumer.accept(currentWrapperState));
        });
    }

    public WrapperState getDefaultState()
    {
        return defaultWrapperState;
    }

    public void changeCurrentState(WrapperState wrapperState)
    {
        if (wrapperStateList.contains(wrapperState) || wrapperState == defaultWrapperState)
        {
            this.currentWrapperState = wrapperState;
        }
    }

    public void initDefaultState(Consumer<WrapperState> initDefaultStateConsumer)
    {
        Validate.needFalse("Trying to initialize default wrapper state twice", defaultWrapperStateInitialized);
        defaultWrapperStateInitialized = true;

        //Is not needed to add the default to the state set since is manager differently
        initDefaultStateConsumer.accept(defaultWrapperState);
    }

    public void addState(WrapperState wrapperState)
    {
        this.addState(wrapperState, true);
    }

    public void addState(WrapperState wrapperState, boolean cloneFromDefault)
    {
        this.requireDefaultInit();

        wrapperStateList.add(wrapperState);

        if (cloneFromDefault) //If this is not used accurately, might lead to broken attribute maps
        {
            //This other than clone, it populates the added state with the attribute
            wrapperState.getAttributeMap().cloneFromOther(defaultWrapperState.getAttributeMap());
        }
    }

    public void removeState(WrapperState wrapperState)
    {
        this.requireDefaultInit();

        wrapperStateList.remove(wrapperState);
    }

    public WrapperState getCurrentState()
    {
        this.requireDefaultInit();
        return Objects.requireNonNullElse(currentWrapperState, defaultWrapperState);
    }

    public WrapperState getStateOf(int state)
    {
        this.requireDefaultInit();

        //Since this could be a pretty hot method, better use for loop that are a bit more efficient
        for (var wrapperState : wrapperStateList)
        {
            if (wrapperState.isActive(state))
            {
                return wrapperState;
            }
        }
        return defaultWrapperState;
    }

    public void forEachNoDefault(Consumer<WrapperState> consumer)
    {
        wrapperStateList.forEach(consumer);
    }

    public void forEach(Consumer<WrapperState> consumer)
    {
        consumer.accept(defaultWrapperState);
        this.forEachNoDefault(consumer);
    }

    public void removeStateValueChangedConsumer(Consumer<WrapperState> consumer)
    {
        wrapperStateValueChangedConsumerSet.remove(consumer);
    }

    public void addStateValueChangedConsumer(Consumer<WrapperState> consumer)
    {
        wrapperStateValueChangedConsumerSet.add(consumer);
    }

    public boolean contains(WrapperState wrapperState)
    {
        return wrapperStateList.contains(wrapperState);
    }

    @Override
    public JSONDataMap serialize()
    {
        this.requireDefaultInit();

        var jsonDataMap = super.serialize();

        jsonDataMap.set("DefaultWrapperState", WrapperStateSerializer.serializeDefaultState(defaultWrapperState));

        var jsonArray = new JSONDataArray();
        for (var wrapperState : wrapperStateList)
        {
            jsonArray.add(WrapperStateSerializer.serialize(wrapperState));
        }
        jsonDataMap.set("WrapperStateList", jsonArray);

        return jsonDataMap;
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        this.requireDefaultInit();

        super.deserialize(jsonDataMap);

        var defaultWrapperStateJSONDataMap = Objects.requireNonNull(jsonDataMap.getMap("DefaultWrapperState"),
                "A WrapperStateMap has no serialized DefaultWrapperState");
        WrapperStateSerializer.deserializeDefaultState(defaultWrapperStateJSONDataMap, defaultWrapperState);

        var jsonArray = jsonDataMap.getArray("WrapperStateList");
        if (jsonArray != null)
        {
            jsonArray.stream().filter(JSONObject.class::isInstance)
                    .map(JSONObject.class::cast)
                    .map(JSONDataMap::new)
                    .forEach(wrapperJSONDataMap -> WrapperStateSerializer.deserialize(wrapperJSONDataMap, this::addState));
        } else
        {
            Logger.getLogger(WrapperStateMap.class.getSimpleName()).log(Level.WARNING,
                    "WrapperStateList has not been found for WrapperStateMap");
        }

    }

    private void requireDefaultInit()
    {
        Validate.needTrue("Trying to execute operation on a non initialized default WrapperState", defaultWrapperStateInitialized);
    }

}
