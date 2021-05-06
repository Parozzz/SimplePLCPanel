package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.converter.IntegerStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.awt.*;
import java.io.IOException;

public abstract class NetworkCommunicationManager<T extends CommThread<?>> extends FXObject
{
    //These values here are needed for every network setup!
    @FXML
    private CheckBox alwaysRetryConnectionCheckBox;
    @FXML
    private TextField timeBetweenRetriesTextField;
    @FXML
    private Button connectButton;

    protected final T commThread;
    private boolean firstRun = true;

    public NetworkCommunicationManager(String name, T commThread) throws IOException
    {
        super(name);

        this.commThread = commThread;
    }

    public T getCommThread()
    {
        return commThread;
    }

    @Override
    public void setup()
    {
        super.setup();

        serializableDataSet.addBoolean("AlwaysRetryConnection", alwaysRetryConnectionCheckBox.selectedProperty())
                .addString("TimeBetweenRetries", timeBetweenRetriesTextField.textProperty(), "10");

        timeBetweenRetriesTextField.setTextFormatter(
                FXTextFormatterUtil.integerBuilder().min(1).digitCount(4).getTextFormatter()
        );
        timeBetweenRetriesTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            var time = Math.min(1, Util.parseInt(newValue, -1));
            commThread.setTimeBetweenRetries(time);
        });

        alwaysRetryConnectionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> commThread.setAlwaysRetryConnection(newValue));
        connectButton.setOnAction(event -> this.parseAndUpdateCommunicationParams());
    }

    @Override
    public void loop()
    {
        super.loop();

        if(!commThread.isActive())
        {
            return;
        }

        //This allows the communication to auto-start the first time!
        if(firstRun)
        {
            firstRun = false;
            this.parseAndUpdateCommunicationParams();
        }
    }

    public abstract Parent getParent();

    public abstract void parseAndUpdateCommunicationParams();

    public void setActive(boolean active)
    {
        commThread.setActive(active);
    }

    public boolean isActive()
    {
        return commThread.isActive();
    }

    protected void registerSwitchToNextTextFieldOnDecimalPress(TextField textField, TextField nextTextField)
    {
        textField.setOnKeyPressed(event ->
        {
            if(event.getCode() == KeyCode.DECIMAL)
            {
                nextTextField.requestFocus();
            }
        });
    }
}
