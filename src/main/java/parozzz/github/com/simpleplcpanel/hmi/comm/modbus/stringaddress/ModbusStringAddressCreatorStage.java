package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.ModbusDataLength;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.ModbusFunctionCode;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.util.stream.Stream;

public class ModbusStringAddressCreatorStage
        extends CommunicationStringAddressCreatorStage<ModbusStringAddressData>
{
    @FXML
    private ChoiceBox<ModbusFunctionCode> dataTypeChoiceBox;

    @FXML
    private Label lengthLabel;
    @FXML
    private ChoiceBox<ModbusDataLength> dataLengthChoiceBox;

    @FXML
    private TextField addressTextField;

    @FXML
    private Label bitNumberLabel;
    @FXML
    private TextField bitNumberTextField;

    @FXML
    private Label signedLabel;
    @FXML
    private CheckBox signedCheckBox;

    private boolean ignoreUpdate;

    public ModbusStringAddressCreatorStage() throws IOException
    {
        super(CommunicationType.MODBUS_TCP,"stringAddress/modbusTCPAddressStringCreator.fxml");
    }

    @Override
    public void setup()
    {
        super.setup();

        dataTypeChoiceBox.setConverter(new EnumStringConverter<>(ModbusFunctionCode.class).setCapitalize());
        dataTypeChoiceBox.getItems().addAll(ModbusFunctionCode.values());
        dataTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue == null)
            {
                return;
            }

            switch (newValue)
            {
                case COIL:
                case DISCRETE_INPUT:
                    this.convertPaneToBit();
                    break;
                case INPUT_REGISTER:
                case HOLDING_REGISTER:
                    this.convertPaneToWord();
                    break;
            }
        });

        dataLengthChoiceBox.setConverter(new EnumStringConverter<>(ModbusDataLength.class).setCapitalize());
        dataLengthChoiceBox.getItems().addAll(ModbusDataLength.values());
        dataLengthChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var functionCode = dataTypeChoiceBox.getValue();
            if (newValue == null || functionCode == null)
            {
                return;
            }

            var isBit = newValue == ModbusDataLength.BIT;
            bitNumberLabel.setVisible(isBit);
            bitNumberTextField.setVisible(isBit);

            signedLabel.setVisible(!isBit);
            signedCheckBox.setVisible(!isBit);
        });

        addressTextField.setTextFormatter(FXTextFormatterUtil.positiveInteger(4));
        bitNumberTextField.setTextFormatter(
                FXTextFormatterUtil.integerBuilder()
                        .max(15)
                        .min(0)
                        .getTextFormatter()
        );

        Stream.of(dataTypeChoiceBox.valueProperty(), dataLengthChoiceBox.valueProperty(),
                addressTextField.textProperty(), bitNumberTextField.textProperty(),
                signedCheckBox.selectedProperty())
                .forEach(property ->
                        property.addListener((observable, oldValue, newValue) ->
                                this.updateTextConvertedAddress()
                        )
                );
    }

    @Override
    public void setDefault()
    {
        super.setDefault();
        this.loadStringDataToActualValues(new ModbusStringAddressData());
    }

    @Override
    public boolean loadStringDataToActualValues(String stringData)
    {
        var stringAddressData = ModbusStringAddressData.parseStringData(stringData);
        if (stringAddressData == null || !stringAddressData.validate())
        {
            this.setDefault();
            return false;
        }

        return this.loadStringDataToActualValues(stringAddressData);
    }

    @Override
    public boolean loadStringDataToActualValues(ModbusStringAddressData stringAddressData)
    {
        if(stringAddressData == null)
        {
            return false;
        }

        ignoreUpdate = true;
        dataTypeChoiceBox.setValue(stringAddressData.getFunctionCode());
        dataLengthChoiceBox.setValue(stringAddressData.getDataLength());
        addressTextField.setText("" + stringAddressData.getOffset());
        bitNumberTextField.setText("" + stringAddressData.getBitOffset());
        signedCheckBox.setSelected(stringAddressData.isSigned());
        ignoreUpdate = false;

        this.updateTextConvertedAddress();
        return true;
    }

    @Override
    public ModbusStringAddressData createDataFromActualValues()
    {
        var functionCode = dataTypeChoiceBox.getValue();
        var dataLength = dataLengthChoiceBox.getValue();
        if (functionCode == null || dataLength == null)
        {
            return null;
        }

        var bitNumber = Util.parseInt(bitNumberTextField.getText(), 0);
        var address = Util.parseInt(addressTextField.getText(), 1);
        var signed = signedCheckBox.isSelected();

        return new ModbusStringAddressData(functionCode, dataLength, address, bitNumber, signed);
    }

    @Override
    public void updateTextConvertedAddress()
    {
        if (ignoreUpdate)
        {
            return;
        }

        var data = this.createDataFromActualValues();
        if(data != null)
        {
            convertedAddressTextField.setText(data.getStringData());
        }
    }

    private void convertPaneToBit() //Bits like coils only use the offset and are boolean only
    {
        dataLengthChoiceBox.setValue(ModbusDataLength.BIT);

        lengthLabel.setVisible(false);
        dataLengthChoiceBox.setVisible(false);

        bitNumberLabel.setVisible(false); //Each value is already a bit. They are counter as word from 1 to 9999
        bitNumberTextField.setVisible(false);

        signedLabel.setVisible(false);
        signedCheckBox.setVisible(false); //Signed booleans. A man can dream sometime.
    }

    private void convertPaneToWord()
    {
        dataLengthChoiceBox.setValue(ModbusDataLength.WORD);

        lengthLabel.setVisible(true);
        dataLengthChoiceBox.setVisible(true);
    }
}
