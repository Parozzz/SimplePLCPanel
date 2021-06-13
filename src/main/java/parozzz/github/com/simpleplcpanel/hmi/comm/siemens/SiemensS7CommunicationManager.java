package parozzz.github.com.simpleplcpanel.hmi.comm.siemens;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.converter.NumberStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommUtils;
import parozzz.github.com.simpleplcpanel.hmi.comm.NetworkCommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;
import java.util.stream.Stream;

public final class SiemensS7CommunicationManager extends NetworkCommunicationManager<SiemensS7ConnectionParams>
{
    @FXML private TextField rackTextField;
    @FXML private TextField slotTextField;
    @FXML private Label modelLabel;

    private final StackPane mainStackPane;

    private final IntegerProperty rack;
    private final IntegerProperty slot;
    private final StringProperty model;

    //private final SettableConcurrentObject<String> modelObject;
    //private boolean queryModelNumber = false;

    public SiemensS7CommunicationManager() throws IOException
    {
        super("SiemensPLCCommunicationManager");

        mainStackPane = (StackPane) FXUtil.loadFXML("siemensCommPane.fxml", this);

        this.rack = new SimpleIntegerProperty();
        this.slot = new SimpleIntegerProperty();
        this.model = new SimpleStringProperty();

        //this.modelObject = new SettableConcurrentObject<>();
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        serializableDataSet.addInt("Rack", rack, 0)
                .addInt("Slot", slot, 2);

        rackTextField.textProperty().bindBidirectional(rack, new NumberStringConverter());
        slotTextField.textProperty().bindBidirectional(slot, new NumberStringConverter());
        Stream.of(rackTextField, slotTextField).forEach(textField ->
                textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(2))
        );

        model.bindBidirectional(modelLabel.textProperty());
    }

    @Override
    public void onSetDefault()
    {
        super.onSetDefault();

        rackTextField.setText(CommUtils.DEFAULT_SIEMENS_RACK_STRING);
        slotTextField.setText(CommUtils.DEFAULT_SIEMENS_SLOT_STRING);
    }

    @Override
    public void onLoop()
    {
        super.onLoop();
/*
        if (!commThread.isActive())
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
        }*/
    }

    @Override
    public Parent getParent()
    {
        return mainStackPane;
    }

    @Override
    protected SiemensS7ConnectionParams createConnectionParams()
    {
        var ipAddress = super.getIpAddress();
        return ipAddress == null
                ? null
                : new SiemensS7ConnectionParams(ipAddress, rack.get(), slot.get());
    }

    public IntegerProperty rackProperty()
    {
        return rack;
    }

    public IntegerProperty slotProperty()
    {
        return slot;
    }

    public StringProperty modelProperty()
    {
        return model;
    }

    /*
    @Override
    public void parseAndUpdateCommunicationParams()
    {
        try
        {
            var ipAddress = CommUtils.validateAndCreateIpAddress(address1TextField,
                    address2TextField, address3TextField, address4TextField);
            var slot = Util.parseInt(slotTextField.getText(), -1);
            var rack = Util.parseInt(rackTextField.getText(), -1);

            if (ipAddress == null || slot < 0 || rack < 0)
            {
                MainLogger.getInstance().info("Communication parameters for Siemens S7 PLC are invalid", this);
                return;
            }

            commThread.setConnectionParameters(new SiemensS7ConnectionParams(ipAddress, slot, rack));
            queryModelNumber = true;
        } catch (NumberFormatException exception)
        {
            MainLogger.getInstance().warning("Something went wrong while setting communication parameters for Siemens S7 PLC",
                    exception, this);
        }
    }*/
}
