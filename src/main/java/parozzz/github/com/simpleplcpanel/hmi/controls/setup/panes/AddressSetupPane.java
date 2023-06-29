package parozzz.github.com.simpleplcpanel.hmi.controls.setup.panes;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.controls.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public class AddressSetupPane<A extends AddressAttribute>
        extends SetupPane<A>
{
    @FXML
    private TextField tagKeyTextField;

    @FXML
    private TextField stringAddressTextField;

    private final TagsManager tagsManager;
    private final CommunicationDataHolder communicationDataHolder;
    private final VBox mainVBox;

    private final Property<CommunicationTag> tagProperty;

    public AddressSetupPane(ControlWrapperSetupStage setupPage,
            TagsManager tagsManager, CommunicationDataHolder communicationDataHolder,
            AttributeType<A> attributeType) throws IOException
    {
        super(setupPage, attributeType.getAttributeClass().getSimpleName() + "-SetupPage", attributeType);

        this.tagsManager = tagsManager;
        this.communicationDataHolder = communicationDataHolder;
        this.mainVBox = (VBox) FXUtil.loadFXML("setup/addressSetupPaneV3.fxml", this);

        this.tagProperty = new SimpleObjectProperty<>();
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        tagKeyTextField.setCursor(Cursor.HAND);
        tagKeyTextField.setContextMenu(
                ContextMenuBuilder.builder()
                        .simple("Clear", () ->
                        {
                            tagProperty.setValue(null);
                            /*
                            var addressAttribute = this.fetchAddressAttribute();
                            if (addressAttribute != null)
                            {
                                addressAttribute.setValue(addressAttribute.getTagAttributeProperty(), null);
                            }*/
                        }).getContextMenu()
        );
        tagKeyTextField.setOnMouseClicked(event ->
        {
            if (event.getButton() == MouseButton.PRIMARY)
            {
                var addressAttribute = this.fetchAddressAttribute();
                if (addressAttribute != null)
                {
                    TagStage.showAsInput(tagsManager, communicationDataHolder, super.getSetupStage(), addressAttribute::setCommunicationTag);
                }
            }
        });

        ChangeListener<String> tagKeyChanged = (observable, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                tagKeyTextField.setText("");
                return;
            }

            tagKeyTextField.setText(newValue);
        };

        ChangeListener<CommunicationStringAddressData> communicationStringAddressDataChanged = (observable, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                stringAddressTextField.setText("");
                return;
            }

            stringAddressTextField.setText(newValue.getStringData());
        };
        tagProperty.addListener((observable, oldValue, newValue) ->
        {
            if(oldValue != null)
            {
                oldValue.communicationStringAddressDataProperty().removeListener(communicationStringAddressDataChanged);
                oldValue.keyValueProperty().removeListener(tagKeyChanged);
            }

            if(newValue == null)
            {
                tagKeyTextField.setText("");
                stringAddressTextField.setText("");
            }
            else
            {
                newValue.keyValueProperty().addListener(tagKeyChanged);
                newValue.communicationStringAddressDataProperty().addListener(communicationStringAddressDataChanged);

                tagKeyTextField.setText(newValue.getHierarchicalKey());

                var stringAddressString = newValue.getStringAddressData();
                if (stringAddressString != null)
                {
                    stringAddressTextField.setText(stringAddressString.getStringData());
                }
            }
        });

        super.getAttributeChangerList().create(tagProperty, this.getAttributeProperty());
        super.computeProperties(); //Do this after i parse the address pane so all the values inside the attribute changer list are there.
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }

    @Override
    public void clearControlWrapper()
    {
        super.clearControlWrapper();

        tagProperty.setValue(null);
    }

    private AttributeProperty<CommunicationTag> getAttributeProperty()
    {
        return this.getAttributeType() == AttributeType.READ_ADDRESS
                ? ReadAddressAttribute.READ_TAG
                : WriteAddressAttribute.WRITE_TAG;
    }

    @Nullable
    private AddressAttribute fetchAddressAttribute()
    {
        //I need them both because one of the AddressAttribute is a global one
        var selectedControlWrapper = super.getSetupStage().getSelectedControlWrapper();
        return selectedControlWrapper == null
                ? null
                : AttributeFetcher.fetch(selectedControlWrapper, super.getAttributeType());
    }
}
