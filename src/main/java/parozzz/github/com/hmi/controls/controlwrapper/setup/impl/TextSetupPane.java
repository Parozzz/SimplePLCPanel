package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.converter.IntegerStringConverter;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TextSetupPane extends SetupPane<TextAttribute>
{
    private static final List<Integer> DEFAULT_LINE_SPACING = List.of(30, 15, 5, 0, -5, -15, -30);

    @FXML private Button appendValuePlaceholderButton;
    @FXML private TextArea textArea;

    @FXML private ChoiceBox<TextAlignment> textAlignmentChoiceBox;
    @FXML private ComboBox<Integer> lineSpacingComboBox;

    private final VBox mainVBox;

    public TextSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "TextSetupPane", "Text", TextAttribute.class);

        mainVBox = (VBox) FXUtil.loadFXML("setup/textSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        appendValuePlaceholderButton.setOnAction(actionEvent -> textArea.setText(textArea.getText() + ControlWrapper.VALUE_PLACEHOLDER));

        textAlignmentChoiceBox.setConverter(new EnumStringConverter<>(TextAlignment.class).setCapitalize());
        textAlignmentChoiceBox.getItems().addAll(TextAlignment.values());

        lineSpacingComboBox.setConverter(new IntegerStringConverter());
        lineSpacingComboBox.getItems().addAll(DEFAULT_LINE_SPACING);
        lineSpacingComboBox.setValue(0);
        var editor = lineSpacingComboBox.getEditor();
        if (editor == null)
        {
            Logger.getLogger(TextSetupPane.class.getSimpleName()).log(Level.WARNING, "LineSpacingComboBox editor is null");
        } else
        {
            editor.setTextFormatter(FXTextFormatterUtil.simpleInteger(2));
        }

        super.getAttributeChangerList().create(textArea.textProperty(), TextAttribute.TEXT)
                .create(textAlignmentChoiceBox.valueProperty(), TextAttribute.TEXT_ALIGNMENT)
                .create(lineSpacingComboBox.valueProperty(), TextAttribute.LINE_SPACING);

        super.computeProperties();

        super.getSetupStage().getSelectAndMultipleWrite()
                .onSelectingMultiplesChangeListener(selectMultiples -> appendValuePlaceholderButton.setVisible(!selectMultiples));
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        var undoRedoManager = super.getSetupStage().getUndoRedoManager();

        undoRedoManager.setIgnoreNew(true);
        textAlignmentChoiceBox.getSelectionModel().selectFirst();
        undoRedoManager.setIgnoreNew(false);
    }

    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }

    @Override
    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasAttribute(TextAttribute.class);
    }
}
