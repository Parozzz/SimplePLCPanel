package parozzz.github.com.simpleplcpanel.util;

public class MathUtil
{
    /**
     * Get the nearest number to 0 (Not the lowest) independent of the sign
     * @param doubles The varargs of values
     * @return The number nearest to zero
     */
    public static double findNearestToZero(double... doubles)
    {
        switch (doubles.length)
        {
            case 0:
                return 0d;
            case 1:
                return doubles[0];
            default:
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

    public static double between(double min, double v, double max)
    {
        return Math.max(min, Math.min(max, v));
    }
}
