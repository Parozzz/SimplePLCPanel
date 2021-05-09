package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.SplitPaneSkin;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIPane;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;

public final class TagSplitPane extends FXObject implements HMIPane
{
    @FXML private Label nameLabel;
    @FXML private ChoiceBox<CommunicationType<?>> typeChoiceBox;
    @FXML private TextField addressTextField;

    private final Tag tag;
    private final SplitPane splitPane;

    public TagSplitPane(Tag tag) throws IOException
    {
        this.tag = tag;
        splitPane = (SplitPane) FXUtil.loadFXML("tabSplitPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        splitPane.setContextMenu(tag.getContextMenu());

        nameLabel.setText(tag.getKey());

        tag.communicationTypeProperty().bindBidirectional(typeChoiceBox.valueProperty());
        typeChoiceBox.setMinSize(0, 0);
        typeChoiceBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        typeChoiceBox.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
        typeChoiceBox.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        typeChoiceBox.getItems().addAll(CommunicationType.values());
        typeChoiceBox.setConverter(new StringConverter<>()
        {
            @Override
            public String toString(CommunicationType<?> communicationType)
            {
                return Util.capitalizeWithUnderscore(communicationType.getName());
            }

            @Override
            public CommunicationType<?> fromString(String s)
            {
                return CommunicationType.getByName(s);
            }
        });

        typeChoiceBox.setValue(tag.getCommunicationType());
        typeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue != null)
            {
                tag.communicationTypeProperty().setValue(newValue);
            }
        });


        addressTextField.setEditable(false);
        addressTextField.setMinSize(0, 0);
        addressTextField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        addressTextField.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
        addressTextField.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        addressTextField.textProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var communicationType = tag.getCommunicationType();
            if (communicationType == null)
            {
                return;
            }

            var stringAddressData = communicationType.parseStringAddressData(newValue);
            if (stringAddressData == null || !stringAddressData.validate())
            {
                return;
            }

            tag.communicationStringAddressDataProperty().setValue(stringAddressData);
        });
        addressTextField.setOnMouseClicked(event ->
        {
            var communicationType = tag.getCommunicationType();
            if (communicationType != null)
            {
                var creatorStage = communicationType.supplyStringAddressCreatorStage();
                if (creatorStage != null)
                {
                    creatorStage.showAsStandalone();
                }
            }
        });

        addressTextField.setText(tag.getStringAddressData() == null ? "" : tag.getStringAddressData().getStringData());
        tag.communicationStringAddressDataProperty().addListener((observableValue, oldValue, newValue) ->
                addressTextField.setText(newValue == null ? "" : newValue.getStringData())
        );
    }

    @Override
    public SplitPane getMainParent()
    {
        return splitPane;
    }

}
