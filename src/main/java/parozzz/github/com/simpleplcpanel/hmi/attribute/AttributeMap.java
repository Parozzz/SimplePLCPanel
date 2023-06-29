package parozzz.github.com.simpleplcpanel.hmi.attribute;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes.ControlWrapperAttributeTypeManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class AttributeMap extends FXController
{
    private final ControlWrapper<?> controlWrapper;
    private final Map<AttributeType<?>, Attribute> typeToAttributeMap;
    private final Set<Attribute> attributeSet;

    public AttributeMap(ControlWrapper<?> controlWrapper)
    {
        this(controlWrapper, "AttributeMap");
    }

    public AttributeMap(ControlWrapper<?> controlWrapper, String name)
    {
        super(name);

        this.controlWrapper = controlWrapper;
        this.typeToAttributeMap = new HashMap<>();
        this.attributeSet = new HashSet<>();
    }

    public void parseAttributes(ControlWrapperAttributeTypeManager attributeTypeManager, boolean state)
    {
        typeToAttributeMap.clear();
        attributeSet.clear();

        (state ? attributeTypeManager.getStateTypeCollection() : attributeTypeManager.getGlobalTypeCollection()).forEach(attributeType ->
        {
            var attribute = attributeType.create(this);
            super.addFXChild(attribute);
            typeToAttributeMap.put(attributeType, attribute);
            attributeSet.add(attribute);
        });
    }

    public ControlWrapper<?> getControlWrapper()
    {
        return controlWrapper;
    }

    @Nullable
    public <T extends Attribute> T get(AttributeType<T> attributeType)
    {
        var attributeClass = attributeType.getAttributeClass();

        var attribute = typeToAttributeMap.get(attributeType);
        return attributeClass.isInstance(attribute)
                ? attributeClass.cast(attribute)
                : null;
    }

    public <T extends Attribute> T getRequired(AttributeType<T> attributeType)
    {
        var attributeClass = attributeType.getAttributeClass();

        var attribute = typeToAttributeMap.get(attributeType);
        if(attributeClass.isInstance(attribute))
        {
            return attributeClass.cast(attribute);
        }

        throw new IllegalArgumentException("An attribute of type "
                + attributeType.getAttributeClass().getSimpleName() +
                " is not present inside AttributeMap althougn required.");
    }

    public boolean contains(Attribute attribute)
    {
        return attributeSet.contains(attribute);
    }

    public boolean hasType(AttributeType<?> attributeType)
    {
        return typeToAttributeMap.containsKey(attributeType);
    }

    public void copyInto(AttributeMap pasteAttributeMap)
    {
        attributeSet.forEach(attribute ->
        {
            var attributeType = attribute.getType();

            var pasteAttribute = pasteAttributeMap.get(attributeType);
            if (pasteAttribute != null)
            {
                attribute.copyInto(pasteAttribute);
            }
        });
    }

    public void forEach(Consumer<Attribute> consumer)
    {
        attributeSet.forEach(consumer);
    }
}
