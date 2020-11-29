package parozzz.github.com.hmi.controls.controlwrapper.state;

import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.util.functionalinterface.primitives.IntBiPredicate;

import java.util.Objects;

public class WrapperState implements Comparable<WrapperState>
{
    public enum CompareType
    {
        ALWAYS_TRUE((t1, t2) -> true),
        EQUAL((t1, t2) -> t1 == t2),
        HIGHER((t1, t2) -> t1 > t2),
        HIGHER_EQUAL((t1, t2) -> t1 >= t2),
        LOWER((t1, t2) -> t1 < t2),
        LOWER_EQUAL((t1, t2) -> t1 <= t2);
        //BETWEEN("{first}<X<{second}", false, (value, low, high) -> value < high && value > low),
        //BETWEEN_EQUAL("{first}<=X<={second}", false, (value, low, high) -> value <= high && value >= low);

        private final IntBiPredicate predicate;

        CompareType(IntBiPredicate predicate)
        {
            this.predicate = predicate;
        }

        public boolean test(int compare, int value)
        {
            return predicate.test(value, compare);
        }

        public String getVisualText()
        {
            switch (this)
            {
                case EQUAL:
                    return "==";
                case HIGHER_EQUAL:
                case LOWER_EQUAL:
                    return "<=";
                case HIGHER:
                case LOWER:
                    return "<";
                default:
                    return "";
            }
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }

    private static String createStringVersion(int firstCompare, CompareType firstCompareType,
            int secondCompare, CompareType secondCompareType)
    {

        var stringBuilder = new StringBuilder();
        if (firstCompareType != CompareType.ALWAYS_TRUE)
        {
            stringBuilder.append(firstCompare)
                    .append(firstCompareType.getVisualText());
        }

        stringBuilder.append("X");

        if (secondCompareType != CompareType.ALWAYS_TRUE)
        {
            stringBuilder.append(secondCompareType.getVisualText())
                    .append(secondCompare);
        }

        return stringBuilder.toString();
    }

    private final String stringVersion;
    private final AttributeMap attributeMap;

    private final short firstCompare;
    private final CompareType firstCompareType;
    private final short secondCompare;
    private final CompareType secondCompareType;

    WrapperState(int firstCompare, CompareType firstCompareType,
            int secondCompare, CompareType secondCompareType)
    {
        this.firstCompare = (short) firstCompare;
        this.firstCompareType = firstCompareType;
        this.secondCompare = (short) secondCompare;
        this.secondCompareType = secondCompareType;

        this.stringVersion = createStringVersion(firstCompare, firstCompareType, secondCompare, secondCompareType);

        this.attributeMap = new AttributeMap();
    }

    //NEED TO DO A WAY TO PARSE A STATE FROM A STRING. SAME FOR A WAY TO TRASFORM IT BACK TO A STRING.
    //OR MAYBE DO SOMETHING FORCED WITH THE GUI?

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

    public CompareType getFirstCompareType()
    {
        return firstCompareType;
    }

    public int getFirstCompare()
    {
        return firstCompare;
    }

    public CompareType getSecondCompareType()
    {
        return secondCompareType;
    }

    public int getSecondCompare()
    {
        return secondCompare;
    }

    public boolean isActive(int value)
    {
        return firstCompareType.test(firstCompare, value)
                && secondCompareType.test(secondCompare, value);
    }

    public WrapperState cloneEmpty()
    {
        return new WrapperState(firstCompare, firstCompareType, secondCompare, secondCompareType);
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
        if (wrapperState == this)
        {
            return 0;
        } else if (wrapperState.isDefault())
        {
            return 1;
        }

        var otherFirstCompare = wrapperState.getFirstCompare();
        if (firstCompare > otherFirstCompare)
        {
            return 1;
        } else if (firstCompare < otherFirstCompare)
        {
            return -1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }


        if (!(object instanceof WrapperState))
        {
            return false;
        }

        var otherWrapperState = (WrapperState) object;
        return firstCompare == otherWrapperState.firstCompare && firstCompareType == otherWrapperState.firstCompareType
                && secondCompare == otherWrapperState.secondCompare && secondCompareType == otherWrapperState.secondCompareType;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(firstCompare, firstCompareType, secondCompare, secondCompareType);
    }


    public static class Builder
    {
        private int firstCompare;
        private CompareType firstCompareType = CompareType.ALWAYS_TRUE;

        private int secondCompare;
        private CompareType secondCompareType = CompareType.ALWAYS_TRUE;

        private Builder()
        {

        }

        public Builder firstCompare(CompareType compareType, int firstCompare)
        {
            switch (compareType)
            {
                case EQUAL:
                case HIGHER:
                case HIGHER_EQUAL:
                    this.firstCompare = firstCompare;
                    this.firstCompareType = compareType;
                    break;
            }

            return this;
        }

        public Builder secondCompare(CompareType compareType, int secondCompare)
        {
            switch (compareType)
            {
                case EQUAL:
                case LOWER:
                case LOWER_EQUAL:
                    this.secondCompare = secondCompare;
                    this.secondCompareType = compareType;
                    break;
            }

            return this;
        }

        public WrapperState create()
        {
            return new WrapperState(firstCompare, firstCompareType, secondCompare, secondCompareType);
        }
    }
}
