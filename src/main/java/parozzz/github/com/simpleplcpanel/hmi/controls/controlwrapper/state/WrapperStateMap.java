package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.Nullable;
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

    private final WrapperState defaultWrapperState;
    private final ReadOnlyListWrapper<WrapperState> wrapperStateListProperty;
    private final ReadOnlyObjectWrapper<WrapperState> currentWrapperStateProperty;

    private boolean defaultStateInitialized = false;

    public WrapperStateMap(ControlWrapper<?> controlWrapper)
    {
        this.controlWrapper = controlWrapper;

        this.defaultWrapperState = new WrapperDefaultState(this);
        this.wrapperStateListProperty = new ReadOnlyListWrapper<>(new ObservableListWrapper<>(new LinkedList<>()));
        this.currentWrapperStateProperty = new ReadOnlyObjectWrapper<>(defaultWrapperState);
    }

    public ControlWrapper<?> getControlWrapper()
    {
        return controlWrapper;
    }

    public WrapperState getDefaultState()
    {
        return defaultWrapperState;
    }

    public WrapperState getCurrentState()
    {
        this.requireDefaultInit();
        return Objects.requireNonNullElse(currentWrapperStateProperty.getValue(), defaultWrapperState);
    }

    public ReadOnlyProperty<WrapperState> currentWrapperStateProperty()
    {
        return currentWrapperStateProperty.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<WrapperState> wrapperStateListProperty()
    {
        return wrapperStateListProperty.getReadOnlyProperty();
    }

    public void initDefaultState(ControlWrapperAttributeTypeManager attributeTypeManager)
    {
        Validate.needFalse("Trying to initialize empty attributes twice", defaultStateInitialized);
        defaultStateInitialized = true;

        defaultWrapperState.getAttributeMap().parseAttributes(attributeTypeManager, true);
    }

    @Nullable
    public WrapperState createState(WrapperState wrapperState)
    {
        return this.createState(
                wrapperState.getFirstCompare(), wrapperState.getFirstCompareType(),
                wrapperState.getSecondCompare(), wrapperState.getSecondCompareType()
        );
    }

    @Nullable
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

    public void setWrapperState(WrapperState wrapperState)
    {
        this.requireDefaultInit();
        if (wrapperStateListProperty.contains(wrapperState) || wrapperState == defaultWrapperState)
        {
            currentWrapperStateProperty.setValue(wrapperState);
        }
    }

    public void setNumericState(int numericState)
    {
        this.setWrapperState(this.getStateOf(numericState));
    }

    private boolean addState(WrapperState wrapperState)
    {
        this.requireDefaultInit();
        if (wrapperStateListProperty.contains(wrapperState))
        {
            return false;
        }

        var attributeManager = controlWrapper.getAttributeTypeManager();
        wrapperState.getAttributeMap().parseAttributes(attributeManager, true);

        defaultWrapperState.copyInto(wrapperState); //Copy the default state into it!
        wrapperStateListProperty.add(wrapperState); //Do this after initialization so for listener is already populated!
        return true;
    }

    public void removeState(WrapperState wrapperState)
    {
        this.requireDefaultInit();

        wrapperStateListProperty.remove(wrapperState);
        if (currentWrapperStateProperty.getValue() == wrapperState)
        {
            currentWrapperStateProperty.setValue(defaultWrapperState);
        }
    }

    public WrapperState getStateOf(int state)
    {
        this.requireDefaultInit();

        //Since this could be a pretty hot method, better use for loop that are a bit more efficient
        for (var wrapperState : wrapperStateListProperty)
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
        wrapperStateListProperty.forEach(wrapperState ->
        {
            var pasteWrapperState = pasteStateMap.createState(wrapperState);
            wrapperState.copyInto(pasteWrapperState);
        });

        defaultWrapperState.copyInto(pasteStateMap.getDefaultState());
    }

    public List<Attribute> getAllAttributesOfType(AttributeType<?> attributeType)
    {
        return wrapperStateListProperty.stream()
                .map(wrapperState -> wrapperState.getAttributeMap().get(attributeType))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void forEachNoDefault(Consumer<WrapperState> consumer)
    {
        wrapperStateListProperty.forEach(consumer);
    }

    public void forEach(Consumer<WrapperState> consumer)
    {
        consumer.accept(defaultWrapperState);
        this.forEachNoDefault(consumer);
    }

    public boolean contains(WrapperState wrapperState)
    {
        return wrapperStateListProperty.contains(wrapperState);
    }

    @Override
    public JSONDataMap serialize()
    {
        this.requireDefaultInit();

        var jsonDataMap = super.serialize();
        jsonDataMap.set("DefaultWrapperState", WrapperStateSerializer.serializeDefaultState(defaultWrapperState));

        var jsonArray = new JSONDataArray();
        wrapperStateListProperty.stream().map(WrapperStateSerializer::serialize).forEach(jsonArray::add);
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
