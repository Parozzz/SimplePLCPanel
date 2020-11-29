package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.control;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import parozzz.github.com.hmi.attribute.impl.control.InputDataAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.impl.textinput.InputWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;

public class InputDataSetupPane extends SetupPane<InputDataAttribute>
{
    private static void parseSpinnerEditor(Spinner<?> spinner)
    {
        var editor = spinner.getEditor();
        editor.setAlignment(Pos.CENTER);
        editor.setFont(Font.font(17));
    }

    @FXML private ChoiceBox<InputWrapper.Type> inputTypeChoiceBox;
    @FXML private StackPane centerStackPane;

    private final VBox vBox;

    private final DataPane integerDataPane;
    private final DataPane realDataPane;
    private final DataPane characterDataPane;

    public InputDataSetupPane(ControlWrapperSetupStage setupStage) throws IOException
    {
        super(setupStage, "InputDataSetupPane", "Input Data", InputDataAttribute.class, false);

        vBox = (VBox) FXUtil.loadFXML("setup/inputData/inputDataMainPane.fxml", this);

        integerDataPane = new IntegerDataPane();
        realDataPane = new RealDataPane();
        characterDataPane = new CharacterDataPane();
    }

    @Override
    public void setup()
    {
        super.setup();

        integerDataPane.setup(super.getAttributeChangerList());
        realDataPane.setup(super.getAttributeChangerList());
        characterDataPane.setup(super.getAttributeChangerList());

        inputTypeChoiceBox.setConverter(new EnumStringConverter<>(InputWrapper.Type.class).setCapitalize());
        inputTypeChoiceBox.getItems().addAll(InputWrapper.Type.values());
        inputTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue != null)
            {
                var children = centerStackPane.getChildren();
                children.clear();
                switch (newValue)
                {
                    case INTEGER:
                        children.add(integerDataPane.getParent());
                        break;
                    case REAL:
                        children.add(realDataPane.getParent());
                        break;
                    case STRING:
                        children.add(characterDataPane.getParent());
                        break;
                }
            }
        });

        super.getAttributeChangerList().create(inputTypeChoiceBox.valueProperty(), InputDataAttribute.TYPE);

        super.computeGlobalProperties();
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }

    private static abstract class DataPane
    {
        private final VBox vBox;

        public DataPane(String resource) throws IOException
        {
            vBox = (VBox) FXUtil.loadFXML(resource, this);
        }

        public Parent getParent()
        {
            return vBox;
        }

        public abstract void setup(SetupPaneAttributeChangerList<InputDataAttribute> attributeChangerList);
    }

    private static final class IntegerDataPane extends DataPane
    {
        @FXML private TextField maxValueTextField;
        @FXML private TextField minValueTextField;

        public IntegerDataPane() throws IOException
        {
            super("setup/inputData/numericInputDataPane.fxml");
        }

        @Override
        public void setup(SetupPaneAttributeChangerList<InputDataAttribute> attributeChangerList)
        {
            maxValueTextField.setTextFormatter(FXTextFormatterUtil.integerBuilder().getTextFormatter());
            minValueTextField.setTextFormatter(FXTextFormatterUtil.integerBuilder().getTextFormatter());
           /*
            var maxValueFactory = this.parseSpinner(maxValueSpinner);
            maxValueFactory.minProperty().bind(minValueSpinner.valueProperty());

            var minValueFactory = this.parseSpinner(minValueSpinner);
            minValueFactory.maxProperty().bind(maxValueSpinner.valueProperty());
*/
            attributeChangerList.createStringToNumber(maxValueTextField.textProperty(), InputDataAttribute.INTEGER_MAX_VALUE, Util::parseIntOrZero)
                    .createStringToNumber(minValueTextField.textProperty(), InputDataAttribute.INTEGER_MIN_VALUE, Util::parseIntOrZero);
        }

        private SpinnerValueFactory.IntegerSpinnerValueFactory parseSpinner(Spinner<Integer> spinner)
        {
            var valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
            spinner.setValueFactory(valueFactory);
            spinner.setEditable(true);
            parseSpinnerEditor(spinner);

            return valueFactory;
        }
    }

    private static final class RealDataPane extends DataPane
    {
        @FXML private TextField maxValueTextField;
        @FXML private TextField minValueTextField;

        public RealDataPane() throws IOException
        {
            super("setup/inputData/numericInputDataPane.fxml");
        }

        @Override
        public void setup(SetupPaneAttributeChangerList<InputDataAttribute> attributeChangerList)
        {
            maxValueTextField.setTextFormatter(FXTextFormatterUtil.doubleBuilder().getTextFormatter());
            minValueTextField.setTextFormatter(FXTextFormatterUtil.doubleBuilder().getTextFormatter());
            /*
            Stream.of(maxDecimalsLabel, maxDecimalsSpinner).forEach(control -> control.setVisible(true));

            var maxDecimalsFactory = this.parseIntegerSpinner(maxDecimalsSpinner);

            var maxValueFactory = this.parseDoubleSpinner(maxValueSpinner);
            var minValueFactory = this.parseDoubleSpinner(minValueSpinner);
*/
            attributeChangerList.createStringToNumber(minValueTextField.textProperty(), InputDataAttribute.REAL_MAX_VALUE, Util::parseDoubleOrZero)
                    .createStringToNumber(maxValueTextField.textProperty(), InputDataAttribute.REAL_MIN_VALUE, Util::parseDoubleOrZero);
        }

        /*
        @FXML private Spinner<Double> maxValueSpinner;
        @FXML private Spinner<Double> minValueSpinner;

        @FXML private Label maxDecimalsLabel;
        @FXML private Spinner<Integer> maxDecimalsSpinner;

        public RealDataPane() throws IOException
        {
            super("setup/input/numericInputDataPane.fxml");
        }

        @Override
        public void setup(SetupPaneAttributeChangerList<InputDataAttribute> attributeChangerList)
        {
            //By default these are all invisible!
            Stream.of(maxDecimalsLabel, maxDecimalsSpinner).forEach(control -> control.setVisible(true));

            var maxDecimalsFactory = this.parseIntegerSpinner(maxDecimalsSpinner);

            var maxValueFactory = this.parseDoubleSpinner(maxValueSpinner);
            var minValueFactory = this.parseDoubleSpinner(minValueSpinner);

            attributeChangerList.create(maxDecimalsFactory.valueProperty(), InputDataAttribute.REAL_MAX_DECIMALS)
                    .create(maxValueFactory.valueProperty(), InputDataAttribute.REAL_MAX_VALUE)
                    .create(minValueFactory.valueProperty(), InputDataAttribute.REAL_MIN_VALUE);
        }*/

        private SpinnerValueFactory.IntegerSpinnerValueFactory parseIntegerSpinner(Spinner<Integer> spinner)
        {
            var valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
            spinner.setValueFactory(valueFactory);
            spinner.setEditable(true);
            parseSpinnerEditor(spinner);

            return valueFactory;
        }

        private SpinnerValueFactory.DoubleSpinnerValueFactory parseDoubleSpinner(Spinner<Double> spinner)
        {
            var valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 0.1d);
            spinner.setValueFactory(valueFactory);
            spinner.setEditable(true);
            parseSpinnerEditor(spinner);

            return valueFactory;
        }
    }

    private static final class CharacterDataPane extends DataPane
    {
        @FXML private TextField characterLimitTextField;

        public CharacterDataPane() throws IOException
        {
            super("setup/inputData/stringInputDataPane.fxml");
        }

        @Override
        public void setup(SetupPaneAttributeChangerList<InputDataAttribute> attributeChangerList)
        {
            characterLimitTextField.setTextFormatter(
                    FXTextFormatterUtil.integerBuilder()
                            .min(1)
                            .max(250)
                            .getTextFormatter()
            );
            /*
            var valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 250, 1);
            characterLimitSpinner.setValueFactory(valueFactory);
            characterLimitSpinner.setEditable(true);
            parseSpinnerEditor(characterLimitSpinner);
*/
            attributeChangerList.createStringToNumber(characterLimitTextField.textProperty(), InputDataAttribute.CHARACTER_LIMIT, Util::parseIntOrZero);
        }
    }
}
