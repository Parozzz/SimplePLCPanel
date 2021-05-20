package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl;

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
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.SetupPane;
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

    private final TagStage tagStage;
    private final VBox mainVBox;

    public AddressSetupPane(ControlWrapperSetupStage setupPage, TagStage tagStage,
            String buttonText, AttributeType<A> attributeType) throws IOException
    {
        super(setupPage, buttonText + "SetupPage", buttonText, attributeType);

        this.tagStage = tagStage;
        this.mainVBox = (VBox) FXUtil.loadFXML("setup/addressSetupPaneV3.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        tagKeyTextField.setCursor(Cursor.HAND);

        tagKeyTextField.setContextMenu(
                ContextMenuBuilder.builder()
                        .simple("Clear", () ->
                        {
                            var addressAttribute = this.fetchAddressAttribute();
                            if (addressAttribute != null)
                            {
                                addressAttribute.setValue(addressAttribute.getTagAttributeProperty(), null);
                            }
                        }).getContextMenu()
        );

        tagKeyTextField.setOnMouseClicked(event ->
        {
            if (event.getButton() == MouseButton.PRIMARY)
            {
                var addressAttribute = this.fetchAddressAttribute();
                if (addressAttribute != null)
                {
                    tagStage.showAsSelection(addressAttribute::setCommunicationTag);
                }
            }
        });

        super.getAttributeChangerList().setPostReadConsumer(attribute ->
        {
            var tag = attribute.getValue(attribute.getTagAttributeProperty());
            if (tag == null)
            {
                tagKeyTextField.setText("");
                stringAddressTextField.setText("");
                return;
            }

            tagKeyTextField.setText(tag.getHierarchicalKey());

            var stringAddressString = tag.getStringAddressData();
            if (stringAddressString != null)
            {
                stringAddressTextField.setText(stringAddressString.getStringData());
            }
        });
        super.computeProperties(); //Do this after i parse the address pane so all the values inside the attribute changer list are there.
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
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
