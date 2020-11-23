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

public final class TextSetupPane extends SetupPane<TextAttribute>
{
    private static final List<Integer> DEFAULT_LINE_SPACING = List.of(30, 15, 10, 5, 0, -5, -10, -15, -30);

    @FXML private Button appendValuePlaceholderButton;
    @FXML private TextArea textArea;

    @FXML private ChoiceBox<TextAlignment> newLineAlignmentChoiceBox;
    @FXML private ComboBox<Integer> newLineSpacingComboBox;

    private final VBox mainVBox;

    public TextSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "TextSetupPane", "Custom Text", TextAttribute.class);

        mainVBox = (VBox) FXUtil.loadFXML("setupv2/textSetupPaneV2.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        appendValuePlaceholderButton.setOnAction(actionEvent -> textArea.setText(textArea.getText() + ControlWrapper.VALUE_PLACEHOLDER));

        newLineAlignmentChoiceBox.setConverter(new EnumStringConverter<>(TextAlignment.class).setCapitalize());
        newLineAlignmentChoiceBox.getItems().addAll(TextAlignment.values());

        newLineSpacingComboBox.getStylesheets().add("stylesheets/combo_box_setup_style.css");
        newLineSpacingComboBox.setConverter(new IntegerStringConverter());
        newLineSpacingComboBox.getItems().addAll(DEFAULT_LINE_SPACING);
        newLineSpacingComboBox.setValue(0);
        newLineSpacingComboBox.getEditor().setTextFormatter(FXTextFormatterUtil.simpleInteger(2));

        super.getAttributeChangerList().create(textArea.textProperty(), TextAttribute.TEXT)
                .create(newLineAlignmentChoiceBox.valueProperty(), TextAttribute.TEXT_ALIGNMENT)
                .create(newLineSpacingComboBox.valueProperty(), TextAttribute.LINE_SPACING);

        super.computeProperties();

        //super.getSetupStage().getSelectAndMultipleWrite()
        //        .onSelectingMultiplesChangeListener(selectMultiples -> appendValuePlaceholderButton.setVisible(!selectMultiples));
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        var undoRedoManager = super.getSetupStage().getUndoRedoManager();

        undoRedoManager.setIgnoreNew(true);
        newLineAlignmentChoiceBox.getSelectionModel().selectFirst();
        undoRedoManager.setIgnoreNew(false);
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }

    @Override
    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasAttribute(TextAttribute.class);
    }
}
