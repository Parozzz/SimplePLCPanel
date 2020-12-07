package parozzz.github.com.hmi.controls.controlwrapper.setup.quicktext;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.FontSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.Objects;

class QuickTextEditorStatePane extends FXObject
{
    @FXML private Label stateNameLabel;
    @FXML private TextArea textArea;
    @FXML private ColorPicker textColorPicker;
    @FXML private ComboBox<Integer> textSizeComboBox;
    @FXML private ToggleButton boldToggleButton;
    @FXML private ToggleButton italicToggleButton;
    @FXML private ToggleButton underlineToggleButton;

    private final ControlWrapperQuickTextEditorStage editorStage;
    private final WrapperState wrapperState;
    private final HBox hBox;

    public QuickTextEditorStatePane(ControlWrapperQuickTextEditorStage editorStage,
            WrapperState wrapperState) throws IOException
    {
        super("QuickTextEditorStatePane");

        this.editorStage = editorStage;
        this.wrapperState = wrapperState;

        hBox = (HBox) FXUtil.loadFXML("setup/quicktext/quickTextStatePane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        this.refreshValues();

        textArea.textProperty().addListener((observable, oldValue, newValue) ->
                this.setToTextAttribute(TextAttribute.TEXT, newValue)
        );

        textColorPicker.valueProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.TEXT_COLOR, newValue)
        );

        textSizeComboBox.setConverter(new IntegerStringConverter());
        textSizeComboBox.getItems().addAll(FontSetupPane.TEXT_SIZE_DEFAULT_CHOICE);
        textSizeComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.FONT_TEXT_SIZE, newValue)
        );

        boldToggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.BOLD_WEIGHT, newValue)
        );

        italicToggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.ITALIC_POSTURE, newValue)
        );

        underlineToggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.UNDERLINE, newValue)
        );
    }

    public void refreshValues()
    {
        stateNameLabel.setText(wrapperState.getStringVersion());
        textArea.setText(this.getFromTextAttribute(TextAttribute.TEXT));
        textColorPicker.setValue(this.getFromFontAttribute(FontAttribute.TEXT_COLOR));
        textSizeComboBox.setValue(this.getFromFontAttribute(FontAttribute.FONT_TEXT_SIZE));
        boldToggleButton.setSelected(this.getFromFontAttribute(FontAttribute.BOLD_WEIGHT));
        italicToggleButton.setSelected(this.getFromFontAttribute(FontAttribute.ITALIC_POSTURE));
        underlineToggleButton.setSelected(this.getFromFontAttribute(FontAttribute.UNDERLINE));
    }

    public Parent getParent()
    {
        return hBox;
    }

    private <T> T getFromTextAttribute(AttributeProperty<T> attributeProperty)
    {
        var attribute = wrapperState.getAttributeMap().get(AttributeType.TEXT);
        return Objects.requireNonNull(attribute).getValue(attributeProperty);
    }

    private <T> void setToTextAttribute(AttributeProperty<T> attributeProperty, T value)
    {
        if (value == null)
        {
            return;
        }

        var attribute = wrapperState.getAttributeMap().get(AttributeType.TEXT);
        Objects.requireNonNull(attribute).setValue(attributeProperty, value);
    }

    private <T> T getFromFontAttribute(AttributeProperty<T> attributeProperty)
    {
        var attribute = wrapperState.getAttributeMap().get(AttributeType.FONT);
        return Objects.requireNonNull(attribute).getValue(attributeProperty);
    }

    private <T> void setToFontAttribute(AttributeProperty<T> attributeProperty, T value)
    {
        if (value == null)
        {
            return;
        }

        var attribute = wrapperState.getAttributeMap().get(AttributeType.FONT);
        Objects.requireNonNull(attribute).setValue(attributeProperty, value);
    }
}
