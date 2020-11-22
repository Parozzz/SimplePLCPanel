package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.control;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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

    private final VBox mainVBox;

    public ButtonDataSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "ButtonDataSetupPane", "ButtonData", ButtonDataAttribute.class);

        this.mainVBox = (VBox) FXUtil.loadFXML("setupv2/buttonDataSetupPaneV2.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        buttonTypeChoiceBox.setConverter(new EnumStringConverter<>(ButtonWrapperType.class).setCapitalize());
        buttonTypeChoiceBox.getItems().addAll(ButtonWrapperType.values());
        buttonTypeChoiceBox.setValue(ButtonWrapperType.NORMAL);

        super.getAttributeChangerList().create(buttonTypeChoiceBox.valueProperty(), ButtonDataAttribute.TYPE);

        super.computeGlobalProperties();
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }
}
