package parozzz.github.com.simpleplcpanel.hmi.comm.siemens;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommUtils;
import parozzz.github.com.simpleplcpanel.hmi.comm.NetworkCommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.Util;
import parozzz.github.com.simpleplcpanel.util.concurrent.SettableConcurrentObject;

import java.io.IOException;
import java.util.stream.Stream;

public final class SiemensS7CommunicationManager extends NetworkCommunicationManager<SiemensS7Thread>
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
    private TextField rackTextField;
    @FXML
    private TextField slotTextField;

    @FXML
    private Label modelLabel;

    private final StackPane mainStackPane;

    private final SettableConcurrentObject<String> modelObject;
    private boolean queryModelNumber = false;

    public SiemensS7CommunicationManager(SiemensS7Thread plcThread) throws IOException
    {
        super("SiemensPLCCommunicationManager", plcThread);

        mainStackPane = (StackPane) FXUtil.loadFXML("siemensCommPane.fxml", this);

        this.modelObject = new SettableConcurrentObject<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        serializableDataSet.addString("Address1", address1TextField.textProperty(), CommUtils.DEFAULT_IP1_STRING)
                .addString("Address2", address2TextField.textProperty(), CommUtils.DEFAULT_IP2_STRING)
                .addString("Address3", address3TextField.textProperty(), CommUtils.DEFAULT_IP3_STRING)
                .addString("Address4", address4TextField.textProperty(), CommUtils.DEFAULT_IP4_STRING)
                .addString("Slot", slotTextField.textProperty(), CommUtils.DEFAULT_SIEMENS_SLOT_STRING)
                .addString("Rack", rackTextField.textProperty(), CommUtils.DEFAULT_SIEMENS_RACK_STRING);

        Stream.of(address1TextField, address2TextField, address3TextField, address4TextField).forEach(textField ->
                textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(3))
        );

        super.registerSwitchToNextTextFieldOnDecimalPress(address1TextField, address2TextField);
        super.registerSwitchToNextTextFieldOnDecimalPress(address2TextField, address3TextField);
        super.registerSwitchToNextTextFieldOnDecimalPress(address3TextField, address4TextField);

        Stream.of(rackTextField, slotTextField).forEach(textField ->
                textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(2))
        );
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        address1TextField.setText(CommUtils.DEFAULT_IP1_STRING);
        address2TextField.setText(CommUtils.DEFAULT_IP2_STRING);
        address3TextField.setText(CommUtils.DEFAULT_IP3_STRING);
        address4TextField.setText(CommUtils.DEFAULT_IP4_STRING);

        rackTextField.setText(CommUtils.DEFAULT_SIEMENS_RACK_STRING);
        slotTextField.setText(CommUtils.DEFAULT_SIEMENS_SLOT_STRING);
    }

    @Override
    public void loop()
    {
        super.loop();

        if(!commThread.isActive())
        {
            return;
        }

        if(queryModelNumber && commThread.isConnected())
        {
            commThread.queryModel(modelObject);
            queryModelNumber = false;
        }

        if(modelObject.isObjectSet())
        {
            modelLabel.setText(modelObject.getObject());
            modelObject.reset();
        }
    }

    @Override
    public Parent getParent()
    {
        return mainStackPane;
    }

    @Override
    public void parseAndUpdateCommunicationParams()
    {
        try
        {
            var ipAddress = CommUtils.validateAndCreateIpAddress(address1TextField,
                    address2TextField, address3TextField, address4TextField);
            var slot = Util.parseInt(slotTextField.getText(), -1);
            var rack = Util.parseInt(rackTextField.getText(), -1);

            if(ipAddress == null || slot < 0 || rack < 0)
            {
                MainLogger.getInstance().info("Communication parameters for Siemens S7 PLC are invalid", this);
                return;
            }

            commThread.setConnectionParameters(new SiemensS7ConnectionParams(ipAddress, slot, rack));
            queryModelNumber = true;
        }
        catch(NumberFormatException exception)
        {
            MainLogger.getInstance().warning("Something went wrong while setting communication parameters for Siemens S7 PLC",
                    exception, this);
        }
    }
}
