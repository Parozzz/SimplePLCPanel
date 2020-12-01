package parozzz.github.com.hmi.attribute;

import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeManager;

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

    public AttributeMap (ControlWrapper<?> controlWrapper)
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

    public void parseAttributes(ControlWrapperAttributeManager<?> attributeManager, boolean state)
    {
        typeToAttributeMap.clear();
        attributeSet.clear();

        if(state)
        {
            attributeManager.forEachStateType(attributeType ->
            {
                var attribute = attributeType.create(this);
                typeToAttributeMap.put(attributeType, attribute);
                attributeSet.add(attribute);

                super.addFXChild(attribute);
            });
        }
        else
        {
            attributeManager.forEachGlobalType(attributeType ->
            {
                var attribute = attributeType.create(this);
                typeToAttributeMap.put(attributeType, attribute);
                attributeSet.add(attribute);

                super.addFXChild(attribute);
            });
        }
    }

    /*
    public <A extends Attribute> boolean addAttribute(A attribute)
    {
        return this.addAttribute(attribute, FunctionalInterfaceUtil.emptyConsumer());
    }


    public <A extends Attribute> boolean addAttribute(A attribute, Consumer<A> consumer)
    {
        var attributeWrapper = new AttributeWrapper<>(attribute, consumer);
        return this.addAttributeWrapper(attributeWrapper);
    }
*/
    public ControlWrapper<?> getControlWrapper()
    {
        return controlWrapper;
    }

    /*public void addAll(Collection<Attribute> collection)
    {
        collection.forEach(this::addAttribute);
    }

    public boolean addAttribute(Attribute attribute)
    {
        if (classToAttributeMap.containsKey(attribute.getClass()))
        {
            return false;
        }

        if (attributeSet.add(attribute))
        {
            super.addFXChild(attribute);

            classToAttributeMap.put(attribute.getClass(), attribute);

            //When adding a new attribute, update internals values to create startup data
            attribute.update();

            return true;
        }

        return false;
    }*/
/*
    public void addOrReplaceAttribute(Attribute attribute)
    {
        var attributeClass = attribute.getClass();
        if(classToAttributeMap.containsKey(attributeClass))
        {
            this.removeAttribute(attributeClass);
        }

        this.addAttribute(attribute);
    }
*/
    public <T extends Attribute> T get(AttributeType<T> attributeType)
    {
        var attributeClass = attributeType.getAttributeClass();

        var attribute = typeToAttributeMap.get(attributeType);
        return attributeClass.isInstance(attribute)
                ? attributeClass.cast(attribute)
                : null;
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
            if(pasteAttribute != null)
            {
                attribute.copyInto(pasteAttribute);
            }
        });
    }
/*
    public void removeAttribute(Class<?> attributeClass)
    {
        var attribute = typeToAttributeMap.get(attributeClass);
        if (attribute != null)
        {
            this.removeAttribute(attribute);
        }
    }
*/
    /*
    public void removeAttribute(Attribute attribute)
    {
        if (attributeSet.remove(attribute))
        {
            super.removeFXChild(attribute);
            typeToAttributeMap.remove(attribute.getClass());
        }
    }*/

    /*
    public void setAttributesToControlWrapper()
    {
        attributeClassMap.values().forEach(AttributeWrapper::consume);
    }
*/
    public void forEach(Consumer<Attribute> consumer)
    {
        attributeSet.forEach(consumer);
    }

    /*
    public void cloneFromOther(AttributeMap otherAttributeMap)
    {
        for (var otherAttribute : otherAttributeMap.attributeSet)
        {
            this.addOrReplaceAttribute(otherAttribute.clone());
        }
    }*/

    /*
    private static class AttributeWrapper<A extends Attribute>
    {
        private final A attribute;
        private final Consumer<A> consumer;

        public AttributeWrapper(A attribute, Consumer<A> consumer)
        {
            this.attribute = attribute;
            this.consumer = consumer;
        }

        public void consume()
        {
            consumer.accept(attribute);
        }

        @Override
        public AttributeWrapper<A> clone()
        {
            return new AttributeWrapper<>((A) attribute.clone(), consumer);
        }
    }*/
}
