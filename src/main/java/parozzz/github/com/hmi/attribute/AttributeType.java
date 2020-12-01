package parozzz.github.com.hmi.attribute;

import parozzz.github.com.hmi.attribute.impl.*;
import parozzz.github.com.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.control.ButtonDataAttribute;
import parozzz.github.com.hmi.attribute.impl.control.InputDataAttribute;

import java.util.ArrayList;
import java.util.List;

public final class AttributeType<A extends Attribute>
{
    private static List<AttributeType<?>> ATTRIBUTE_TYPE_LIST;

    private static <A extends Attribute> AttributeType<A> create(Class<A> attributeClass,
            AttributeSupplier<A> attributeSupplier)
    {
        if(ATTRIBUTE_TYPE_LIST == null)
        {
            ATTRIBUTE_TYPE_LIST = new ArrayList<>();
        }

        var attributeType = new AttributeType<>(attributeClass, attributeSupplier);
        ATTRIBUTE_TYPE_LIST.add(attributeType);
        return attributeType;
    }

    //STATE BASED
    public static final AttributeType<BackgroundAttribute> BACKGROUND = create(BackgroundAttribute.class, BackgroundAttribute::new);
    public static final AttributeType<BorderAttribute> BORDER = create(BorderAttribute.class, BorderAttribute::new);
    public static final AttributeType<FontAttribute> FONT = create(FontAttribute.class, FontAttribute::new);
    public static final AttributeType<SizeAttribute> SIZE = create(SizeAttribute.class, SizeAttribute::new);
    public static final AttributeType<TextAttribute> TEXT = create(TextAttribute.class, TextAttribute::new);
    public static final AttributeType<ValueAttribute> VALUE = create(ValueAttribute.class, ValueAttribute::new);
    public static final AttributeType<WriteAddressAttribute> WRITE_ADDRESS = create(WriteAddressAttribute.class, WriteAddressAttribute::new);

    //GLOBAL BASED
    public static final AttributeType<ChangePageAttribute> CHANGE_PAGE = create(ChangePageAttribute.class, ChangePageAttribute::new);
    public static final AttributeType<ButtonDataAttribute> BUTTON_DATA = create(ButtonDataAttribute.class, ButtonDataAttribute::new);
    public static final AttributeType<InputDataAttribute> INPUT_DATA = create(InputDataAttribute.class, InputDataAttribute::new);
    public static final AttributeType<ReadAddressAttribute> READ_ADDRESS = create(ReadAddressAttribute.class, ReadAddressAttribute::new);

    private final Class<A> attributeClass;
    private final AttributeSupplier<A> attributeSupplier;

    private AttributeType(Class<A> attributeClass, AttributeSupplier<A> attributeSupplier)
    {
        this.attributeClass = attributeClass;
        this.attributeSupplier = attributeSupplier;
    }

    public Class<A> getAttributeClass()
    {
        return attributeClass;
    }

    public A create(AttributeMap attributeMap)
    {
        return attributeSupplier.supply(attributeMap);
    }

    @FunctionalInterface
    public interface AttributeSupplier<A extends Attribute>
    {
        A supply(AttributeMap attributeMap);
    }
}
