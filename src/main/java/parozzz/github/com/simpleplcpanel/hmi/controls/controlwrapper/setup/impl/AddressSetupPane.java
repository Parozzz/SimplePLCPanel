package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public class AddressSetupPane<A extends AddressAttribute>
        extends SetupPane<A>
{
    @FXML
    private TextField tagKeyTextField;

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

        tagKeyTextField.setOnMouseClicked(event ->
        {
            var addressAttribute = this.fetchAddressAttribute();
            if(addressAttribute != null)
            {
                tagStage.showAsSelection(tag -> {
                    addressAttribute.setValue(addressAttribute.getTagAttributeProperty(), tag);
                    tagKeyTextField.setText(tag.getHierarchicalKey());
                });
            }
        });

        super.getAttributeChangerList().setPostReadConsumer(attribute ->
        {
            var tag = attribute.getValue(attribute.getTagAttributeProperty());
            if(tag == null)
            {
                tagKeyTextField.setText("");
                return;
            }

            tagKeyTextField.setText(tag.getHierarchicalKey());
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
        if(selectedControlWrapper == null)
        {
            return null;
        }

        return AttributeFetcher.fetch(selectedControlWrapper, super.getAttributeType());
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
