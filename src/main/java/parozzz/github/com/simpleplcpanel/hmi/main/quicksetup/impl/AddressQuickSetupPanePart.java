package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
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
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPanePart;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public final class AddressQuickSetupPanePart
        extends FXObject
        implements QuickSetupPanePart
{
    @FXML private Label topLabel;
    @FXML private TextField tagTextField;
    private TextField stringAddressDataTextField;

    private final MainEditStage mainEditStage;
    private final QuickSetupPane quickSetupPane;
    private final CommunicationDataHolder communicationDataHolder;
    private final TagsManager tagsManager;
    private final boolean readOnly;

    private final VBox vBox;

    private final Property<CommunicationTag> tagProperty;

    public AddressQuickSetupPanePart(MainEditStage mainEditStage, QuickSetupPane quickSetupPane,
            TagsManager tagsManager, CommunicationDataHolder communicationDataHolder,
            boolean readOnly) throws IOException
    {
        this.mainEditStage = mainEditStage;
        this.quickSetupPane = quickSetupPane;
        this.tagsManager = tagsManager;
        this.communicationDataHolder = communicationDataHolder;
        this.readOnly = readOnly;

        this.vBox = (VBox) FXUtil.loadFXML("quickproperties/addressQuickSetupPane.fxml", this);

        this.tagProperty = new SimpleObjectProperty<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        topLabel.setText(readOnly ? "Read Tag" : "Write Tag");

        stringAddressDataTextField = new TextField();
        stringAddressDataTextField.setMinSize(0, 0);
        stringAddressDataTextField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        stringAddressDataTextField.setBorder(null);
        stringAddressDataTextField.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        stringAddressDataTextField.setEditable(false);
        stringAddressDataTextField.setFocusTraversable(false);
        stringAddressDataTextField.setAlignment(Pos.CENTER);

        tagTextField.setCursor(Cursor.HAND);
        tagTextField.setContextMenu(
                ContextMenuBuilder.builder()
                        .custom(stringAddressDataTextField, false)
                        .spacer(2)
                        .simple("Clear", () ->
                        {
                            //This is set directly to the attribute
                            var addressAttribute = this.fetchAttribute();
                            if (addressAttribute != null)
                            {
                                addressAttribute.setCommunicationTag(null);
                            }
                        }).getContextMenu()
        );

        tagTextField.setOnMouseClicked(mouseEvent ->
        {
            if (mouseEvent.getButton() == MouseButton.PRIMARY)
            {
                var addressAttribute = this.fetchAttribute();
                if (addressAttribute != null)
                {
                    TagStage.showAsInput(
                            tagsManager, communicationDataHolder, mainEditStage, addressAttribute::setCommunicationTag
                    );
                }
            }
        });

        ChangeListener<String> tagKeyChanged = (observable, oldValue, newValue) ->
        {
            if (newValue == null)
            {
                tagTextField.setText("");
                return;
            }

            tagTextField.setText(newValue);
        };

        tagProperty.addListener((observable, oldValue, newValue) ->
        {
            if (oldValue != null)
            {
                oldValue.keyValueProperty().removeListener(tagKeyChanged);
            }

            if(newValue == null)
            {
                tagTextField.setText("");
                stringAddressDataTextField.setText("");
            }
            else
            {
                newValue.keyValueProperty().addListener(tagKeyChanged);

                tagTextField.setText(newValue.getHierarchicalKey());

                var stringAddressData = newValue.getStringAddressData();
                if (stringAddressData != null)
                {
                    stringAddressDataTextField.setText(stringAddressData.getStringData());
                }
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
        tagProperty.setValue(null);
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(this.getAttributeType())
                .direct(tagProperty, this.getAttributeProperty());
                /*
                .addLoadConsumer(attributeType ->
                {
                    if (attributeType == this.getAttributeType())
                    {
                        var attribute = this.fetchAttribute();
                        if (attribute != null)
                        {
                            var tag = attribute.getCommunicationTag();
                            if (tag == null)
                            {
                                tagTextField.setText("");
                                stringAddressDataTextField.setText("");
                                return;
                            }

                            tagTextField.setText(tag.getHierarchicalKey());

                            var stringAddressData = tag.getStringAddressData();
                            if (stringAddressData != null)
                            {
                                stringAddressDataTextField.setText(stringAddressData.getStringData());
                            }
                        }
                    }
                });*/
    }

    private AttributeProperty<CommunicationTag> getAttributeProperty()
    {
        return readOnly ? ReadAddressAttribute.READ_TAG : WriteAddressAttribute.WRITE_TAG;
    }

    private AddressAttribute fetchAttribute()
    {
        var controlWrapper = quickSetupPane.getSelectedControlWrapper();
        return controlWrapper == null
                ? null
                : AttributeFetcher.fetch(controlWrapper, this.getAttributeType());
    }

    private AttributeType<? extends AddressAttribute> getAttributeType()
    {
        return readOnly ? AttributeType.READ_ADDRESS : AttributeType.WRITE_ADDRESS;
    }
}
