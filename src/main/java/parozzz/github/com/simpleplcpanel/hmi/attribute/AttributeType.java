package parozzz.github.com.simpleplcpanel.hmi.attribute;

import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.*;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.control.ButtonDataAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.control.InputDataAttribute;

import java.util.ArrayList;
import java.util.List;

public final class AttributeType<A extends Attribute>
{
    private static List<AttributeType<?>> ATTRIBUTE_TYPE_LIST;

    private static <A extends Attribute> AttributeType<A> create(String attributeName, Class<A> attributeClass,
            AttributeSupplier<A> attributeSupplier)
    {
        if(ATTRIBUTE_TYPE_LIST == null)
        {
            ATTRIBUTE_TYPE_LIST = new ArrayList<>();
        }

        var attributeType = new AttributeType<>(attributeName, attributeClass, attributeSupplier);
        ATTRIBUTE_TYPE_LIST.add(attributeType);
        return attributeType;
    }

    //STATE BASED
    public static final AttributeType<BackgroundAttribute> BACKGROUND = create("Background", BackgroundAttribute.class, BackgroundAttribute::new);
    public static final AttributeType<BorderAttribute> BORDER = create("Border", BorderAttribute.class, BorderAttribute::new);
    public static final AttributeType<FontAttribute> FONT = create("Font", FontAttribute.class, FontAttribute::new);
    public static final AttributeType<SizeAttribute> SIZE = create("Size", SizeAttribute.class, SizeAttribute::new);
    public static final AttributeType<TextAttribute> TEXT = create("Text", TextAttribute.class, TextAttribute::new);
    public static final AttributeType<ValueAttribute> VALUE = create("Value", ValueAttribute.class, ValueAttribute::new);
    public static final AttributeType<WriteAddressAttribute> WRITE_ADDRESS = create("WriteTag", WriteAddressAttribute.class, WriteAddressAttribute::new);

    //GLOBAL BASED
    public static final AttributeType<ChangePageAttribute> CHANGE_PAGE = create("ChangePage", ChangePageAttribute.class, ChangePageAttribute::new);
    public static final AttributeType<ButtonDataAttribute> BUTTON_DATA = create("ButtonData", ButtonDataAttribute.class, ButtonDataAttribute::new);
    public static final AttributeType<InputDataAttribute> INPUT_DATA = create("InputData", InputDataAttribute.class, InputDataAttribute::new);
    public static final AttributeType<ReadAddressAttribute> READ_ADDRESS = create("ReadTag", ReadAddressAttribute.class, ReadAddressAttribute::new);

    private final String attributeName;
    private final Class<A> attributeClass;
    private final AttributeSupplier<A> attributeSupplier;

    private AttributeType(String attributeName, Class<A> attributeClass, AttributeSupplier<A> attributeSupplier)
    {
        this.attributeName = attributeName;
        this.attributeClass = attributeClass;
        this.attributeSupplier = attributeSupplier;
    }

    public String getName()
    {
        return attributeName;
    }

    public Class<A> getAttributeClass()
    {
        return attributeClass;
    }

    public A create(AttributeMap attributeMap)
    {
        return attributeSupplier.supply(attributeMap);
    }

    @Override
    public String toString()
    {
        return attributeClass.getSimpleName();
    }

    @FunctionalInterface
    public interface AttributeSupplier<A extends Attribute>
    {
        A supply(AttributeMap attributeMap);
    }
}
