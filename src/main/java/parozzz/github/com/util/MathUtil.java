package parozzz.github.com.util;

public class MathUtil
{
    public static double findNearestToZero(double... doubles)
    {
        if (doubles.length == 0)
        {
            return 0d;
        }

        if (doubles.length == 1)
        {
            return doubles[0];
        }

        double nearNumberZero = 0;
        for (int x = 0; x < doubles.length; x++)
        {
            var number = doubles[x];
            // by default first is nearest or it will check for other numbers
            if (x == 0 || Math.abs(number) < Math.abs(nearNumberZero))
            {
                nearNumberZero = number;
            }
        }
        return nearNumberZero;
    }
}
