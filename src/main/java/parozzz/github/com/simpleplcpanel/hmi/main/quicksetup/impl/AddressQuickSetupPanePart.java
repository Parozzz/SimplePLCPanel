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
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.AddressSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPanePart;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public final class AddressQuickSetupPanePart
        extends FXObject
        implements QuickSetupPanePart
{
    @FXML private Label topLabel;
    @FXML private TextField tagTextField;

    private final QuickSetupPane quickSetupPane;
    private final TagStage tagStage;
    private final boolean readOnly;

    private final VBox vBox;

    public AddressQuickSetupPanePart(QuickSetupPane quickSetupPane,
            TagStage tagStage,
            boolean readOnly) throws IOException
    {
        this.quickSetupPane = quickSetupPane;
        this.tagStage = tagStage;
        this.readOnly = readOnly;

        this.vBox = (VBox) FXUtil.loadFXML("quickproperties/addressQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        topLabel.setText(readOnly ? "Read Tag" : "Write Tag");
        tagTextField.setOnMouseClicked(mouseEvent ->
        {
            var controlWrapper = quickSetupPane.getSelectedControlWrapper();
            if(controlWrapper == null)
            {
                return;
            }

            var addressAttribute = AttributeFetcher.fetch(controlWrapper, this.getAttributeType());
            if(addressAttribute != null)
            {
                tagStage.showAsSelection(tag ->
                        addressAttribute.setValue(addressAttribute.getTagAttributeProperty(), tag)
                );
            }
        });
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
        tagTextField.setText("");
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(this.getAttributeType())
                .readOnlyIndirect(tagTextField.textProperty(),
                        CommunicationTag::getHierarchicalKey,
                        () -> readOnly ? ReadAddressAttribute.READ_TAG : WriteAddressAttribute.WRITE_TAG
                );
    }

    private AttributeType<? extends AddressAttribute> getAttributeType()
    {
        return readOnly ? AttributeType.READ_ADDRESS : AttributeType.WRITE_ADDRESS;
    }
}
