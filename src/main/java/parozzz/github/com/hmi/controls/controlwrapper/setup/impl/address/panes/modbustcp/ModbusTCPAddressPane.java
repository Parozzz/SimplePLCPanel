package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.modbustcp;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import parozzz.github.com.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.data.AddressDataType;
import parozzz.github.com.hmi.attribute.impl.address.data.ModbusTCPDataPropertyHolder;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPDataLength;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPFunctionCode;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressStringParser;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.AddressPane;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;
import java.util.stream.Stream;

public class ModbusTCPAddressPane extends AddressPane
{
    @FXML ChoiceBox<ModbusTCPFunctionCode> typeChoiceBox;
    @FXML TextField offsetTextField;
    @FXML ChoiceBox<ModbusTCPDataLength> lengthChoiceBox;
    @FXML CheckBox signedCheckBox;
    @FXML Label bitOffsetLabel;
    @FXML Spinner<Integer> bitOffsetSpinner;

    private final boolean readOnly;

    private final AnchorPane mainAnchorPane;
    private final ModbusTCPAddressStringParser stringParser;

    public ModbusTCPAddressPane(boolean readOnly) throws IOException
    {
        super("ModbusTCPAddressPane", AddressDataType.MODBUS_TCP);

        this.readOnly = readOnly;

        this.mainAnchorPane = (AnchorPane) FXUtil.loadFXML("setup/address/modbusTCPAddressPane.fxml", this);
        this.stringParser = new ModbusTCPAddressStringParser(this);
    }

    @Override
    public void setup()
    {
        super.setup();

        typeChoiceBox.setConverter(new EnumStringConverter<>(ModbusTCPFunctionCode.class).setCapitalize());
        Stream.of(ModbusTCPFunctionCode.values())
                .filter(functionCode -> readOnly || !functionCode.isReadOnly())
                .forEach(typeChoiceBox.getItems()::add);

        offsetTextField.setTextFormatter(FXTextFormatterUtil.positiveInteger(5));

        lengthChoiceBox.setConverter(new EnumStringConverter<>(ModbusTCPDataLength.class).setCapitalize());
        lengthChoiceBox.getItems().addAll(ModbusTCPDataLength.values());
        lengthChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var isBit = newValue == ModbusTCPDataLength.BIT;
            bitOffsetLabel.setVisible(isBit);
            bitOffsetSpinner.setVisible(isBit);
        });

        signedCheckBox.setVisible(readOnly); //When i write values, the system does not care about signed / unsigned

        bitOffsetSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 15));
    }

    @Override
    public void setDefault()
    {
        typeChoiceBox.setValue(ModbusTCPDataPropertyHolder.FUNCTION_CODE.getDefaultValue());
        offsetTextField.setText("" + ModbusTCPDataPropertyHolder.OFFSET.getDefaultValue());
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
        return mainAnchorPane;
    }

    @Override
    public void parseAttributeChangerList(SetupPaneAttributeChangerList<? extends AddressAttribute> attributeChangerList)
    {
        attributeChangerList.create(typeChoiceBox.valueProperty(), ModbusTCPDataPropertyHolder.FUNCTION_CODE)
                .createStringToNumber(offsetTextField.textProperty(), ModbusTCPDataPropertyHolder.OFFSET, Util::parseIntOrZero)
                .create(lengthChoiceBox.valueProperty(), ModbusTCPDataPropertyHolder.DATA_LENGTH)
                .create(signedCheckBox.selectedProperty(), ModbusTCPDataPropertyHolder.SIGNED)
                .createStringToNumber(bitOffsetSpinner.getEditor().textProperty(), ModbusTCPDataPropertyHolder.BIT_OFFSET, Util::parseIntOrZero);
    }
}