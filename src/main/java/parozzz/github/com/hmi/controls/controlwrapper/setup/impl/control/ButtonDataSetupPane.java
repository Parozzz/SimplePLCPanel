package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.control;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.control.ButtonDataAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.impl.button.ButtonWrapperType;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public class ButtonDataSetupPane extends SetupPane<ButtonDataAttribute>
{
    @FXML private ChoiceBox<ButtonWrapperType> buttonTypeChoiceBox;
    @FXML private Label toggleInformationLabel;

    private final VBox mainVBox;

    public ButtonDataSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "ButtonDataSetupPane", "Button Data", AttributeType.BUTTON_DATA, false);

        this.mainVBox = (VBox) FXUtil.loadFXML("setup/buttonDataSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        toggleInformationLabel.setVisible(false); //Start as not visible :)

        buttonTypeChoiceBox.setConverter(new EnumStringConverter<>(ButtonWrapperType.class).setCapitalize());
        buttonTypeChoiceBox.getItems().addAll(ButtonWrapperType.values());
        buttonTypeChoiceBox.setValue(ButtonWrapperType.NORMAL);
        buttonTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
                toggleInformationLabel.setVisible(newValue == ButtonWrapperType.TOGGLE)
        );
        super.getAttributeChangerList().create(buttonTypeChoiceBox.valueProperty(), ButtonDataAttribute.TYPE);

        super.computeGlobalProperties();
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }
}
