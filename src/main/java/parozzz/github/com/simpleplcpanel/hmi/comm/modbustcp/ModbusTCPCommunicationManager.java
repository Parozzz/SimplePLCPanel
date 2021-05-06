package parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommUtils;
import parozzz.github.com.simpleplcpanel.hmi.comm.NetworkCommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.util.stream.Stream;

public final class ModbusTCPCommunicationManager extends NetworkCommunicationManager<ModbusTCPThread>
{
    @FXML
    private TextField address1TextField;
    @FXML
    private TextField address2TextField;
    @FXML
    private TextField address3TextField;
    @FXML
    private TextField address4TextField;

    @FXML
    private TextField portTextField;

    private final StackPane mainStackPane;

    public ModbusTCPCommunicationManager(ModbusTCPThread thread) throws IOException
    {
        super("ModbusTCPCommunicationManager", thread);

        this.mainStackPane = (StackPane) FXUtil.loadFXML("modbusTCPCommPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        Stream.of(address1TextField, address2TextField, address3TextField, address4TextField).forEach(textField ->
                textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(3))
        );

        super.registerSwitchToNextTextFieldOnDecimalPress(address1TextField, address2TextField);
        super.registerSwitchToNextTextFieldOnDecimalPress(address2TextField, address3TextField);
        super.registerSwitchToNextTextFieldOnDecimalPress(address3TextField, address4TextField);

        portTextField.setTextFormatter(FXTextFormatterUtil.positiveInteger(5));

        serializableDataSet.addString("Address1", address1TextField.textProperty(), CommUtils.DEFAULT_IP1_STRING)
                .addString("Address2", address2TextField.textProperty(), CommUtils.DEFAULT_IP2_STRING)
                .addString("Address3", address3TextField.textProperty(), CommUtils.DEFAULT_IP3_STRING)
                .addString("Address4", address4TextField.textProperty(), CommUtils.DEFAULT_IP4_STRING)
                .addString("Port", portTextField.textProperty(), CommUtils.DEFAULT_MODBUSTCP_PORT_STRING);
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        address1TextField.setText(CommUtils.DEFAULT_IP1_STRING);
        address2TextField.setText(CommUtils.DEFAULT_IP2_STRING);
        address3TextField.setText(CommUtils.DEFAULT_IP3_STRING);
        address4TextField.setText(CommUtils.DEFAULT_IP4_STRING);

        portTextField.setText(CommUtils.DEFAULT_MODBUSTCP_PORT_STRING); //Default Modbus TCP port
    }

    @Override
    public Parent getParent()
    {
        return mainStackPane;
    }

    @Override
    public void parseAndUpdateCommunicationParams()
    {
        var ipAddress = CommUtils.validateAndCreateIpAddress(address1TextField,
                address2TextField, address3TextField, address4TextField);
        var port = Util.parseInt(portTextField.getText(), -1);

        if(ipAddress == null || port < 0)
        {
            MainLogger.getInstance().warning("Invalid connection parameters for ModbusTCP.", this);
            return;
        }

        try
        {
            commThread.setConnectionParameters(new ModbusTCPConnectionParams(ipAddress, port));
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().warning("An error occurred when setting connection parameters for ModbusTCP.", this);
        }
    }
}
