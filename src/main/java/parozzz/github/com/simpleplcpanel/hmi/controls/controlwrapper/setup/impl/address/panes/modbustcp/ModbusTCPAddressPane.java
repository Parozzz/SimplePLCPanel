package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.panes.modbustcp;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data.AddressDataType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data.ModbusTCPDataPropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPDataLength;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPFunctionCode;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.AddressSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.AddressStringParser;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.panes.AddressPane;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.util.stream.Stream;

public class ModbusTCPAddressPane extends AddressPane
{
    @FXML ChoiceBox<ModbusTCPFunctionCode> typeChoiceBox;
    @FXML private Label lengthLabel;
    @FXML ChoiceBox<ModbusTCPDataLength> lengthChoiceBox;
    @FXML TextField addressTextField;
    @FXML private Label bitNumberLabel;
    @FXML TextField bitNumberTextField;

    @FXML private Label signedLabel;
    @FXML CheckBox signedCheckBox;

    private final boolean isReadAddress;
    private final VBox vBox;
    private final ModbusTCPAddressStringParser stringParser;

    public ModbusTCPAddressPane(AddressSetupPane<?> addressSetupPane, boolean isReadAddress) throws IOException
    {
        super(AddressDataType.MODBUS_TCP);

        this.isReadAddress = isReadAddress;
        this.vBox = (VBox) FXUtil.loadFXML("setup/address/modbusTCPAddressDataPane.fxml", this);
        this.stringParser = new ModbusTCPAddressStringParser(addressSetupPane, this);
    }

    @Override
    public void setup()
    {
        super.setup();

        stringParser.init();

        lengthChoiceBox.setConverter(new EnumStringConverter<>(ModbusTCPDataLength.class).setCapitalize());
        lengthChoiceBox.getItems().addAll(ModbusTCPDataLength.values());
        lengthChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var functionCode = typeChoiceBox.getValue();
            if(newValue == null || functionCode == null)
            {
                return;
            }

            var isBit = newValue == ModbusTCPDataLength.BIT;
            bitNumberLabel.setVisible(isBit);
            bitNumberTextField.setVisible(isBit);

            switch (functionCode)
            {
                case HOLDING_REGISTER:
                case INPUT_REGISTER:
                    signedLabel.setVisible(!isBit);
                    signedCheckBox.setVisible(!isBit); //Signed booleans. A man can dream sometime.
                    break;
            }
        });

        typeChoiceBox.setConverter(new EnumStringConverter<>(ModbusTCPFunctionCode.class).setCapitalize());
        Stream.of(ModbusTCPFunctionCode.values())
                .filter(functionCode -> isReadAddress || !functionCode.isReadOnly())
                .forEach(typeChoiceBox.getItems()::add);
        typeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                return;
            }

            switch (newValue)
            {
                case COIL:
                case DISCRETE_INPUT:
                    this.setBitType();
                    break;
                case INPUT_REGISTER:
                case HOLDING_REGISTER:
                    this.setWordType();
                    break;
            }
        });

        addressTextField.setTextFormatter(FXTextFormatterUtil.positiveInteger(4));
        bitNumberTextField.setTextFormatter(
                FXTextFormatterUtil.integerBuilder()
                        .max(15)
                        .min(0)
                        .getTextFormatter()
        );

        signedLabel.setVisible(isReadAddress);  //When i write values, the system does not care about signed / unsigned
    }

    @Override
    public void setDefault()
    {
        typeChoiceBox.setValue(ModbusTCPDataPropertyHolder.FUNCTION_CODE.getDefaultValue());
        addressTextField.setText("" + ModbusTCPDataPropertyHolder.OFFSET.getDefaultValue());
        lengthChoiceBox.setValue(ModbusTCPDataPropertyHolder.DATA_LENGTH.getDefaultValue());
        signedCheckBox.setSelected(ModbusTCPDataPropertyHolder.SIGNED.getDefaultValue());
    }

    @Override
    public AddressStringParser<?> getAddressStringParser()
    {
        return stringParser;
    }

    @Override
    public Parent getMainParent()
    {
        return vBox;
    }

    @Override
    public void parseAttributeChangerList(
            SetupPaneAttributeChangerList<? extends AddressAttribute> attributeChangerList)
    {
        attributeChangerList.create(typeChoiceBox.valueProperty(), ModbusTCPDataPropertyHolder.FUNCTION_CODE)
                .createStringToNumber(addressTextField.textProperty(), ModbusTCPDataPropertyHolder.OFFSET, Util::parseIntOrZero)
                .create(lengthChoiceBox.valueProperty(), ModbusTCPDataPropertyHolder.DATA_LENGTH)
                .createStringToNumber(bitNumberTextField.textProperty(), ModbusTCPDataPropertyHolder.BIT_OFFSET, Util::parseIntOrZero)
                .create(signedCheckBox.selectedProperty(), ModbusTCPDataPropertyHolder.SIGNED);
    }

    @Override
    public void setAsState()
    {

    }

    @Override
    public void setAsGlobal()
    {

    }

    public boolean isReadAddress()
    {
        return isReadAddress;
    }

    private void setBitType() //Bits like coils only use the offset and are boolean only
    {
        lengthChoiceBox.setValue(ModbusTCPDataLength.BIT);

        lengthLabel.setVisible(false);
        lengthChoiceBox.setVisible(false);

        bitNumberLabel.setVisible(false);
        bitNumberTextField.setVisible(false);

        signedLabel.setVisible(false);
        signedCheckBox.setVisible(false); //Signed booleans. A man can dream sometime.
    }

    private void setWordType()
    {
        lengthChoiceBox.setValue(ModbusTCPDataLength.WORD);

        lengthLabel.setVisible(true);
        lengthChoiceBox.setVisible(true);
    }
}
