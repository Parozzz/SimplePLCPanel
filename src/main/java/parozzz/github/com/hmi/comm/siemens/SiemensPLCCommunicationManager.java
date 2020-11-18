package parozzz.github.com.hmi.comm.siemens;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.comm.DeviceCommunicationManager;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.concurrent.SettableConcurrentObject;

import java.io.IOException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class SiemensPLCCommunicationManager extends DeviceCommunicationManager<SiemensPLCThread>
{
    @FXML private TextField address1TextField;
    @FXML private TextField address2TextField;
    @FXML private TextField address3TextField;
    @FXML private TextField address4TextField;
    @FXML private TextField rackTextField;
    @FXML private TextField slotTextField;

    @FXML private Button connectButton;
    @FXML private Label modelLabel;

    private final StackPane mainStackPane;

    private final SettableConcurrentObject<String> modelObject;
    private boolean queryModelNumber = false;

    public SiemensPLCCommunicationManager(SiemensPLCThread plcThread) throws IOException
    {
        super("SiemensPLCCommunicationManager", plcThread);

        mainStackPane = (StackPane) FXUtil.loadFXML("siemensCommPane.fxml", this);

        this.modelObject = new SettableConcurrentObject<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        serializableDataSet.addString("Address1", address1TextField.textProperty())
                .addString("Address2", address2TextField.textProperty())
                .addString("Address3", address3TextField.textProperty())
                .addString("Address4", address4TextField.textProperty())
                .addString("Slot", slotTextField.textProperty())
                .addString("Rack", rackTextField.textProperty());

        Stream.of(address1TextField, address2TextField, address3TextField, address4TextField).forEach(textField ->
                textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(3))
        );

        super.setSkipOnNextForDot(address1TextField, address2TextField);
        super.setSkipOnNextForDot(address2TextField, address3TextField);
        super.setSkipOnNextForDot(address3TextField, address4TextField);

        Stream.of(rackTextField, slotTextField).forEach(textField ->
                textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(2)))
        ;

        connectButton.setOnAction(event -> this.setPLCAddress());
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        address1TextField.setText("192");
        address2TextField.setText("168");
        address3TextField.setText("1");
        address4TextField.setText("5");

        rackTextField.setText("0");
        slotTextField.setText("0");
    }

    @Override
    public void loop()
    {
        super.loop();

        if(!commThread.isActive())
        {
            return;
        }

        if (queryModelNumber && commThread.isConnected())
        {
            commThread.queryModel(modelObject);
            queryModelNumber = false;
        }

        if (modelObject.isObjectSet())
        {
            modelLabel.setText(modelObject.getObject());
            modelObject.reset();
        }
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        this.setPLCAddress();
    }

    @Override
    public Parent getParent()
    {
        return mainStackPane;
    }

    private void setPLCAddress()
    {
        try
        {
            var ipAddress = address1TextField.getText() + "." + address2TextField.getText() +
                    "." + address3TextField.getText() + "." + address4TextField.getText();
            var slot = Integer.parseInt(slotTextField.getText());
            var rack = Integer.parseInt(rackTextField.getText());

            commThread.setConnectionParameters(ipAddress, slot, rack);

            queryModelNumber = true;
        } catch (NumberFormatException exception)
        {
            Logger.getLogger(SiemensPLCCommunicationManager.class.getSimpleName())
                    .log(Level.WARNING, "Something went wrong while setting the PLC Address", exception);
        }
    }
}
