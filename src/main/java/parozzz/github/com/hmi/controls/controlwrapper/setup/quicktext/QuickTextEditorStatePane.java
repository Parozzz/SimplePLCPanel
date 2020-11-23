package parozzz.github.com.hmi.controls.controlwrapper.setup.quicktext;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.FontSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.Objects;

public class QuickTextEditorStatePane extends FXObject
{
    @FXML
    private Label stateNameLabel;
    @FXML
    private TextArea textArea;
    @FXML
    private ColorPicker textColorPicker;
    @FXML
    private ComboBox<Integer> textSizeComboBox;
    @FXML
    private ToggleButton boldToggleButton;
    @FXML
    private ToggleButton italicToggleButton;
    @FXML
    private ToggleButton underlineToggleButton;

    private final WrapperState wrapperState;
    private final HBox hBox;

    public QuickTextEditorStatePane(WrapperState wrapperState) throws IOException
    {
        super("QuickTextEditorStatePane");

        this.wrapperState = wrapperState;

        hBox = (HBox) FXUtil.loadFXML("setupv2/quicktext/quickTextStatePane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        stateNameLabel.setText(wrapperState.getStringVersion());

        textArea.setText(this.getFromTextAttribute(TextAttribute.TEXT));
        textArea.textProperty().addListener((observable, oldValue, newValue) ->
                this.setToTextAttribute(TextAttribute.TEXT, newValue)
        );

        textColorPicker.setValue(this.getFromFontAttribute(FontAttribute.TEXT_COLOR));
        textColorPicker.valueProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.TEXT_COLOR, newValue)
        );

        textSizeComboBox.setConverter(new IntegerStringConverter());
        textSizeComboBox.getItems().addAll(FontSetupPane.SIZE_DEFAULT_CHOICE);
        textSizeComboBox.setValue(this.getFromFontAttribute(FontAttribute.FONT_TEXT_SIZE));
        textSizeComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.FONT_TEXT_SIZE, newValue)
        );

        boldToggleButton.setSelected(this.getFromFontAttribute(FontAttribute.BOLD_WEIGHT));
        boldToggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.BOLD_WEIGHT, newValue)
        );

        italicToggleButton.setSelected(this.getFromFontAttribute(FontAttribute.ITALIC_POSTURE));
        italicToggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.ITALIC_POSTURE, newValue)
        );

        underlineToggleButton.setSelected(this.getFromFontAttribute(FontAttribute.UNDERLINE));
        underlineToggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                this.setToFontAttribute(FontAttribute.UNDERLINE, newValue)
        );
    }

    public Parent getParent()
    {
        return hBox;
    }

    private <T> T getFromTextAttribute(AttributeProperty<T> attributeProperty)
    {
        var textAttribute = AttributeFetcher.fetch(wrapperState, TextAttribute.class);
        Objects.requireNonNull(textAttribute);
        return textAttribute.getValue(attributeProperty);

    }

    private <T> void setToTextAttribute(AttributeProperty<T> attributeProperty, T value)
    {
        if(value == null)
        {
            return;
        }

        var textAttribute = AttributeFetcher.fetch(wrapperState, TextAttribute.class);
        if(textAttribute != null)
        {
            textAttribute.setValue(attributeProperty, value);
        }
    }

    private <T> T getFromFontAttribute(AttributeProperty<T> attributeProperty)
    {
        var fontAttribute = AttributeFetcher.fetch(wrapperState, FontAttribute.class);
        Objects.requireNonNull(fontAttribute);
        return fontAttribute.getValue(attributeProperty);
    }

    private <T> void setToFontAttribute(AttributeProperty<T> attributeProperty, T value)
    {
        if(value == null)
        {
            return;
        }

        var fontAttribute = AttributeFetcher.fetch(wrapperState, FontAttribute.class);
        if(fontAttribute != null)
        {
            fontAttribute.setValue(attributeProperty, value);
        }
    }
}
