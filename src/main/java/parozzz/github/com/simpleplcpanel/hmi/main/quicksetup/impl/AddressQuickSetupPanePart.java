package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.BackgroundAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.AddressSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPanePart;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public final class AddressQuickSetupPanePart
        extends FXObject
        implements QuickSetupPanePart
{
    @FXML private Label addressTypeLabel;
    @FXML private TextField addressTextField;
    @FXML private ChoiceBox<AddressSetupPane.AddressType> addressTypeChoiceBox;

    private final QuickSetupPane quickSetupPane;
    private final CommunicationDataHolder communicationDataHolder;
    private final boolean readOnly;

    private final VBox vBox;

    public AddressQuickSetupPanePart(QuickSetupPane quickSetupPane,
            CommunicationDataHolder communicationDataHolder,
            boolean readOnly) throws IOException
    {
        this.quickSetupPane = quickSetupPane;
        this.communicationDataHolder = communicationDataHolder;
        this.readOnly = readOnly;

        this.vBox = (VBox) FXUtil.loadFXML("quickproperties/addressQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {

        communicationDataHolder.getCommunicationStage().addCommunicationTypeListener(communicationType ->
                quickSetupPane.loadAllValuesFromControlWrapperOf(this.getAttributeType())
        );

        addressTypeLabel.setText(readOnly ? "Read Address" : "Write Address");
        addressTextField.setOnMouseClicked(mouseEvent ->
        {
            var controlWrapper = quickSetupPane.getSelectedControlWrapper();
            if(controlWrapper == null)
            {
                return;
            }

            var addressType = addressTypeChoiceBox.getValue();
            if(addressType == null || addressType == AddressSetupPane.AddressType.NONE)
            {
                return;
            }

            var addressAttribute = AttributeFetcher.fetch(controlWrapper, this.getAttributeType());
            if(addressAttribute == null)
            {
                return;
            }

            if(addressType == AddressSetupPane.AddressType.COMMUNICATION)
            {
                var communicationType = communicationDataHolder.getCurrentCommunicationType();
                if(communicationType == null)
                {
                    return;
                }

                var stringAddressCreator = communicationType.supplyStringAddressCreatorStage();
                if(stringAddressCreator == null)
                {
                    return;
                }

                stringAddressCreator.loadAddressAttributeToActualValues(addressAttribute);
                stringAddressCreator.showAsInputTextAddress(addressAttribute);
            }
        });

        addressTypeChoiceBox.setConverter(new EnumStringConverter<>(AddressSetupPane.AddressType.class).setCapitalize());
        addressTypeChoiceBox.getItems().addAll(AddressSetupPane.AddressType.values());
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public boolean validateControlWrapper(ControlWrapper<?> controlWrapper)
    {
        return controlWrapper.getAttributeTypeManager().hasType(this.getAttributeType());
    }

    @Override
    public void clearControlWrapper()
    {
        addressTextField.setText("");
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(this.getAttributeType())
                .readOnlyIndirect(addressTextField.textProperty(),
                        CommunicationStringAddressData::getStringData,
                        () ->
                        {
                            var attributeProperty = this.getAttributeProperty();
                            if(attributeProperty == null)
                            {
                                addressTextField.setText("");
                                return null;
                            }

                            return attributeProperty;
                        }
                ).direct(addressTypeChoiceBox.valueProperty(), AddressAttribute.ADDRESS_TYPE);
    }

    @Nullable
    private AttributeProperty<? extends CommunicationStringAddressData> getAttributeProperty()
    {
        var addressType = addressTypeChoiceBox.getValue();
        if(addressType == null || addressType == AddressSetupPane.AddressType.NONE)
        {
            return null;
        }

        return communicationDataHolder.getCurrentCommunicationType().getAttributeProperty();
    }

    private AttributeType<? extends AddressAttribute> getAttributeType()
    {
        return readOnly ? AttributeType.READ_ADDRESS : AttributeType.WRITE_ADDRESS;
    }
}
