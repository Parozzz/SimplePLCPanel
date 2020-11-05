package parozzz.github.com.hmi.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.database.ControlContainerDatabase;
import parozzz.github.com.hmi.page.PaneHMIStage;

import java.io.IOException;

public class ControlContainerCreationStage extends PaneHMIStage<VBox>
{
    @FXML private AnchorPane nameAnchorPane;
    @FXML private TextField nameTextField;

    @FXML private ColorPicker backgroundColorPicker;
    @FXML private TextField imageNameTextField;
    @FXML private Button selectImageButton;

    @FXML private AnchorPane createButtonAnchorPane;
    @FXML private Button createButton;

    private final ControlContainerDatabase controlContainerDatabase;

    public ControlContainerCreationStage(ControlContainerDatabase controlContainerDatabase) throws IOException
    {
        super("PageCreation", "controlContainerCreatePane.fxml", VBox.class);

        this.controlContainerDatabase = controlContainerDatabase;
    }

    public void reset()
    {
        backgroundColorPicker.setValue(Color.WHITE);
        nameTextField.setText("");
        imageNameTextField.setText("");
    }

    @Override
    public void setup()
    {
        super.setup();

        var stageSetter = super.getStageSetter()
                .setOnWindowCloseRequest(windowEvent -> this.reset());

        this.reset();

        selectImageButton.setOnAction(actionEvent ->
        {
            var pictureBank = controlContainerDatabase.getMainEditStage().getPictureBankStage();
            pictureBank.startImageSelection(file -> imageNameTextField.setText(file.getName()));
        });

        createButton.setOnAction(event ->
        {
            var name = nameTextField.getText();
            if (name.isEmpty() || controlContainerDatabase.doNameExists(name))
            {
                var alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error while adding new page.");
                alert.setContentText("A page with that name already exists");
                alert.show();
                return;
            }

            var controlsPage = controlContainerDatabase.create(name);
            if (controlsPage != null)
            {
                controlsPage.setBackgroundColor(backgroundColorPicker.getValue());
                stageSetter.close(); //If everything is ok, go BACK!

                var pictureName = imageNameTextField.getText();
                if(pictureName != null && !pictureName.isEmpty())
                {
                    controlsPage.setBackgroundPictureName(pictureName);
                }
            }
        });
    }
}
