package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;

public class AddressSetupPane<A extends AddressAttribute>
        extends SetupPane<A>
{

    public enum AddressType
    {
        NONE,
        COMMUNICATION,
        //LOCAL;
    }

    @FXML private ChoiceBox<AddressType> addressTypeChoiceBox;
    @FXML private TextField textAddressTextField;
    @FXML private Button changeSettingsButton;

    private final CommunicationDataHolder communicationDataHolder;
    private final VBox mainVBox;

    public AddressSetupPane(ControlWrapperSetupStage setupPage, CommunicationDataHolder communicationDataHolder,
            String buttonText, AttributeType<A> attributeType) throws IOException
    {
        super(setupPage, buttonText + "SetupPage", buttonText, attributeType);

        this.communicationDataHolder = communicationDataHolder;
        this.mainVBox = (VBox) FXUtil.loadFXML("setup/addressSetupPaneV2.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        addressTypeChoiceBox.setConverter(new EnumStringConverter<>(AddressType.class).setCapitalize());
        addressTypeChoiceBox.getItems().addAll(AddressType.values());
        addressTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                return;
            }

            textAddressTextField.setText(""); //Reset it just to be sure. In case is populated.

            boolean isNone = false;
            switch (newValue)
            {
                case NONE:
                    isNone = true;
                    break;
                case COMMUNICATION:
                    var communicationType = communicationDataHolder.getCommunicationStage().getCommunicationType();
                    if(communicationType == CommunicationType.NONE)
                    {
                        isNone = true;
                    }
                    break;
            }

            textAddressTextField.setEditable(!isNone);
            this.updateTextAddressTextField();
        });

        changeSettingsButton.setOnMouseClicked(event ->
        {
            var addressType = addressTypeChoiceBox.getValue();
            if (addressType == null || addressType == AddressType.NONE)
            {
                return;
            }

            if(addressType == AddressType.COMMUNICATION)
            {
                var communicationType = communicationDataHolder.getCurrentCommunicationType();
                if(communicationType == null)
                {
                    return;
                }

                var addressAttribute = this.fetchAddressAttribute();
                if(addressAttribute == null)
                {
                    return;
                }

                var creatorStage = communicationType.supplyStringAddressCreatorStage();
                if(creatorStage == null)
                {
                    return;
                }

                //Before showing the actual page, load the data from the actual string data inside it.
                //I don't care if is valid or not. If invalid, it won't load and will be defaulted!
                creatorStage.setAsSubWindow(super.getSetupStage());
                creatorStage.loadStringDataToActualValues(textAddressTextField.getText());
                creatorStage.setReadOnly(this.isReadOnly());
                creatorStage.showAsInputTextAddress(addressAttribute);
            }
        });

        //This allow updating the string parser when a value here changes!
        textAddressTextField.textProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue == null || newValue.isEmpty())
            {
                return;
            }

            var addressType = addressTypeChoiceBox.getValue();
            if (addressType == null || addressType == AddressType.NONE)
            {
                return;
            }

            var addressAttribute = this.fetchAddressAttribute();
            if (addressAttribute == null)
            {
                return;
            }

            if(addressType == AddressType.COMMUNICATION)
            {
                var communicationType = communicationDataHolder.getCurrentCommunicationType();
                if(communicationType != null)
                {
                    communicationType.updateAddressAttributeWithStringData(newValue, addressAttribute);
                }
            }
        });
        /*
        textAddressTextField.setOnMouseExited(mouseEvent -> this.parseAddressStringParser());
        textAddressTextField.setOnKeyReleased(keyEvent ->
        {
            if(FXUtil.CONTROL_PASTE.match(keyEvent))
            {
                this.parseAddressStringParser();
            }
        });*/

        super.getAttributeChangerList().create(addressTypeChoiceBox.valueProperty(), AddressAttribute.ADDRESS_TYPE)
                .setPostReadRunnable(this::updateTextAddressTextField);
        super.computeProperties(); //Do this after i parse the address pane so all the values inside the attribute changer list are there.
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }

    public TextField getTextAddressTextField()
    {
        return textAddressTextField;
    }

    private void updateTextAddressTextField()
    {
        var addressType = this.addressTypeChoiceBox.getValue();
        if (addressType == null || addressType == AddressType.NONE)
        {
            return;
        }

        if(addressType == AddressType.COMMUNICATION)
        {
            var communicationType = communicationDataHolder.getCurrentCommunicationType();
            if(communicationType == null || communicationType == CommunicationType.NONE)
            {
                return;
            }

            var addressAttribute = this.fetchAddressAttribute();
            if (addressAttribute == null)
            {
                return;
            }

            var stringAddressData = addressAttribute.getValue(communicationType.getAttributeProperty());
            if(stringAddressData.validate())
            {
                textAddressTextField.setText(stringAddressData.getStringData());
            }
        }
    }

    @Nullable
    private AddressAttribute fetchAddressAttribute()
    {
        //I need them both because one of the AddressAttribute is a global one
        var selectedControlWrapper = super.getSetupStage().getSelectedControlWrapper();
        if (selectedControlWrapper == null)
        {
            return null;
        }

        return AttributeFetcher.fetch(selectedControlWrapper, super.getAttributeType());
    }

    private boolean isReadOnly()
    {
        return super.getAttributeType() == AttributeType.READ_ADDRESS;
    }
/*
    private void parseAddressStringParser()
    {
        if(selectedAddressPane == null || selectedAddressPane.getAddressDataType() == AddressDataType.NONE)
        {
            textAddressTextField.setText("");
            return;
        }

        selectedAddressPane.getAddressStringParser().parse(textAddressTextField.getText());
    }*/
}
