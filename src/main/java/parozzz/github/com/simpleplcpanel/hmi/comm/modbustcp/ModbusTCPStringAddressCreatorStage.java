package parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data.ModbusTCPDataPropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data.SiemensDataPropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.panes.modbustcp.ModbusTCPAddressStringParser;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ModbusTCPStringAddressCreatorStage
        extends CommunicationStringAddressCreatorStage<ModbusTCPAddressStringParser.Data>
{
    @FXML
    private ChoiceBox<ModbusTCPFunctionCode> dataTypeChoiceBox;

    @FXML
    private Label lengthLabel;
    @FXML
    private ChoiceBox<ModbusTCPDataLength> dataLengthChoiceBox;

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


    public ModbusTCPStringAddressCreatorStage() throws IOException
    {
        super("stringAddress/modbusTCPAddressStringCreator.fxml");
    }

    public ModbusTCPStringAddressCreatorStage setReadOnlyAddress(boolean readOnly)
    {
        readOnlyAddressCheckBox.setSelected(readOnly);
        return this;
    }

    public ModbusTCPStringAddressCreatorStage disableChangesOnReadOnly(boolean disable)
    {
        readOnlyAddressCheckBox.setDisable(disable);
        return this;
    }

    @Override
    public void setup()
    {
        super.setup();

        dataTypeChoiceBox.setConverter(new EnumStringConverter<>(ModbusTCPFunctionCode.class).setCapitalize());
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

        dataLengthChoiceBox.setConverter(new EnumStringConverter<>(ModbusTCPDataLength.class).setCapitalize());
        dataLengthChoiceBox.getItems().addAll(ModbusTCPDataLength.values());
        dataLengthChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var functionCode = dataTypeChoiceBox.getValue();
            if (newValue == null || functionCode == null)
            {
                return;
            }

            var isBit = newValue == ModbusTCPDataLength.BIT;
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

        dataTypeChoiceBox.getSelectionModel().select(ModbusTCPDataPropertyHolder.FUNCTION_CODE.getDefaultValue());
        dataLengthChoiceBox.getSelectionModel().select(ModbusTCPDataPropertyHolder.DATA_LENGTH.getDefaultValue());
        addressTextField.setText("" + ModbusTCPDataPropertyHolder.OFFSET.getDefaultValue());
        bitNumberTextField.setText("" + ModbusTCPDataPropertyHolder.BIT_OFFSET.getDefaultValue());
        signedCheckBox.setSelected(ModbusTCPDataPropertyHolder.SIGNED.getDefaultValue());
    }

    @Override
    public void showAsStandalone(HMIStage<?> owner)
    {
        this.disableChangesOnReadOnly(false);

        super.showAsStandalone(owner);
    }

    @Override
    public boolean loadStringDataToActualValues(String stringData)
    {
        var data = ModbusTCPAddressStringParser.parseDataFromString(stringData);
        if (data == null)
        {
            return false;
        }

        ignoreUpdate = true;
        dataTypeChoiceBox.setValue(data.getFunctionCode());
        dataLengthChoiceBox.setValue(data.getDataLength());
        addressTextField.setText("" + data.getAddress());
        bitNumberLabel.setText("" + data.getBitNumber());
        signedCheckBox.setSelected(data.isSigned());
        ignoreUpdate = false;

        this.updateTextConvertedAddress();
        return true;
    }

    @Override
    public ModbusTCPAddressStringParser.Data createDataFromActualValues()
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
        var read = readOnlyAddressCheckBox.isSelected();

        return new ModbusTCPAddressStringParser.Data(functionCode, dataLength, address, bitNumber, signed, read);
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
            var textAddress = ModbusTCPAddressStringParser.createStringFromData(data);
            convertedAddressTextField.setText(textAddress == null ? "" : textAddress);
        }
    }

    private void convertPaneToBit() //Bits like coils only use the offset and are boolean only
    {
        dataLengthChoiceBox.setValue(ModbusTCPDataLength.BIT);

        lengthLabel.setVisible(false);
        dataLengthChoiceBox.setVisible(false);

        bitNumberLabel.setVisible(false); //Each value is already a bit. They are counter as word from 1 to 9999
        bitNumberTextField.setVisible(false);

        signedLabel.setVisible(false);
        signedCheckBox.setVisible(false); //Signed booleans. A man can dream sometime.
    }

    private void convertPaneToWord()
    {
        dataLengthChoiceBox.setValue(ModbusTCPDataLength.WORD);

        lengthLabel.setVisible(true);
        dataLengthChoiceBox.setVisible(true);
    }

    private void updateReadOnlyProperty()
    {
        var readOnly = readOnlyAddressCheckBox.isSelected();

        var dataTypeItems = dataTypeChoiceBox.getItems();
        dataTypeItems.clear();
        Stream.of(ModbusTCPFunctionCode.values())
                .filter(functionCode -> readOnly || !functionCode.isReadOnly())
                .forEach(dataTypeItems::add);
        dataTypeChoiceBox.getSelectionModel().selectFirst();
    }
}
