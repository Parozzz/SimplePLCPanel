package parozzz.github.com.hmi.attribute;

import parozzz.github.com.hmi.FXController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class AttributeMap extends FXController
{
    private final Map<Class<?>, Attribute> classToAttributeMap;
    private final Set<Attribute> attributeSet;

    public AttributeMap()
    {
        this("AttributeMap");
    }

    public AttributeMap(String name)
    {
        super(name);

        this.classToAttributeMap = new HashMap<>();
        this.attributeSet = new HashSet<>();
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
            attribute.updateInternals();

            return true;
        }

        return false;
    }

    <T> T getAttribute(Class<T> attributeClass)
    {
        var attribute = classToAttributeMap.get(attributeClass);
        return attributeClass.isInstance(attribute)
                ? attributeClass.cast(attribute)
                : null;
    }

    public boolean hasAttribute(Attribute attribute)
    {
        return attributeSet.contains(attribute);
    }

    public boolean hasAttribute(Class<? extends Attribute> attributeClass)
    {
        return classToAttributeMap.containsKey(attributeClass);
    }

    public void removeAttribute(Class<?> attributeClass)
    {
        var attribute = classToAttributeMap.get(attributeClass);
        if (attribute != null)
        {
            this.removeAttribute(attribute);
        }
    }

    public void removeAttribute(Attribute attribute)
    {
        if (attributeSet.remove(attribute))
        {
            super.removeFXChild(attribute);
            classToAttributeMap.remove(attribute.getClass());
        }
    }

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

    public void cloneOtherAttributeMap(AttributeMap otherAttributeMap)
    {
        for (var otherAttribute : otherAttributeMap.attributeSet)
        {
            this.addAttribute(otherAttribute.clone());
        }
    }

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
