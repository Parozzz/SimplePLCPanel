package parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.panes.modbustcp.ModbusTCPAddressStringParser;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ModbusTCPStringAddressCreatorStage extends HMIStage<VBox>
{
    private static ModbusTCPStringAddressCreatorStage addressCreatorStage;

    public static ModbusTCPStringAddressCreatorStage getInstance()
    {
        if(addressCreatorStage == null)
        {
            try
            {
                addressCreatorStage = new ModbusTCPStringAddressCreatorStage();
                addressCreatorStage.setup();
            }
            catch(IOException e)
            {
                throw new NullPointerException();
            }
        }

        return addressCreatorStage;
    }

    @FXML
    private ChoiceBox<ModbusTCPFunctionCode> dataTypeChoiceBox;

    @FXML
    private Label lengthLabel;
    @FXML
    private ChoiceBox<ModbusTCPDataLength> lengthChoiceBox;

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
    private TextField convertedAddressTextField;

    @FXML
    private StackPane acceptButtonStackPane;
    @FXML
    private Button acceptButton;

    @FXML
    private CheckBox readAddressCheckBox;

    private boolean ignoreUpdate;
    private Consumer<String> inputTextAddressConsumer;

    private ModbusTCPStringAddressCreatorStage() throws IOException
    {
        super("stringAddress/modbusTCPAddressStringCreator.fxml", VBox.class);
    }

    @Override
    public void setup()
    {
        super.setup();

        dataTypeChoiceBox.setConverter(new EnumStringConverter<>(ModbusTCPFunctionCode.class).setCapitalize());
        dataTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                return;
            }

            switch(newValue)
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

        lengthChoiceBox.setConverter(new EnumStringConverter<>(ModbusTCPDataLength.class).setCapitalize());
        lengthChoiceBox.getItems().addAll(ModbusTCPDataLength.values());
        lengthChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var functionCode = dataTypeChoiceBox.getValue();
            if(newValue == null || functionCode == null)
            {
                return;
            }

            var isBit = newValue == ModbusTCPDataLength.BIT;
            bitNumberLabel.setVisible(isBit);
            bitNumberTextField.setVisible(isBit);

            if(readAddressCheckBox.isSelected()) //Write values have no signed label whatsoever
            {
                switch(functionCode)
                {
                    case HOLDING_REGISTER:
                    case INPUT_REGISTER:
                        signedLabel.setVisible(!isBit);
                        signedCheckBox.setVisible(!isBit); //Signed booleans. A man can dream sometime.
                        break;
                }
            }else
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
        readAddressCheckBox.selectedProperty().addListener((observable, oldValue, readOnly) ->
                this.updateReadOnlyProperty()
        );

        acceptButton.setOnAction(event ->
        {
            var textAddress = convertedAddressTextField.getText();
            if(textAddress == null || textAddress.isEmpty())
            {
                return;
            }

            if(inputTextAddressConsumer != null)
            {
                inputTextAddressConsumer.accept(textAddress);
                inputTextAddressConsumer = null;
            }

            this.getStageSetter().close();
        });

        Stream.of(dataTypeChoiceBox.valueProperty(), lengthChoiceBox.valueProperty(),
                addressTextField.textProperty(), bitNumberTextField.textProperty(),
                signedCheckBox.selectedProperty())
                .forEach(property ->
                        property.addListener((observable, oldValue, newValue) ->
                                this.updateTextConvertedAddress()
                        )
                );
    }

    @Override
    public void showStage()
    {
        this.updateTextConvertedAddress();

        var children = super.parent.getChildren();
        if(inputTextAddressConsumer == null)
        {
            children.remove(acceptButtonStackPane);
        }else
        {
            if(!children.contains(acceptButtonStackPane))
            {
                children.add(acceptButtonStackPane);
            }
        }

        super.showStage();
    }

    public void showAsStandalone()
    {
        readAddressCheckBox.setSelected(true);
        readAddressCheckBox.setDisable(false);

        inputTextAddressConsumer = null;
        this.showStage();
    }

    public void showAsInputTextAddress(Consumer<String> inputTextAddressConsumer, boolean readOnly)
    {
        readAddressCheckBox.setSelected(readOnly);
        readAddressCheckBox.setDisable(true);

        this.inputTextAddressConsumer = inputTextAddressConsumer;
        this.showAsStandalone();
    }

    public void loadTextConvertedAddress(String textAddress)
    {
        var data = ModbusTCPAddressStringParser.parseDataFromString(textAddress);
        if(data == null)
        {
            return;
        }

        ignoreUpdate = true;
        dataTypeChoiceBox.setValue(data.getFunctionCode());
        lengthChoiceBox.setValue(data.getDataLength());
        addressTextField.setText("" + data.getAddress());
        bitNumberLabel.setText("" + data.getBitNumber());
        signedCheckBox.setSelected(data.isSigned());
        ignoreUpdate = false;

        this.updateTextConvertedAddress();
    }

    public void updateTextConvertedAddress()
    {
        if(ignoreUpdate)
        {
            return;
        }

        var functionCode = dataTypeChoiceBox.getValue();
        var dataLength = lengthChoiceBox.getValue();
        if(functionCode == null || dataLength == null)
        {
            convertedAddressTextField.setText("");
            return;
        }

        var address = Util.parseInt(addressTextField.getText(), 0);
        var bitNumber = Util.parseInt(bitNumberLabel.getText(), 0);
        var signed = signedCheckBox.isSelected();
        var read = readAddressCheckBox.isSelected();

        var data = new ModbusTCPAddressStringParser.Data(functionCode, dataLength, address, bitNumber, signed, read);

        var textAddress = ModbusTCPAddressStringParser.createStringFromData(data, read);
        convertedAddressTextField.setText(textAddress == null ? "" : textAddress);
    }

    private void convertPaneToBit() //Bits like coils only use the offset and are boolean only
    {
        lengthChoiceBox.setValue(ModbusTCPDataLength.BIT);

        lengthLabel.setVisible(false);
        lengthChoiceBox.setVisible(false);

        bitNumberLabel.setVisible(false);
        bitNumberTextField.setVisible(false);

        signedLabel.setVisible(false);
        signedCheckBox.setVisible(false); //Signed booleans. A man can dream sometime.
    }

    private void convertPaneToWord()
    {
        lengthChoiceBox.setValue(ModbusTCPDataLength.WORD);

        lengthLabel.setVisible(true);
        lengthChoiceBox.setVisible(true);
    }

    private void updateReadOnlyProperty()
    {
        var readOnly = readAddressCheckBox.isSelected();

        var dataTypeItems = dataTypeChoiceBox.getItems();
        dataTypeItems.clear();
        Stream.of(ModbusTCPFunctionCode.values())
                .filter(functionCode -> readOnly || !functionCode.isReadOnly())
                .forEach(dataTypeItems::add);
        dataTypeChoiceBox.getSelectionModel().selectFirst();
    }
}
