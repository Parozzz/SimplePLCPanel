package parozzz.github.com.hmi.controls.controlwrapper.state;

import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.util.functionalinterface.primitives.IntTriPredicate;

import java.util.Objects;

public class WrapperState implements Comparable<WrapperState>
{
    private final static String FIRST_COMPARE_PLACEHOLDER = "{first}";
    private final static String SECOND_COMPARE_PLACEHOLDER = "{second}";

    public enum Type
    {
        EQUAL("X=={first}", true, (value, first, second) -> value == first),
        HIGHER("X>{first}", true, (value, low, high) -> value > low),
        HIGHER_EQUAL("X>={first}", true, (value, low, high) -> value >= low),
        LOWER("X<{second}", true, (value, low, high) -> value < high),
        LOWER_EQUAL("X<={second}", true, (value, low, high) -> value <= high),
        BETWEEN("{first}<X<{second}", false, (value, low, high) -> value < high && value > low),
        BETWEEN_EQUAL("{first}<=X<={second}", false, (value, low, high) -> value <= high && value >= low);

        private final String text;
        private final boolean singleCompare;
        private final IntTriPredicate predicate;

        Type(String text, boolean singleCompare, IntTriPredicate predicate)
        {
            this.text = text;
            this.singleCompare = singleCompare;
            this.predicate = predicate;
        }

        public String getTextWithoutPlaceholders()
        {
            return text.replace(FIRST_COMPARE_PLACEHOLDER, "")
                    .replace(SECOND_COMPARE_PLACEHOLDER, "");
        }

        public String getTextWithPlaceholders()
        {
            return text;
        }

        public boolean isSingleCompare()
        {
            return singleCompare;
        }

        public boolean compare(int value, int firstCompare, int secondCompare)
        {
            return predicate.test(value, firstCompare, secondCompare);
        }

        public WrapperState create(int lowerBound, int higherBound)
        {
            return lowerBound < higherBound
                     ? new WrapperState(this, lowerBound, higherBound)
                     : null;
        }

        public WrapperState create(int compare)
        {
            switch(this)
            {
                case EQUAL:
                case LOWER:
                case LOWER_EQUAL:
                    return new WrapperState(this, compare, 0);
                case HIGHER:
                case HIGHER_EQUAL:
                    return new WrapperState(this, 0, compare);
                default:
                    return null;
            }
        }

        public WrapperState cloneEmpty(WrapperState wrapperState)
        {
            var firstCompare = wrapperState.firstCompare;
            var secondCompare = wrapperState.secondCompare;

            if(firstCompare == secondCompare || secondCompare == 0)
            {
                return create(firstCompare);
            }
            else if(firstCompare == 0)
            {
                return create(secondCompare);
            }

            return this.create(firstCompare, secondCompare);
        }
    }

    private static String createStringVersion(Type type, int firstCompare, int secondCompare)
    {
        return type.getTextWithPlaceholders().replace(FIRST_COMPARE_PLACEHOLDER, Integer.toString(firstCompare))
                .replace(SECOND_COMPARE_PLACEHOLDER, Integer.toString(secondCompare));
    }

    private final Type type;

    private final String stringVersion;
    private final AttributeMap attributeMap;

    private final int firstCompare;
    private final int secondCompare;

    WrapperState(Type type, int firstCompare)
    {
        this(type, firstCompare, 0);
    }

    WrapperState(Type type, int firstCompare, int secondCompare)
    {
        this.type = type;
        this.firstCompare = firstCompare;
        this.secondCompare = secondCompare;
        this.stringVersion = createStringVersion(type, firstCompare, secondCompare);

        this.attributeMap = new AttributeMap();
    }

    //NEED TO DO A WAY TO PARSE A STATE FROM A STRING. SAME FOR A WAY TO TRASFORM IT BACK TO A STRING.
    //OR MAYBE DO SOMETHING FORCED WITH THE GUI?

    public Type getType()
    {
        return type;
    }

    public String getStringVersion()
    {
        return stringVersion;
    }

    public AttributeMap getAttributeMap()
    {
        return attributeMap;
    }

    public boolean isDefault()
    {
        return false;
    }

    public int getFirstCompare()
    {
        return firstCompare;
    }

    public int getSecondCompare()
    {
        return secondCompare;
    }

    public boolean isActive(int value)
    {
        return type.compare(value, firstCompare, secondCompare);
    }

    public WrapperState cloneEmpty()
    {
        return type.cloneEmpty(this);
    }

    @Override
    public WrapperState clone()
    {
        var clonedWrapperState = this.cloneEmpty();
        Objects.requireNonNull(clonedWrapperState, "Trying to clone an invalid state.");
        clonedWrapperState.getAttributeMap().cloneFromOther(attributeMap);
        return clonedWrapperState;
    }

    @Override
    public int compareTo(WrapperState wrapperState)
    {
        if(wrapperState == this)
        {
            return 0;
        }else if(wrapperState.isDefault())
        {
            return 1;
        }

        var otherFirstCompare = wrapperState.getFirstCompare();
        if(firstCompare > otherFirstCompare)
        {
            return 1;
        }else if(firstCompare < otherFirstCompare)
        {
            return -1;
        }

        return 0;
    }
}
