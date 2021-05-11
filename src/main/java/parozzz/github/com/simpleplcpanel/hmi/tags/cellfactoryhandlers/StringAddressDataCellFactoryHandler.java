package parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

public final class StringAddressDataCellFactoryHandler extends CellFactoryHandler<CommunicationStringAddressData>
{
    private final TextField textField;
    private final Property<CommunicationStringAddressData> property;

    public StringAddressDataCellFactoryHandler()
    {
        this.textField = new TextField();
        this.property = new SimpleObjectProperty<>();
    }

    @Override
    public void init()
    {
        super.init();

        textField.setMinSize(0, 0);
        textField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        textField.setAlignment(Pos.CENTER);
        textField.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        textField.setBorder(null);
        textField.setEditable(false);

        textField.setOnMouseClicked(event ->
        {
            if(event.getButton() != MouseButton.PRIMARY)
            {
                return;
            }

            var communicationType = this.getCommunicationType();
            if(communicationType != null)
            {
                var creatorStage = communicationType.supplyStringAddressCreatorStage();
                if(creatorStage != null)
                {
                    creatorStage.showAsStandalone();
                }
            }
        });

        textField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                return;
            }

            var communicationType = this.getCommunicationType();
            if(communicationType != null)
            {
                var stringAddressData = communicationType.parseStringAddressData(newValue);
                if(stringAddressData != null && stringAddressData.validate())
                {
                    property.setValue(stringAddressData);
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
    private CommunicationType<?> getCommunicationType()
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
        if(tag instanceof CommunicationTag)
        {
            return ((CommunicationTag) tag).getCommunicationType();
        }

        return null;
    }
}
