package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl;

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
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPanePart;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
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

/*
        var addressTooltip = new Tooltip();
        addressTooltip.setShowDelay(Duration.seconds(1));
        addressTooltip.setShowDuration(Duration.seconds(3));
        addressTooltip.setFont(Font.font(12));
        addressTooltip.addEventFilter(WindowEvent.WINDOW_SHOWN, event ->
        {
            var addressAttribute = this.fetchAttribute();
            if(addressAttribute == null)
            {
                event.consume();
                return;
            }

            var tag = addressAttribute.getCommunicationTag();
            if(tag == null)
            {
                event.consume();
                return;
            }

            var stringAddressData = tag.getStringAddressData();
            if(stringAddressData != null)
            {
                addressTooltip.setText(stringAddressData.getStringData());
            }
        });
        tagTextField.setTooltip(addressTooltip);
        */

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
                            var addressAttribute = this.fetchAttribute();
                            if (addressAttribute != null)
                            {
                                addressAttribute.setCommunicationTag(null);
                            }
                        }).getContextMenu()
        );

        tagTextField.setOnMouseClicked(mouseEvent ->
        {
            if(mouseEvent.getButton() == MouseButton.PRIMARY)
            {
                var addressAttribute = this.fetchAttribute();
                if(addressAttribute != null)
                {
                    tagStage.showAsSelection(addressAttribute::setCommunicationTag);
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
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(this.getAttributeType())
                .addLoadConsumer(attributeType ->
                {
                    if(attributeType == this.getAttributeType())
                    {
                        var attribute = this.fetchAttribute();
                        if(attribute != null)
                        {
                            var tag = attribute.getCommunicationTag();
                            if(tag == null)
                            {
                                tagTextField.setText("");
                                stringAddressDataTextField.setText("");
                                return;
                            }

                            tagTextField.setText(tag.getHierarchicalKey());

                            var stringAddressData = tag.getStringAddressData();
                            if(stringAddressData != null)
                            {
                                stringAddressDataTextField.setText(stringAddressData.getStringData());
                            }
                        }
                    }
                });
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
