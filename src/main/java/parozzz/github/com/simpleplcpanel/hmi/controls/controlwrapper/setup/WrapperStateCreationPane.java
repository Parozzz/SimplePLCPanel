package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.others.TimedControlColorHandler;
import parozzz.github.com.simpleplcpanel.util.functionalinterface.primitives.BooleanConsumer;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class WrapperStateCreationPane extends FXObject
{

    @FXML private TextField lowerValueTextField;
    @FXML private TextField higherValueTextField;
    @FXML private ChoiceBox<WrapperState.CompareType> firstCompareChoiceBox;
    @FXML private ChoiceBox<WrapperState.CompareType> secondCompareChoiceBox;

    @FXML private Button createStateButton;

    private final ControlWrapperSetupStage setupStage;
    private final VBox vBox;
    private final TimedControlColorHandler createStateButtonColorHandler;
    private final Runnable successfulCreationRunnable;

    public WrapperStateCreationPane(ControlWrapperSetupStage setupStage, Runnable successfulCreationRunnable) throws IOException
    {
        this.setupStage = setupStage;

        vBox = (VBox) FXUtil.loadFXML("setup/wrapperStateCreationPane.fxml", this);

        this.createStateButtonColorHandler = new TimedControlColorHandler(createStateButton);
        this.successfulCreationRunnable = successfulCreationRunnable;
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        /*vBox.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            if (keyEvent.getCode() == KeyCode.ENTER)
            {
                MouseInfo.getPointerInfo().getLocation();
                this.createState();
            }
        });*/

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

    public Parent getParent()
    {
        return vBox;
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
        var selectedControlWrapper = setupStage.getSelectedControlWrapper();
        Objects.requireNonNull(selectedControlWrapper, "Trying to add new state but the SelectedControlWrapper is null");

        var firstCompareType = firstCompareChoiceBox.getValue();
        var invalidFirstCompare = firstCompareType == null || firstCompareType == WrapperState.CompareType.ALWAYS_TRUE;

        var secondCompareType = secondCompareChoiceBox.getValue();
        var invalidSecondCompare = secondCompareType == null || secondCompareType == WrapperState.CompareType.ALWAYS_TRUE;

        if (invalidFirstCompare && invalidSecondCompare)
        {
            this.createAndShowInvalidTooltip(mouseEvent, "Invalid comparison selection");
            return;
        }

        var wrapperStateBuilder = selectedControlWrapper.getStateMap().stateBuilder();
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
        if(wrapperState == null)
        {
            this.createAndShowInvalidTooltip(mouseEvent, "State duplicate");
            return;
        }

        setupStage.getStateListView().loadStates();

        createStateButtonColorHandler.setBackground(FXUtil.createBackground(Color.DARKGREEN), 1000);
        successfulCreationRunnable.run();
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

        createStateButtonColorHandler.setBackground(FXUtil.createBackground(Color.DARKRED), 1000);
    }
}
