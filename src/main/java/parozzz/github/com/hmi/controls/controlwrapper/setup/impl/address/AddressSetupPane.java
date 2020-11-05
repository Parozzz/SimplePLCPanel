package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
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

    @FXML private ChoiceBox<AddressDataType> addressTypeChoiceBox;

    @FXML private StackPane addressSetupStackPane;

    @FXML private TextField textAddressTextField;

    private final Class<A> attributeClass;
    private final boolean global;
    private final VBox mainVBox;

    private final SiemensAddressPane siemensAddressPane;
    private final ModbusTCPAddressPane modbusTCPAddressPane;
    private AddressPane selectedAddressPane;

    public AddressSetupPane(ControlWrapperSetupStage setupPage, String tabName,
                            Class<A> attributeClass, boolean global) throws IOException
    {
        super(setupPage, tabName + "SetupPage", tabName, attributeClass);

        this.attributeClass = attributeClass;
        this.global = global;
        this.mainVBox = (VBox) FXUtil.loadFXML("setup/addressSetupPane.fxml", this);

        this.siemensAddressPane = new SiemensAddressPane(this);
        this.modbusTCPAddressPane = new ModbusTCPAddressPane(global); //The global one will always be read address
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

            var children = addressSetupStackPane.getChildren();
            children.clear();

            if (newValue == AddressDataType.NONE)
            {
                var emptyAnchorPane = new AnchorPane();
                emptyAnchorPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                children.add(emptyAnchorPane);
                return;
            }

            AddressAttribute attribute;

            //I need them both because one of the AddressAttribute is a global one
            var selectedControlWrapper = super.getSetupStage().getSelectedControlWrapper();
            var selectedWrapperState = super.getSetupStage().getSelectedWrapperState();
            if (selectedControlWrapper == null || selectedWrapperState == null
                    || (attribute = AttributeFetcher.fetch(selectedControlWrapper, selectedWrapperState, attributeClass)) == null)
            {
                addressTypeChoiceBox.setValue(AddressDataType.NONE);
                return;
            }

            attribute.setValue(AddressAttribute.DATA_TYPE, newValue);
            //attribute.setDataType(newValue);
            if (newValue == AddressDataType.SIEMENS)
            {
                selectedAddressPane = siemensAddressPane;
            }
            else if(newValue == AddressDataType.MODBUS_TCP)
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

        super.getAttributeChangerList().create(addressTypeChoiceBox.valueProperty(), AddressAttribute.DATA_TYPE);

        this.parseAddressPane(siemensAddressPane);
        this.parseAddressPane(modbusTCPAddressPane);

        if(!global) //Do this after i parse the address pane so all the values inside the attribute changer list are there.
        {
            super.computeProperties();
            super.getSetupStage().getSelectAndMultipleWrite()
                    .onSelectingMultiplesChangeListener(selectMultiples -> textAddressTextField.setVisible(!selectMultiples));
        }
        else
        {
            super.computeGlobalProperties();
        }
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        siemensAddressPane.setDefault();
        modbusTCPAddressPane.setDefault();

        addressTypeChoiceBox.setValue(AddressDataType.NONE);
    }

    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }

    private void parseAddressPane(AddressPane addressPane)
    {
        addressPane.parseAttributeChangerList(super.getAttributeChangerList());

        var stringParser = addressPane.getAddressStringParser();
        stringParser.getProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (addressTypeChoiceBox.getValue() == addressPane.getAddressDataType())
            {
                textAddressTextField.setText(newValue);
            }
        });
        textAddressTextField.textProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (addressTypeChoiceBox.getValue() == addressPane.getAddressDataType())
            {
                stringParser.parse(newValue);
            }
        });
    }
}
