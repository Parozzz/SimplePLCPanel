package parozzz.github.com.simpleplcpanel.hmi.controls.setup.panes.control;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.control.ButtonDataAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.impl.ButtonWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.setup.panes.SetupPane;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public class ButtonDataSetupPane extends SetupPane<ButtonDataAttribute>
{
    @FXML private ChoiceBox<ButtonWrapper.Type> buttonTypeChoiceBox;
    @FXML private Label toggleInformationLabel;

    private final VBox mainVBox;

    public ButtonDataSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "ButtonDataSetupPane", AttributeType.BUTTON_DATA);

        this.mainVBox = (VBox) FXUtil.loadFXML("setup/buttonDataSetupPane.fxml", this);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        toggleInformationLabel.setVisible(false); //Start as not visible :)

        buttonTypeChoiceBox.setConverter(new EnumStringConverter<>(ButtonWrapper.Type.class).setCapitalize());
        buttonTypeChoiceBox.getItems().addAll(ButtonWrapper.Type.values());
        buttonTypeChoiceBox.setValue(ButtonWrapper.Type.NORMAL);
        buttonTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
                toggleInformationLabel.setVisible(newValue == ButtonWrapper.Type.TOGGLE)
        );
        super.getAttributeChangerList().create(buttonTypeChoiceBox.valueProperty(), ButtonDataAttribute.TYPE);

        super.computeProperties();
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }
}
