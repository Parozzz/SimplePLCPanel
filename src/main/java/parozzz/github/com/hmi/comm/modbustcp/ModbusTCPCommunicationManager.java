package parozzz.github.com.hmi.comm.modbustcp;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import parozzz.github.com.hmi.comm.DeviceCommunicationManager;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.stream.Stream;

public final class ModbusTCPCommunicationManager extends DeviceCommunicationManager<ModbusTCPThread>
{
    @FXML private TextField address1TextField;
    @FXML private TextField address2TextField;
    @FXML private TextField address3TextField;
    @FXML private TextField address4TextField;

    @FXML private TextField portTextField;

    @FXML private Button connectButton;

    private final AnchorPane mainAnchorPane;

    public ModbusTCPCommunicationManager(ModbusTCPThread thread) throws IOException
    {
        super("ModbusTCPCommunicationManager", thread);

        this.mainAnchorPane = (AnchorPane) FXUtil.loadFXML("modbusTCPCommPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        Stream.of(address1TextField, address2TextField, address3TextField, address4TextField).forEach(textField ->
                textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(3))
        );

        portTextField.setTextFormatter(FXTextFormatterUtil.positiveInteger(5));

        connectButton.setOnAction(actionEvent -> this.setConnectionParameters());

        serializableDataSet.addString("Address1", address1TextField.textProperty())
                .addString("Address2", address2TextField.textProperty())
                .addString("Address3", address3TextField.textProperty())
                .addString("Address4", address4TextField.textProperty())
                .addString("Port", portTextField.textProperty());
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        address1TextField.setText("192");
        address2TextField.setText("168");
        address3TextField.setText("1");
        address4TextField.setText("5");

        portTextField.setText("502"); //Default Modbus TCP port
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        this.setConnectionParameters(); //This will allow comm to start automagically!
    }

    @Override
    public Parent getParent()
    {
        return mainAnchorPane;
    }

    private void setConnectionParameters()
    {
        var ipAddress = address1TextField.getText() + "." + address2TextField.getText() +
                "." + address3TextField.getText() + "." + address4TextField.getText();
        var port = Integer.parseInt(portTextField.getText());

        commThread.setConnectionParameters(ipAddress, port);
    }
}