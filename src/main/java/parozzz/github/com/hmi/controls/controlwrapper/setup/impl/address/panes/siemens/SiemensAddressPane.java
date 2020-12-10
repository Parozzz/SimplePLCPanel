package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.siemens;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.data.AddressDataType;
import parozzz.github.com.hmi.attribute.impl.address.data.SiemensDataPropertyHolder;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressStringParser;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.AddressPane;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;

public final class SiemensAddressPane extends AddressPane
{
    //Labels
    @FXML private Label dbNumberLabel;
    @FXML private Label bitOffsetLabel;
    @FXML private Label stringLengthLabel;

    @FXML ChoiceBox<SiemensS7AreaType> memoryAreaChoiceBox;
    @FXML ChoiceBox<SiemensS7ReadableData<?>> dataTypeChoiceBox;

    @FXML TextField dbTextField;
    @FXML TextField offsetTextField;
    @FXML TextField bitOffsetTextField;
    @FXML TextField stringLengthTextField;

    private final VBox vBox;
    private final SiemensAddressStringParser addressStringParser;

    public SiemensAddressPane(AddressSetupPane<? extends AddressAttribute> addressSetupPane) throws IOException
    {
        super(AddressDataType.SIEMENS);

        this.vBox = (VBox) FXUtil.loadFXML("setup/address/siemensAddressDataPane.fxml", this);
        this.addressStringParser = new SiemensAddressStringParser(addressSetupPane, this);
    }

    @Override
    public void setup()
    {
        super.setup();

        addressStringParser.init();

        memoryAreaChoiceBox.setConverter(new EnumStringConverter<>(SiemensS7AreaType.class));
        memoryAreaChoiceBox.getItems().addAll(SiemensS7AreaType.values());
        memoryAreaChoiceBox.valueProperty().addListener((observableValue, oldState, newState) ->
        {
            dbTextField.setVisible(newState == SiemensS7AreaType.DB);
            dbNumberLabel.setVisible(newState == SiemensS7AreaType.DB);
        });

        dataTypeChoiceBox.setConverter(FXUtil.toStringOnlyConverter(SiemensS7ReadableData::getName));
        dataTypeChoiceBox.getItems().addAll(SiemensS7DataStorage.BIT_ZERO,
                SiemensS7DataStorage.BYTE, SiemensS7DataStorage.WORD,
                SiemensS7DataStorage.SHORT, SiemensS7DataStorage.DWORD, SiemensS7DataStorage.DINT,
                SiemensS7DataStorage.FLOAT, SiemensS7DataStorage.DOUBLE,
                SiemensS7DataStorage.EMPTY_STRING);
        dataTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var isBit = newValue == SiemensS7DataStorage.BIT_ZERO;
            bitOffsetLabel.setVisible(isBit);
            bitOffsetTextField.setVisible(isBit);

            var isString = newValue == SiemensS7DataStorage.EMPTY_STRING;
            stringLengthLabel.setVisible(isString);
            stringLengthTextField.setVisible(isString);
        });

        dbTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));

        offsetTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        bitOffsetTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(1));
        stringLengthTextField.setTextFormatter(
                FXTextFormatterUtil.integerBuilder()
                        .min(1)
                        .max(250)
                        .getTextFormatter()
        );
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        memoryAreaChoiceBox.getSelectionModel().select(SiemensDataPropertyHolder.S7_AREA_TYPE.getDefaultValue());
        dataTypeChoiceBox.getSelectionModel().select(SiemensDataPropertyHolder.S7_DATA.getDefaultValue());

        dbTextField.setText("" + SiemensDataPropertyHolder.DB_NUMBER.getDefaultValue());

        offsetTextField.setText("" + SiemensDataPropertyHolder.BYTE_OFFSET.getDefaultValue());
        bitOffsetTextField.setText("" + SiemensDataPropertyHolder.BIT_OFFSET.getDefaultValue());

        stringLengthTextField.setText("" + SiemensDataPropertyHolder.STRING_LENGTH.getDefaultValue());
    }

    @Override
    public AddressStringParser<SiemensAddressPane> getAddressStringParser()
    {
        return addressStringParser;
    }

    @Override
    public Parent getMainParent()
    {
        return vBox;
    }

    @Override
    public void parseAttributeChangerList(SetupPaneAttributeChangerList<? extends AddressAttribute> attributeChangerList)
    {
        attributeChangerList.create(memoryAreaChoiceBox.valueProperty(), SiemensDataPropertyHolder.S7_AREA_TYPE)
                .create(dataTypeChoiceBox.valueProperty(), SiemensDataPropertyHolder.S7_DATA)
                .createStringToNumber(dbTextField.textProperty(), SiemensDataPropertyHolder.DB_NUMBER, Util::parseIntOrZero)
                .createStringToNumber(offsetTextField.textProperty(), SiemensDataPropertyHolder.BYTE_OFFSET, Util::parseIntOrZero)
                .createStringToNumber(bitOffsetTextField.textProperty(), SiemensDataPropertyHolder.BIT_OFFSET, Util::parseIntOrZero)
                .createStringToNumber(stringLengthTextField.textProperty(), SiemensDataPropertyHolder.STRING_LENGTH, Util::parseIntOrZero);
    }

    @Override
    public void setAsState()
    {

    }

    @Override
    public void setAsGlobal()
    {

    }
}
