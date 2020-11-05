package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.converter.IntegerStringConverter;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.BaseAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class BaseSetupPane extends SetupPane<BaseAttribute>
{
    private final static List<Integer> SIZE_DEFAULT_CHOICE = List.of(1, 2, 4, 6, 8, 10, 12, 16, 20, 24, 28, 32, 36, 42, 48, 54, 60, 70, 80);

    @FXML private Label frontLabel;

    @FXML private ColorPicker textColorPicker;
    @FXML private ToggleButton boldTextStyleButton;
    @FXML private ToggleButton italicTextStyleButton;
    @FXML private ToggleButton underlineButton;

    @FXML private ComboBox<Integer> textSizeComboBox;

    @FXML private ComboBox<String> fontComboBox;
    @FXML private ChoiceBox<Pos> textPositionChoiceBox;

    @FXML private CheckBox adaptCheckbox;
    @FXML private TextField widthTextField;
    @FXML private TextField heightTextField;

    private final VBox mainVBox;

    public BaseSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "BaseSetupPane", "Base", BaseAttribute.class);

        this.mainVBox = (VBox) FXUtil.loadFXML("setup/baseSetupVBox.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        textSizeComboBox.setConverter(new IntegerStringConverter());
        textSizeComboBox.getItems().addAll(BaseSetupPane.SIZE_DEFAULT_CHOICE);
        var editor = textSizeComboBox.getEditor();
        if (editor == null)
        {
            Logger.getLogger(TextSetupPane.class.getSimpleName()).log(Level.WARNING, "TextSizeComboBox editor is null");
        } else
        {
            editor.setTextFormatter(FXTextFormatterUtil.simpleInteger(2));
        }

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

        widthTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));
        heightTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));

        super.getAttributeChangerList().create(textColorPicker.valueProperty(), BaseAttribute.TEXT_COLOR)
                .create(textPositionChoiceBox.valueProperty(), BaseAttribute.TEXT_POSITION)
                .create(boldTextStyleButton.selectedProperty(), BaseAttribute.BOLD_WEIGHT)
                .create(italicTextStyleButton.selectedProperty(), BaseAttribute.ITALIC_POSTURE)
                .create(underlineButton.selectedProperty(), BaseAttribute.UNDERLINE)
                .create(textSizeComboBox.valueProperty(), BaseAttribute.FONT_TEXT_SIZE)
                .create(fontComboBox.valueProperty(), BaseAttribute.FONT_NAME)
                .create(adaptCheckbox.selectedProperty(), BaseAttribute.ADAPT)
                .createStringToNumber(widthTextField.textProperty(), BaseAttribute.WIDTH, Util::parseIntOrZero)
                .createStringToNumber(heightTextField.textProperty(), BaseAttribute.HEIGHT, Util::parseIntOrZero);

        super.computeProperties();
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        textColorPicker.setValue(Color.BLACK);
        textSizeComboBox.setValue(12);

        fontComboBox.getSelectionModel().selectFirst();
        textPositionChoiceBox.getSelectionModel().selectFirst();

        widthTextField.setText("120");
        heightTextField.setText("80");
    }


    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }

    @Override
    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasAttribute(BaseAttribute.class);
    }

    /*
    @Override
    protected void setData(BaseAttribute attribute)
    {
        attribute.textColorProperty = textColorPicker.getValue();
        attribute.underlineProperty = underlineButton.isSelected();

        attribute.fontNameProperty = fontChoiceBox.getSelectionModel().getSelectedItem();
        attribute.fontWeightProperty = boldTextStyleButton.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
        attribute.fontPostureProperty = italicTextStyleButton.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;
        attribute.fontTextSizeProperty = this.getTextSize();

        attribute.shapeTypeProperty = shapeChoiceBox.getValue();

        attribute.adaptProperty = adaptCheckbox.isSelected();
        attribute.widthProperty = Integer.parseInt(widthTextField.getText());
        attribute.heightProperty = Integer.parseInt(heightTextField.getText());
    }

    @Override
    protected void loadData(BaseAttribute attribute)
    {
        underlineButton.setSelected(attribute.underlineProperty);
        textColorPicker.setValue(attribute.textColorProperty);

        var index = fontChoiceBox.getItems().indexOf(attribute.fontNameProperty);
        if (index != -1)
        {
            fontChoiceBox.getSelectionModel().select(index);
        }

        boldTextStyleButton.setSelected(attribute.fontWeightProperty == FontWeight.BOLD);
        italicTextStyleButton.setSelected(attribute.fontPostureProperty == FontPosture.ITALIC);
        textSizeComboBox.setValue((int) attribute.fontTextSizeProperty);

        shapeChoiceBox.setValue(attribute.shapeTypeProperty);

        adaptCheckbox.setSelected(attribute.adaptProperty);
        widthTextField.setText("" + (int) attribute.widthProperty);
        heightTextField.setText("" + (int) attribute.heightProperty);
    }*/
}
