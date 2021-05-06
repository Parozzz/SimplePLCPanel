package parozzz.github.com.simpleplcpanel.hmi.comm.siemens;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data.SiemensDataPropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.panes.siemens.SiemensAddressStringParser;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SiemensS7StringAddressCreator
        extends CommunicationStringAddressCreatorStage<SiemensAddressStringParser.Data>
{
    @FXML private Label dbNumberLabel;
    @FXML private Label bitOffsetLabel;
    @FXML private Label stringLengthLabel;

    @FXML private ChoiceBox<SiemensS7AreaType> memoryAreaChoiceBox;
    @FXML private ChoiceBox<SiemensS7ReadableData<?>> dataTypeChoiceBox;

    @FXML private TextField dbTextField;
    @FXML private TextField offsetTextField;
    @FXML private TextField bitOffsetTextField;
    @FXML private TextField stringLengthTextField;

    private boolean ignoreUpdate;

    public SiemensS7StringAddressCreator() throws IOException
    {
        super("stringAddress/siemensS7AddressStringCreator.fxml");
    }

    @Override
    public void setup()
    {
        super.setup();

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

        Stream.of(memoryAreaChoiceBox.valueProperty(), dataTypeChoiceBox.valueProperty(),
                dbTextField.textProperty(), offsetTextField.textProperty(),
                bitOffsetTextField.textProperty(), stringLengthTextField.textProperty())
                //This seems to be necessary. The compiler gives me an error otherwise. Maybe it can't mixed all the properties above?
                .map(Property.class::cast)
                .forEach(property ->
                        property.addListener((observableValue, oldValue, newValue) -> {
                            this.updateTextConvertedAddress();
                        })
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

    public void updateTextConvertedAddress()
    {
        if (ignoreUpdate)
        {
            return;
        }

        var data = this.createDataFromActualValues();
        if (data != null)
        {
            var stringData = SiemensAddressStringParser.createStringOfData(data);
            convertedAddressTextField.setText(stringData);
        }
    }

    @Override
    public boolean loadStringDataToActualValues(String stringData)
    {
        var data = SiemensAddressStringParser.parseDataFromString(stringData);
        if (data == null)
        {
            return false;
        }

        ignoreUpdate = true;
        dataTypeChoiceBox.setValue(data.getReadableData());
        memoryAreaChoiceBox.setValue(data.getAreaType());
        dbTextField.setText("" + data.getDbNumber());
        offsetTextField.setText("" + data.getByteOffset());
        bitOffsetTextField.setText("" + data.getBitOffset());
        stringLengthTextField.setText("" + data.getStringLength());
        ignoreUpdate = false;

        this.updateTextConvertedAddress();
        return true;
    }

    @Override
    public SiemensAddressStringParser.Data createDataFromActualValues()
    {
        var readableData = dataTypeChoiceBox.getValue();
        var areaType = memoryAreaChoiceBox.getValue();
        if (readableData == null || areaType == null)
        {
            return null;
        }

        var dbNumber = Util.parseInt(dbTextField.getText(), 0);
        var byteOffset = Util.parseInt(offsetTextField.getText(), 0);
        var bitOffset = Util.parseInt(bitOffsetTextField.getText(), 0);
        var stringLength = Util.parseInt(stringLengthTextField.getText(), 1);

        return new SiemensAddressStringParser.Data(readableData, areaType, dbNumber, byteOffset, bitOffset, stringLength);
    }
}
