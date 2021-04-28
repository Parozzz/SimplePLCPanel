package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state;

import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeTypeManager;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.util.Validate;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class WrapperStateMap extends FXObject
{
    private final ControlWrapper<?> controlWrapper;

    private final List<WrapperState> wrapperStateList;
    private final WrapperState defaultWrapperState;

    private WrapperState currentWrapperState;
    private int numericState;

    private final Set<WrapperStateChangedConsumer> wrapperStateChangedConsumerList;

    private boolean defaultStateInitialized = false;

    public WrapperStateMap(ControlWrapper<?> controlWrapper)
    {
        this.controlWrapper = controlWrapper;

        this.wrapperStateList = new LinkedList<>();
        this.defaultWrapperState = new WrapperDefaultState(this);

        this.wrapperStateChangedConsumerList = new HashSet<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        var externalValue = controlWrapper.getValue().getOutsideValue();
        externalValue.addNewValueRunnable(() ->
        {
            numericState = externalValue.asInteger();
            this.parseState(WrapperStateChangedConsumer.ChangeType.STATE_CHANGED);
        });
    }

    public ControlWrapper<?> getControlWrapper()
    {
        return controlWrapper;
    }

    public WrapperState getDefaultState()
    {
        return defaultWrapperState;
    }

    public void forceCurrentState(WrapperState forcedWrapperState)
    {
        this.requireDefaultInit();
        if (wrapperStateList.contains(forcedWrapperState) || forcedWrapperState == defaultWrapperState)
        {
            var oldWrapperState = currentWrapperState;
            this.currentWrapperState = forcedWrapperState;
            if(!currentWrapperState.equals(oldWrapperState)) //Using current as primary since the old can be null!
            { //Apply attributes only if the state is actually changed!
                controlWrapper.getAttributeUpdater().updateAllAttributes();
            }

            wrapperStateChangedConsumerList.forEach(consumer ->
                    consumer.stateChanged(this, oldWrapperState, WrapperStateChangedConsumer.ChangeType.STATE_CHANGED)
            );
        }
    }

    public void initDefaultState(ControlWrapperAttributeTypeManager attributeTypeManager)
    {
        Validate.needFalse("Trying to initialize empty attributes twice", defaultStateInitialized);
        defaultStateInitialized = true;

        defaultWrapperState.getAttributeMap().parseAttributes(attributeTypeManager, true);
    }

    public WrapperState createState(WrapperState wrapperState)
    {
        return this.createState(
                wrapperState.getFirstCompare(), wrapperState.getFirstCompareType(),
                wrapperState.getSecondCompare(), wrapperState.getSecondCompareType()
        );
    }

    public WrapperState createState(int firstCompare, WrapperState.CompareType firstCompareType,
            int secondCompare, WrapperState.CompareType secondCompareType)
    {
        var wrapperState = new WrapperState(
                this,
                firstCompare, firstCompareType,
                secondCompare, secondCompareType
        );
        return this.addState(wrapperState) ? wrapperState : null;
    }

    public WrapperState.Builder stateBuilder()
    {
        return new WrapperState.Builder(this);
    }

    private boolean addState(WrapperState wrapperState)
    {
        this.requireDefaultInit();
        if(wrapperStateList.contains(wrapperState))
        {
            return false;
        }

        wrapperStateList.add(wrapperState);

        var attributeManager = controlWrapper.getAttributeTypeManager();
        wrapperState.getAttributeMap().parseAttributes(attributeManager, true);

        defaultWrapperState.copyInto(wrapperState); //Copy the default state into it!

        this.parseState(WrapperStateChangedConsumer.ChangeType.ADD); //In case a new state is added that is more valid than others, it should be updated immediately!
        return true;
    }

    public void removeState(WrapperState wrapperState)
    {
        this.requireDefaultInit();

        wrapperStateList.remove(wrapperState);
        this.parseState(WrapperStateChangedConsumer.ChangeType.REMOVE); //In case something changes, reparse.
    }

    public int getNumericState()
    {
        this.requireDefaultInit();
        return numericState;
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

    public void copyInto(WrapperStateMap pasteStateMap)
    {
        wrapperStateList.forEach(wrapperState ->
        {
            var pasteWrapperState = pasteStateMap.createState(wrapperState);
            wrapperState.copyInto(pasteWrapperState);
        });

        defaultWrapperState.copyInto(pasteStateMap.getDefaultState());
    }

    public void forEachNoDefault(Consumer<WrapperState> consumer)
    {
        wrapperStateList.forEach(consumer);
    }

    public List<Attribute> getAllAttributesOfType(AttributeType<?> attributeType)
    {
        return wrapperStateList.stream()
                .map(wrapperState -> wrapperState.getAttributeMap().get(attributeType))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void forEach(Consumer<WrapperState> consumer)
    {
        consumer.accept(defaultWrapperState);
        this.forEachNoDefault(consumer);
    }

    public void addStateValueChangedConsumer(WrapperStateChangedConsumer consumer)
    {
        wrapperStateChangedConsumerList.add(consumer);
    }

    public void removeStateValueChangedConsumer(WrapperStateChangedConsumer consumer)
    {
        wrapperStateChangedConsumerList.remove(consumer);
    }

    public boolean contains(WrapperState wrapperState)
    {
        return wrapperStateList.contains(wrapperState);
    }

    private void parseState(WrapperStateChangedConsumer.ChangeType changeType)
    {
        var oldWrapperState = currentWrapperState;
        currentWrapperState = Objects.requireNonNull(this.getStateOf(numericState),
                "Trying to get a state but is returned null and not default?");

        //Apply attributes only if the state is actually changed!
        if(!currentWrapperState.equals(oldWrapperState)) //Using current as primary since the old can be null!
        {
            controlWrapper.getAttributeUpdater().updateAllAttributes();
        }

        //This needs to be here after the #setAttributesToControlWrapper because stuff
        //can depend on attributes to be already set
        wrapperStateChangedConsumerList.forEach(consumer ->
                consumer.stateChanged(this, oldWrapperState, changeType)
        );
    }

    @Override
    public JSONDataMap serialize()
    {
        this.requireDefaultInit();

        var jsonDataMap = super.serialize();
        jsonDataMap.set("DefaultWrapperState", WrapperStateSerializer.serializeDefaultState(defaultWrapperState));

        var jsonArray = new JSONDataArray();
        wrapperStateList.stream().map(WrapperStateSerializer::serialize).forEach(jsonArray::add);
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
                    .forEach(wrapperJSONDataMap ->
                            WrapperStateSerializer.deserializeAndAddState(this, wrapperJSONDataMap)
                    );
        } else
        {
            Logger.getLogger(WrapperStateMap.class.getSimpleName()).log(Level.WARNING,
                    "WrapperStateList has not been found for WrapperStateMap");
        }

    }

    private void requireDefaultInit()
    {
        Validate.needTrue("Trying to execute operation on a non initialized default WrapperState", defaultStateInitialized);
    }

}