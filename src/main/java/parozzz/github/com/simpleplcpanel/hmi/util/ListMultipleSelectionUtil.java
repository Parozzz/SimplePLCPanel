package parozzz.github.com.simpleplcpanel.hmi.util;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ListMultipleSelectionUtil
{

    /**
     * Get all the value between the start value and the end value. If the start is lower than the end, the values
     * will be selected upwards.
     * @param start The value to start selecting from
     * @param end The value to end selecting from
     * @return The list will all the values selected (With start and end included). Can be null.
     * @param <T>
     */
    public static <T> List<T> getAllMiddleValues(List<T> items, T start, T end)
    {
        var startIndex = items.indexOf(start);
        var endIndex = items.indexOf(end);
        if (startIndex == -1 || endIndex == -1)
        {
            return null;
        }

        //If the first selection is higher than the second I'll invert them to have the lower
        // always in the first.
        if (startIndex > endIndex)
        {
            var bubble = startIndex;
            startIndex = endIndex;
            endIndex = bubble;
        }

        var retList = new ArrayList<T>();
        for (int x = startIndex; x <= endIndex; x++)
        {
            var item = items.get(x);
            retList.add(item);
        }

        return retList;
    }


    private ListMultipleSelectionUtil() {}
}
