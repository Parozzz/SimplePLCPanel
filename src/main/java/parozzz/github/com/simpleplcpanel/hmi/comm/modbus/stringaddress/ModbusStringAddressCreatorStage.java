package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.propertyholders.ModbusAttributePropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.ModbusDataLength;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.ModbusFunctionCode;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.util.function.Consumer;
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

    @FXML
    private CheckBox readOnlyAddressCheckBox;

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

            if (readOnlyAddressCheckBox.isSelected()) //Write values have no signed label whatsoever
            {
                switch (functionCode)
                {
                    case HOLDING_REGISTER:
                    case INPUT_REGISTER:
                        signedLabel.setVisible(!isBit);
                        signedCheckBox.setVisible(!isBit); //Signed booleans. A man can dream sometime.
                        break;
                }
            } else
            {
                signedLabel.setVisible(false);
                signedCheckBox.setVisible(false); //Signed booleans. A man can dream sometime.
            }
        });

        addressTextField.setTextFormatter(FXTextFormatterUtil.positiveInteger(4));
        bitNumberTextField.setTextFormatter(
                FXTextFormatterUtil.integerBuilder()
                        .max(15)
                        .min(0)
                        .getTextFormatter()
        );

        this.updateReadOnlyProperty();
        readOnlyAddressCheckBox.selectedProperty().addListener((observable, oldValue, readOnly) ->
                this.updateReadOnlyProperty()
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

        var defaultAddressData = new ModbusStringAddressData();
        dataTypeChoiceBox.getSelectionModel().select(defaultAddressData.getFunctionCode());
        dataLengthChoiceBox.getSelectionModel().select(defaultAddressData.getDataLength());
        addressTextField.setText("" + defaultAddressData.getOffset());
        bitNumberTextField.setText("" + defaultAddressData.getBitOffset());
        signedCheckBox.setSelected(defaultAddressData.isSigned());
    }

    @Override
    public void showAsStandalone()
    {
        this.disableChangesOnReadOnly(false);

        super.showAsStandalone();
    }

    @Override
    public void showAsInputTextAddress(AddressAttribute addressAttribute)
    {
        this.disableChangesOnReadOnly(true); //When is showed as a input, disable changes on this part. Is need to be handled to who want the address.

        super.showAsInputTextAddress(addressAttribute);
    }

    @Override
    public void setReadOnly(boolean readOnly)
    {
        readOnlyAddressCheckBox.setSelected(readOnly);
    }

    public void disableChangesOnReadOnly(boolean disable)
    {
        readOnlyAddressCheckBox.setDisable(disable);
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

        ignoreUpdate = true;
        dataTypeChoiceBox.setValue(stringAddressData.getFunctionCode());
        dataLengthChoiceBox.setValue(stringAddressData.getDataLength());
        addressTextField.setText("" + stringAddressData.getOffset());
        bitNumberLabel.setText("" + stringAddressData.getBitOffset());
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
        var readOnly = this.readOnlyAddressCheckBox.isSelected();

        return new ModbusStringAddressData(functionCode, dataLength, address, bitNumber, signed, readOnly);
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

    private void updateReadOnlyProperty()
    {
        var readOnly = readOnlyAddressCheckBox.isSelected();

        var dataTypeItems = dataTypeChoiceBox.getItems();
        dataTypeItems.clear();
        Stream.of(ModbusFunctionCode.values())
                .filter(functionCode -> readOnly || !functionCode.isReadOnly())
                .forEach(dataTypeItems::add);
        dataTypeChoiceBox.getSelectionModel().selectFirst();
    }
}
