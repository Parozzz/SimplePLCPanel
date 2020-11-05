package parozzz.github.com.hmi.controls.controlwrapper.setup;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.util.Util;

import java.util.Objects;

@SuppressWarnings("unused")
class StateCreationPane extends FXObject
{
    private ChoiceBox<WrapperState.Type> stateCreationSelectChoiceBox;
    private Label stateTypeLabel;
    private TextField lowerStateValueTextField;
    private TextField higherStateValueTextField;
    private Button addStateCreationButton;
    private Button closeStateCreationButton;

    private final ControlWrapperSetupStage setupPage;
    private final AnchorPane anchorPane;

    public StateCreationPane(ControlWrapperSetupStage setupPage, AnchorPane anchorPane)
    {
        super("StateCreationPane");

        this.setupPage = setupPage;
        this.anchorPane = anchorPane;

        new SetupInjector(StateCreationPane.class, this)
                .addAll(anchorPane)
                .inject();
    }

    @Override
    public void setup()
    {
        super.setup();

        anchorPane.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            if(anchorPane.isVisible() && keyEvent.getCode() == KeyCode.ENTER)
            {
                this.createState();
            }
        });

        //On visibility reset both the text fields to avoid old values remaining there
        anchorPane.visibleProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if(newValue != null && newValue)
            {
                lowerStateValueTextField.setText("");
                higherStateValueTextField.setText("");
            }
        });

        anchorPane.setVisible(false);
        closeStateCreationButton.setOnAction(actionEvent -> anchorPane.setVisible(false));

        lowerStateValueTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        higherStateValueTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));

        stateCreationSelectChoiceBox.setConverter(new EnumStringConverter<>(WrapperState.Type.class));
        stateCreationSelectChoiceBox.getItems().addAll(WrapperState.Type.values());
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
        stateCreationSelectChoiceBox.setValue(WrapperState.Type.EQUAL);

        addStateCreationButton.setOnAction(actionEvent -> this.createState());
    }

    private void createState()
    {
        WrapperState wrapperState;

        var type = stateCreationSelectChoiceBox.getValue();
        if(type.isSingleCompare())
        {
            wrapperState = type.create(this.getHigherState());
        }
        else
        {
            wrapperState = type.create(this.getLowerState(), this.getHigherState());
        }

        if(wrapperState == null) //It means the values are incorrect
        {
            return;
        }

        var selectedControlWrapper = setupPage.getSelectedControlWrapper();
        Objects.requireNonNull(selectedControlWrapper, "Trying to add new state but the SelectedControlWrapper is null");

        selectedControlWrapper.getStateMap().addState(wrapperState);
        setupPage.updateStateSelectionStates();

        anchorPane.setVisible(false);
    }

    private int getLowerState()
    {
        return Util.parseInt(lowerStateValueTextField.getText(), 0);
    }

    private int getHigherState()
    {
        return Util.parseInt(higherStateValueTextField.getText(), 0);
    }
}
