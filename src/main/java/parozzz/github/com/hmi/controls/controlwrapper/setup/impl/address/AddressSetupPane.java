package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.data.AddressDataType;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.AddressPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.modbustcp.ModbusTCPAddressPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.siemens.SiemensAddressPane;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;

public class AddressSetupPane<A extends AddressAttribute> extends SetupPane<A>
{
    //Labels

    @FXML
    private ChoiceBox<AddressDataType> addressTypeChoiceBox;
    @FXML
    private StackPane centerStackPane;
    @FXML
    private TextField textAddressTextField;

    private final VBox mainVBox;

    private final SiemensAddressPane siemensAddressPane;
    private final ModbusTCPAddressPane modbusTCPAddressPane;
    private AddressPane selectedAddressPane;

    public AddressSetupPane(ControlWrapperSetupStage setupPage, String buttonText,
            AttributeType<A> attributeType) throws IOException
    {
        super(setupPage, buttonText + "SetupPage", buttonText, attributeType);

        this.mainVBox = (VBox) FXUtil.loadFXML("setup/addressSetupPane.fxml", this);

        var readAddress = attributeType == AttributeType.READ_ADDRESS;

        this.siemensAddressPane = new SiemensAddressPane(this);
        this.modbusTCPAddressPane = new ModbusTCPAddressPane(this, readAddress); //The global one will always be read address
    }

    @Override
    public void setup()
    {
        super.setup();

        siemensAddressPane.setup();
        modbusTCPAddressPane.setup();

        addressTypeChoiceBox.setConverter(
                FXUtil.toStringOnlyConverter(
                        addressDataType -> Util.capitalizeWithUnderscore(addressDataType.getName())
                )
        );
        addressTypeChoiceBox.getItems().addAll(AddressDataType.values());
        addressTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            textAddressTextField.setText(""); //Reset it just to be sure. In case is populated.

            var children = centerStackPane.getChildren();
            children.clear();

            if(newValue == AddressDataType.NONE)
            {
                var emptyStackPane = new StackPane();
                emptyStackPane.setPrefSize(200, 200);
                emptyStackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                children.add(emptyStackPane);
                return;
            }

            AddressAttribute attribute;

            //I need them both because one of the AddressAttribute is a global one
            var selectedControlWrapper = super.getSetupStage().getSelectedControlWrapper();
            var selectedWrapperState = super.getSetupStage().getSelectedWrapperState();
            if(selectedControlWrapper == null || selectedWrapperState == null
                    || (attribute = AttributeFetcher.fetch(selectedControlWrapper, super.getAttributeType())) == null)
            {
                addressTypeChoiceBox.setValue(AddressDataType.NONE);
                return;
            }

            attribute.setValue(AddressAttribute.DATA_TYPE, newValue);
            //attribute.setDataType(newValue);
            if(newValue == AddressDataType.SIEMENS)
            {
                selectedAddressPane = siemensAddressPane;
            }else if(newValue == AddressDataType.MODBUS_TCP)
            {
                selectedAddressPane = modbusTCPAddressPane;
            }

            if(selectedAddressPane != null)
            {
                selectedAddressPane.setDefault(); //Defaulting it should avoid dragging values from another control

                children.add(selectedAddressPane.getMainParent());
                textAddressTextField.setText(selectedAddressPane.getAddressStringParser().getStringAddress());
            }
        });

        //This allow updating the string parser when a value here changes!
        textAddressTextField.setOnAction(event -> this.parseAddressStringParser());
        textAddressTextField.setOnMouseExited(mouseEvent -> this.parseAddressStringParser());
        textAddressTextField.setOnKeyReleased(keyEvent ->
        {
            if(FXUtil.CONTROL_PASTE.match(keyEvent))
            {
                this.parseAddressStringParser();
            }
        });

        super.getAttributeChangerList().create(addressTypeChoiceBox.valueProperty(), AddressAttribute.DATA_TYPE);
        siemensAddressPane.parseAttributeChangerList(super.getAttributeChangerList());
        modbusTCPAddressPane.parseAttributeChangerList(super.getAttributeChangerList());

        super.computeProperties(); //Do this after i parse the address pane so all the values inside the attribute changer list are there.
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }

    @Override
    public void doDataCopiedFromAttribute()
    {
        super.doDataCopiedFromAttribute();

        this.parseAddressStringParser();
    }

    public void setAsState()
    {
        super.setAsState();
        siemensAddressPane.setAsState();
        modbusTCPAddressPane.setAsState();
    }

    public void setAsGlobal()
    {
        super.setAsGlobal();
        siemensAddressPane.setAsGlobal();
        modbusTCPAddressPane.setAsGlobal();
    }

    public TextField getTextAddressTextField()
    {
        return textAddressTextField;
    }

    private void parseAddressStringParser()
    {
        if(selectedAddressPane == null || selectedAddressPane.getAddressDataType() == AddressDataType.NONE)
        {
            textAddressTextField.setText("");
            return;
        }

        selectedAddressPane.getAddressStringParser().parse(textAddressTextField.getText());
    }
}
