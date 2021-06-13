package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.tcp;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.converter.NumberStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommUtils;
import parozzz.github.com.simpleplcpanel.hmi.comm.NetworkCommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public final class ModbusTCPCommunicationManager extends NetworkCommunicationManager<ModbusTCPConnectionParams>
{
    @FXML private TextField portTextField;

    private final StackPane mainStackPane;

    private final IntegerProperty port;

    public ModbusTCPCommunicationManager() throws IOException
    {
        super("ModbusTCPCommunicationManager");

        this.mainStackPane = (StackPane) FXUtil.loadFXML("modbusTCPCommPane.fxml", this);

        this.port = new SimpleIntegerProperty();
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        serializableDataSet.addInt("Port", port, 502);

        portTextField.setTextFormatter(FXTextFormatterUtil.positiveInteger(5));
        portTextField.textProperty().bindBidirectional(port, new NumberStringConverter());
    }

    @Override
    public void onSetDefault()
    {
        super.onSetDefault();

        portTextField.setText(CommUtils.DEFAULT_MODBUSTCP_PORT_STRING); //Default Modbus TCP port
    }

    @Override
    public Parent getParent()
    {
        return mainStackPane;
    }

    @Override
    protected ModbusTCPConnectionParams createConnectionParams()
    {
        var ipAddress = super.getIpAddress();
        return ipAddress == null
                ? null
                : new ModbusTCPConnectionParams(ipAddress, port.get());
    }

    /*
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
    }*/
}
