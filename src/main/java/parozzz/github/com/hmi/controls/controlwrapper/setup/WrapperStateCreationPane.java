package parozzz.github.com.hmi.controls.controlwrapper.setup;

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
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("unused")
class WrapperStateCreationPane extends FXObject implements SetupButtonSelectable
{

    @FXML private TextField lowerValueTextField;
    @FXML private TextField higherValueTextField;
    @FXML private ChoiceBox<WrapperState.CompareType> firstCompareChoiceBox;
    @FXML private ChoiceBox<WrapperState.CompareType> secondCompareChoiceBox;

    @FXML private Button createStateButton;

    private final ControlWrapperSetupStage setupPage;
    private final Button selectButton;
    private final VBox vBox;

    public WrapperStateCreationPane(ControlWrapperSetupStage setupPage, Button selectButton) throws IOException
    {
        super("StateCreationPane");

        this.setupPage = setupPage;
        this.selectButton = selectButton;

        vBox = (VBox) FXUtil.loadFXML("setupv2/wrapperStateCreationPaneV2.fxml", this);
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

        selectButton.setOnAction(actionEvent -> setupPage.showSelectable(this));

        lowerValueTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        higherValueTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));

        firstCompareChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState.CompareType::getVisualText));
        firstCompareChoiceBox.getItems().addAll(
                WrapperState.CompareType.ALWAYS_TRUE,
                WrapperState.CompareType.HIGHER, WrapperState.CompareType.HIGHER_EQUAL
        );

        secondCompareChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState.CompareType::getVisualText));
        secondCompareChoiceBox.getItems().addAll(
                WrapperState.CompareType.ALWAYS_TRUE, WrapperState.CompareType.EQUAL,
                WrapperState.CompareType.LOWER, WrapperState.CompareType.LOWER_EQUAL
        );

        createStateButton.setOnMouseClicked(this::createState);
/*

        //On visibility reset both the text fields to avoid old values remaining there
        anchorPane.visibleProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue != null && newValue)
            {
                lowerStateValueTextField.setText("");
                higherStateValueTextField.setText("");
            }
        });

        anchorPane.setVisible(false);
        closeStateCreationButton.setOnAction(actionEvent -> anchorPane.setVisible(false));

        lowerStateValueTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        higherStateValueTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));

        stateCreationSelectChoiceBox.setConverter(new EnumStringConverter<>(WrapperState.CompareType.class));
        stateCreationSelectChoiceBox.getItems().addAll(WrapperState.CompareType.values());
        stateCreationSelectChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue != null)
            {
                stateTypeLabel.setText(newValue.getTextWithoutPlaceholders());
                switch (newValue)
                {
                    case EQUAL:
                    case LOWER:
                    case LOWER_EQUAL:
                    case HIGHER:
                    case HIGHER_EQUAL:
                        stateTypeLabel.setAlignment(Pos.CENTER_RIGHT);
                        lowerStateValueTextField.setVisible(false);
                        break;
                    case BETWEEN:
                    case BETWEEN_EQUAL:
                        stateTypeLabel.setAlignment(Pos.CENTER);
                        lowerStateValueTextField.setVisible(true);
                        break;
                }
            }
        });
        stateCreationSelectChoiceBox.setValue(WrapperState.CompareType.EQUAL);

        addStateCreationButton.setOnAction(actionEvent -> this.createState());*/
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
            this.createAndShowInvalidTooltip(mouseEvent);
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
                this.createAndShowInvalidTooltip(mouseEvent);
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
                this.createAndShowInvalidTooltip(mouseEvent);
                return;
            }
        }

        var wrapperState = wrapperStateBuilder.create();

        var selectedControlWrapper = setupPage.getSelectedControlWrapper();
        Objects.requireNonNull(selectedControlWrapper, "Trying to add new state but the SelectedControlWrapper is null");

        selectedControlWrapper.getStateMap().addState(wrapperState);
        setupPage.updateStateSelectionBox();
    }

    private void createAndShowInvalidTooltip(MouseEvent mouseEvent)
    {
        var label = new Label("Invalid comparison selection");
        label.setFont(Font.font(14));
        label.setTextFill(Color.RED);

        var tooltip = new Tooltip();
        tooltip.setAutoHide(true);
        tooltip.setGraphic(label);
        tooltip.show(createStateButton, mouseEvent.getScreenX(), mouseEvent.getScreenY());
    }
}
