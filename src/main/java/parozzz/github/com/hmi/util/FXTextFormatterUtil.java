package parozzz.github.com.hmi.util;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class FXTextFormatterUtil
{

    public static TextFormatter<String> limited(int charLimit)
    {
        return new TextFormatter<>(change ->
        {
            var newText = change.getControlNewText();
            return newText.length() > charLimit
                    ? null
                    : change;
        });
    }

    public static TextFormatter<String> positiveInteger(int digitCount)
    {
        return integerBuilder().positiveOnly().digitCount(digitCount).getTextFormatter();
    }

    public static TextFormatter<String> simpleInteger(int digitCount)
    {
        return integerBuilder().digitCount(digitCount).getTextFormatter();
    }

    public static IntegerTextFormatterBuilder integerBuilder()
    {
        return new IntegerTextFormatterBuilder();
    }

    public static DoubleTextFormatterBuilder doubleBuilder()
    {
        return new DoubleTextFormatterBuilder();
    }

    public static class IntegerTextFormatterBuilder
    {
        private long minValue = Long.MIN_VALUE;
        private long maxValue = Long.MAX_VALUE;
        private boolean positiveOnly;
        private String defaultText = "";

        private IntegerTextFormatterBuilder()
        {
        }

        public IntegerTextFormatterBuilder defaultText(String defaultText)
        {
            this.defaultText = defaultText;
            return this;
        }

        public IntegerTextFormatterBuilder positiveOnly()
        {
            positiveOnly = true;

            minValue = 0;

            return this;
        }

        public IntegerTextFormatterBuilder digitCount(int digitCount)
        {
            maxValue = 0;
            for (int x = 0; x < digitCount; x++)
            {
                maxValue += 9 * Math.pow(10, x);
            }

            minValue = positiveOnly ? 0 : -maxValue;

            return this;
        }

        public IntegerTextFormatterBuilder min(long minValue)
        {
            this.minValue = positiveOnly && minValue < 0 ? 0 : minValue;
            return this;
        }

        public IntegerTextFormatterBuilder max(long maxValue)
        {
            this.maxValue = maxValue;
            return this;
        }

        public UnaryOperator<TextFormatter.Change> getChangeUnaryOperator()
        {
            return change ->
            {
                var newText = change.getControlNewText();
                if (newText.isEmpty())
                {
                    if (defaultText != null)
                    {
                        change.setText(defaultText);
                    }

                    return change;
                }

                if(newText.length() == 1 && newText.charAt(0) == '-')
                {
                    return change;
                }

                //Checks that all the digits are number
                long longValue;
                try
                {
                    longValue = Long.parseLong(newText);
                    if (longValue < minValue || longValue > maxValue)
                    {
                        return null;
                    }
                } catch (Exception exception)
                {
                    return null;
                }

                return change;
            };
        }

        public TextFormatter<String> getTextFormatter()
        {
            return new TextFormatter<>(this.getChangeUnaryOperator());
        }
    }

    public static class DoubleTextFormatterBuilder
    {
        private double minValue = -Double.MAX_VALUE;
        private double maxValue = Double.MAX_VALUE;
        private int maxDecimals = Integer.MAX_VALUE;
        private int maxDigits = Integer.MAX_VALUE;
        private String defaultText = "";

        private DoubleTextFormatterBuilder()
        {
        }

        public DoubleTextFormatterBuilder defaultText(String defaultText)
        {
            this.defaultText = defaultText;
            return this;
        }

        public DoubleTextFormatterBuilder min(double minValue)
        {
            this.minValue = minValue;
            return this;
        }

        public DoubleTextFormatterBuilder max(double maxValue)
        {
            this.maxValue = maxValue;
            return this;
        }

        public DoubleTextFormatterBuilder maxDecimals(int maxDecimals)
        {
            this.maxDecimals = maxDecimals;
            return this;
        }

        public DoubleTextFormatterBuilder maxDigits(int maxDigits)
        {
            this.maxDigits = maxDigits;
            return this;
        }

        public UnaryOperator<TextFormatter.Change> getChangeUnaryOperator()
        {
            return change ->
            {
                var newText = change.getControlNewText();
                if (newText.isEmpty())
                {
                    if (defaultText != null)
                    {
                        change.setText(defaultText);
                    }

                    return change;
                }

                if(newText.length() == 1 && newText.charAt(0) == '-')
                {
                    return change;
                }

                /*
                //Checks that all the digits are number or point
                for (var c : newText.toCharArray())
                {
                    if (!(Character.isDigit(c) || c == '.'))
                    {
                        return null;
                    }
                }
*/
                double doubleValue;
                try
                {
                    doubleValue = Double.parseDouble(newText);
                    if (doubleValue < minValue || doubleValue > maxValue)
                    {
                        return null;
                    }
                } catch (Exception exception)
                {
                    return null;
                }

                var absDoubleValue = Math.abs(doubleValue);

                var splittedText = Double.toString(absDoubleValue).split("\\.");
                if (splittedText.length == 0 || splittedText.length > 2)
                {
                    return null;
                }

                if (splittedText[0].length() > maxDigits ||
                        (splittedText.length == 2 && splittedText[1].length() > maxDecimals))
                {
                    return null;
                }

                return change;
            };
        }

        public TextFormatter<String> getTextFormatter()
        {
            return new TextFormatter<>(this.getChangeUnaryOperator());
        }
    }

}
