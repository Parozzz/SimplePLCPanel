package parozzz.github.com.simpleplcpanel.hmi.util.textfields;

import javafx.scene.control.TextField;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.util.function.Consumer;

public final class IntegerTextField implements NumericTextField
{
    private final TextField textField;
    private Consumer<Integer> newValueConsumer;

    public IntegerTextField(TextField textField)
    {
        this.textField = textField;
    }

    public void init()
    {
        textField.setOnAction(event -> this.parseText());
        textField.focusedProperty().addListener((observable, oldValue, newValue) ->
        {
            if(!newValue) //When focus is lost.
            {
                this.parseText();
            }
        });
    }

    public IntegerTextField newValueConsumer(Consumer<Integer> consumer)
    {
        this.newValueConsumer = consumer;
        return this;
    }

    public void setValue(int value)
    {
        textField.setText(Integer.toString(value));
    }

    @Override
    public TextField getTextField()
    {
        return textField;
    }

    @Override
    public String getText()
    {
        return textField.getText();
    }

    private void parseText()
    {
        if (newValueConsumer != null)
        {
            try
            {
                newValueConsumer.accept(Integer.parseInt(textField.getText()));
            } catch (NumberFormatException ignored) {

            }
        }
    }
}
