package parozzz.github.com.hmi.controls.controlwrapper.setup;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("unused")
class WrapperStateCreationPane extends FXObject implements SetupSelectable
{

    @FXML private TextField lowerValueTextField;
    @FXML private TextField higherValueTextField;
    @FXML private ChoiceBox<WrapperState.CompareType> firstCompareChoiceBox;
    @FXML private ChoiceBox<WrapperState.CompareType> secondCompareChoiceBox;

    @FXML private JFXButton createStateButton;

    private final ControlWrapperSetupStage setupStage;
    private final Button selectButton;
    private final VBox vBox;

    public WrapperStateCreationPane(ControlWrapperSetupStage setupStage, Button selectButton) throws IOException
    {
        super("StateCreationPane");

        this.setupStage = setupStage;
        this.selectButton = selectButton;

        vBox = (VBox) FXUtil.loadFXML("setup/wrapperStateCreationPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        /*vBox.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            if (keyEvent.getCode() == KeyCode.ENTER)
            {
                MouseInfo.getPointerInfo().getLocation();
                this.createState();
            }
        });*/

        selectButton.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        selectButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        selectButton.setUserData(this);
        selectButton.setOnAction(actionEvent -> setupStage.setShownSelectable(this));

        lowerValueTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        higherValueTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));

        firstCompareChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState.CompareType::getVisualText));
        firstCompareChoiceBox.getItems().addAll(
                WrapperState.CompareType.HIGHER, WrapperState.CompareType.HIGHER_EQUAL
        );
        firstCompareChoiceBox.setValue(WrapperState.CompareType.HIGHER);

        secondCompareChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState.CompareType::getVisualText));
        secondCompareChoiceBox.getItems().addAll(
                WrapperState.CompareType.EQUAL,
                WrapperState.CompareType.LOWER, WrapperState.CompareType.LOWER_EQUAL
        );
        secondCompareChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue == WrapperState.CompareType.EQUAL)
            {
                firstCompareChoiceBox.setValue(WrapperState.CompareType.ALWAYS_TRUE);
                firstCompareChoiceBox.setVisible(false);

                lowerValueTextField.setText("");
                lowerValueTextField.setVisible(false);
            } else
            {
                firstCompareChoiceBox.setVisible(true);
                lowerValueTextField.setVisible(true);
            }
        });
        secondCompareChoiceBox.setValue(WrapperState.CompareType.LOWER);

        createStateButton.addEventFilter(MouseEvent.MOUSE_PRESSED, this::createState);
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public Button getSelectButton()
    {
        return selectButton;
    }

    public void reset()
    {
        lowerValueTextField.setText("");
        higherValueTextField.setText("");

        firstCompareChoiceBox.setValue(WrapperState.CompareType.ALWAYS_TRUE);
        secondCompareChoiceBox.setValue(WrapperState.CompareType.ALWAYS_TRUE);
    }

    private void createState(MouseEvent mouseEvent)
    {
        var firstCompareType = firstCompareChoiceBox.getValue();
        var invalidFirstCompare = firstCompareType == null || firstCompareType == WrapperState.CompareType.ALWAYS_TRUE;

        var secondCompareType = secondCompareChoiceBox.getValue();
        var invalidSecondCompare = secondCompareType == null || secondCompareType == WrapperState.CompareType.ALWAYS_TRUE;

        if (invalidFirstCompare && invalidSecondCompare)
        {
            this.createAndShowInvalidTooltip(mouseEvent, "Invalid comparison selection");
            return;
        }

        var wrapperStateBuilder = WrapperState.builder();

        if (!invalidFirstCompare)
        {
            try
            {
                var firstCompare = Integer.parseInt(lowerValueTextField.getText());
                wrapperStateBuilder.firstCompare(firstCompareType, firstCompare);
            } catch (NumberFormatException exception)
            {
                this.createAndShowInvalidTooltip(mouseEvent,"Invalid comparison selection");
                return;
            }
        }

        if (!invalidSecondCompare)
        {
            try
            {
                var secondCompare = Integer.parseInt(higherValueTextField.getText());
                wrapperStateBuilder.secondCompare(secondCompareType, secondCompare);
            } catch (NumberFormatException exception)
            {
                this.createAndShowInvalidTooltip(mouseEvent,"Invalid comparison selection");
                return;
            }
        }

        var wrapperState = wrapperStateBuilder.create();

        var selectedControlWrapper = setupStage.getSelectedControlWrapper();
        Objects.requireNonNull(selectedControlWrapper, "Trying to add new state but the SelectedControlWrapper is null");

        var stateMap = selectedControlWrapper.getStateMap();
        if(stateMap.contains(wrapperState))
        {
            this.createAndShowInvalidTooltip(mouseEvent, "State duplicate");
            return;
        }

        createStateButton.setRipplerFill(Color.DARKGREEN);

        stateMap.addState(wrapperState);
        setupStage.updateStateSelectionBox();
    }

    private void createAndShowInvalidTooltip(MouseEvent mouseEvent, String text)
    {
        var label = new Label(text);
        label.setFont(Font.font(12));
        label.setTextFill(Color.RED);

        var tooltip = new Tooltip();
        tooltip.setAutoHide(true);
        tooltip.setGraphic(label);
        tooltip.show(createStateButton, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        tooltip.addEventFilter(MouseEvent.MOUSE_EXITED, event -> tooltip.hide());

        createStateButton.setRipplerFill(Color.DARKRED);
    }
}
