package parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

public final class StringAddressDataCellFactoryHandler extends CellFactoryHandler<CommunicationStringAddressData>
{
    private final TextField textField;
    private final Property<CommunicationStringAddressData> property;

    public StringAddressDataCellFactoryHandler(CommunicationDataHolder communicationDataHolder)
    {
        super(communicationDataHolder);

        this.textField = new TextField();
        this.property = new SimpleObjectProperty<>();
    }

    @Override
    public void init()
    {
        super.init();

        cell.setPadding(Insets.EMPTY);

        textField.setPadding(Insets.EMPTY);
        textField.setMinSize(0, 0);
        textField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        textField.setAlignment(Pos.CENTER);
        textField.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        textField.setBorder(null);
        textField.setEditable(false);
        textField.setCursor(Cursor.HAND);

        textField.setOnMouseClicked(event ->
        {
            if(event.getButton() != MouseButton.PRIMARY)
            {
                return;
            }

            var tag = this.getTag();
            if(tag != null && !tag.isLocal())
            {
                var communicationType = communicationDataHolder.getCurrentCommunicationType();
                if(communicationType != null)
                {
                    var creatorStage = communicationType.supplyStringAddressCreatorStage();
                    if(creatorStage != null)
                    {
                        var stringAddressData = tag.getStringAddressData();
                        if(stringAddressData != null)
                        {
                            creatorStage.loadStringDataToActualValues(stringAddressData.getStringData());
                        }

                        creatorStage.showAsInputTextAddress(tag);
                    }
                }
            }
        });

        textField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                return;
            }

            var tag = this.getTag();
            if(tag != null && !tag.isLocal())
            {
                var communicationType = communicationDataHolder.getCurrentCommunicationType();
                if(communicationType != null)
                {
                    var stringAddressData = communicationType.parseStringAddressData(newValue);
                    if(stringAddressData != null && stringAddressData.validate())
                    {
                        property.setValue(stringAddressData);
                    }
                }
            }
        });

        property.addListener((observable, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                textField.setText("");
                return;
            }

            textField.setText(newValue.getStringData());
        });
    }

    @Override
    protected void registerTag(CommunicationTag tag)
    {
        //This need BEFORE BINDING BIDIRECTIONAL. If not set, it will change the value when bind.
        property.setValue(tag.getStringAddressData());

        tag.communicationStringAddressDataProperty().bindBidirectional(property);
    }

    @Override
    protected void unregisterTag(CommunicationTag tag)
    {
        tag.communicationStringAddressDataProperty().unbindBidirectional(property);

        //This need AFTER BINDING BIDIRECTIONAL. If not set, it will change the value inside the binding.
        property.setValue(null);
    }

    @Override
    protected void setGraphic()
    {
        cell.setGraphic(textField);
    }

    @Nullable
    private CommunicationTag getTag()
    {
        var row = cell.getTreeTableRow();
        if(row == null)
        {
            return null;
        }

        var treeItem = row.getTreeItem();
        if(treeItem == null)
        {
            return null;
        }

        var tag = treeItem.getValue();
        return tag instanceof CommunicationTag
                ? (CommunicationTag) tag
               : null;
    }
}
