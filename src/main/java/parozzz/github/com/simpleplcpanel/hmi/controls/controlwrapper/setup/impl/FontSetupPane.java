package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.converter.IntegerStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;
import java.util.List;

public final class FontSetupPane extends SetupPane<FontAttribute>
{
    public final static List<Integer> TEXT_SIZE_DEFAULT_CHOICE = List.of(1, 2, 4, 6, 8, 10, 12, 16, 20, 24, 28, 32, 36, 42, 48, 54, 60, 70, 80);

    @FXML private Label frontLabel;

    @FXML private ColorPicker textColorPicker;
    @FXML private ToggleButton boldTextStyleButton;
    @FXML private ToggleButton italicTextStyleButton;
    @FXML private ToggleButton underlineButton;
    @FXML private ToggleButton strikethroughButton;

    @FXML private ComboBox<Integer> textSizeComboBox;

    @FXML private ComboBox<String> fontComboBox;
    @FXML private ChoiceBox<Pos> textPositionChoiceBox;

    private final VBox vBox;

    public FontSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "FontSetupPane", AttributeType.FONT);

        this.vBox = (VBox) FXUtil.loadFXML("setup/fontSetupPane.fxml", this);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        textSizeComboBox.setConverter(new IntegerStringConverter());
        textSizeComboBox.getItems().addAll(FontSetupPane.TEXT_SIZE_DEFAULT_CHOICE);
        textSizeComboBox.getEditor().setTextFormatter(FXTextFormatterUtil.simpleInteger(2));

        fontComboBox.getItems().addAll(Font.getFontNames());
        fontComboBox.addEventFilter(KeyEvent.KEY_TYPED, keyEvent ->
        {
            if (!fontComboBox.isShowing())
            {
                return;
            }

            int index = 0;

            //This allow to move the cursor inside the ListView to the first of the type char
            var keyChar = keyEvent.getCharacter().toLowerCase();
            for (var item : fontComboBox.getItems())
            {
                if (item.toLowerCase().startsWith(keyChar))
                {
                    var skin = fontComboBox.getSkin();
                    if (skin instanceof ComboBoxListViewSkin)
                    {
                        var displayNode = ((ComboBoxListViewSkin<String>) skin).getDisplayNode();
                        if (displayNode instanceof ListCell)
                        {
                            var listView = ((ListCell<String>) displayNode).getListView();
                            listView.scrollTo(index);
                            listView.getFocusModel().focus(index);
                        }
                    }

                    return;
                }

                index++;
            }
        });

        textPositionChoiceBox.setConverter(new EnumStringConverter<>(Pos.class).setCapitalize());
        textPositionChoiceBox.getItems().addAll(Pos.values());


        super.getAttributeChangerList().create(textColorPicker.valueProperty(), FontAttribute.COLOR)
                .create(textPositionChoiceBox.valueProperty(), FontAttribute.POSITION)
                .create(boldTextStyleButton.selectedProperty(), FontAttribute.BOLD)
                .create(italicTextStyleButton.selectedProperty(), FontAttribute.ITALIC)
                .create(underlineButton.selectedProperty(), FontAttribute.UNDERLINE)
                .create(strikethroughButton.selectedProperty(), FontAttribute.STRIKETHROUGH)
                .create(textSizeComboBox.valueProperty(), FontAttribute.TEXT_SIZE)
                .create(fontComboBox.valueProperty(), FontAttribute.FONT_NAME);

        super.computeProperties();
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }
}
