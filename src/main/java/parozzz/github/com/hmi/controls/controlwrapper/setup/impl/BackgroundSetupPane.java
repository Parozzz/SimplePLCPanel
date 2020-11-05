package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.BackgroundAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;

public final class BackgroundSetupPane extends SetupPane<BackgroundAttribute>
{
    @FXML private TextField imageNameTextField;
    @FXML private ImageView fileImageView;
    @FXML private Button selectImageFileButton;

    @FXML private ColorPicker backgroundColorPicker;
    @FXML private ColorPicker borderColorPicker;
    @FXML private TextField borderWidthTextField;
    @FXML private ToggleButton stretchToggleButton;

    private final VBox mainVBox;

    public BackgroundSetupPane(ControlWrapperSetupStage setupStage) throws IOException
    {
        super(setupStage, "BackgroundSetupPane", "Background", BackgroundAttribute.class);

        this.mainVBox = (VBox) FXUtil.loadFXML("setup/backgroundSetupPane.fxml", this);
    }

    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }

    @Override
    public void setup()
    {
        super.setup();

        imageNameTextField.textProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue == null)
            {
                fileImageView.setImage(null);
                return;
            }

            var imageURI = super.getSetupStage().getControlMainPage().getMainEditStage().getPictureBankStage()
                    .getImageURI(newValue);
            if(imageURI != null)
            {
                fileImageView.setImage(new Image(imageURI.toString()));
            }
            else
            {
                fileImageView.setImage(null);
            }
        });

        selectImageFileButton.setOnMouseClicked(mouseEvent ->
        {
            var pictureBank = super.getSetupStage().getControlMainPage().getMainEditStage().getPictureBankStage();
            pictureBank.startImageSelection(file -> imageNameTextField.setText(file.getName()));
        });

        borderWidthTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));

        var stretchToggleButtonGraphic = stretchToggleButton.getGraphic();
        if (stretchToggleButtonGraphic instanceof ImageView)
        {
            ((ImageView) stretchToggleButtonGraphic)
                    .setImage(new Image(Util.getResource("images/stretch_icon.png").toExternalForm()));
        }
        stretchToggleButton.setBackground(Background.EMPTY);
        stretchToggleButton.selectedProperty().addListener((observableValue, oldValue, newValue) ->
        {
            Background background = Background.EMPTY;
            if(newValue)
            {
                background = new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY));
            }
            stretchToggleButton.setBackground(background);
        });

        super.getAttributeChangerList().create(imageNameTextField.textProperty(), BackgroundAttribute.PICTURE_BANK_IMAGE_NAME)
                .create(backgroundColorPicker.valueProperty(), BackgroundAttribute.BACKGROUND_COLOR)
                .create(borderColorPicker.valueProperty(), BackgroundAttribute.BORDER_COLOR)
                .createStringToNumber(borderWidthTextField.textProperty(), BackgroundAttribute.BORDER_WIDTH, Util::parseIntOrZero)
                .create(stretchToggleButton.selectedProperty(), BackgroundAttribute.STRETCH_IMAGE);

        super.computeProperties();
        //Hide the select file while selecting multiples
        super.getSetupStage().getSelectAndMultipleWrite()
                .onSelectingMultiplesChangeListener(selectMultiples -> selectImageFileButton.setVisible(!selectMultiples));
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        borderWidthTextField.setText("1");
        borderColorPicker.setValue(Color.BLACK);
    }

    @Override
    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasAttribute(BackgroundAttribute.class);
    }

}
