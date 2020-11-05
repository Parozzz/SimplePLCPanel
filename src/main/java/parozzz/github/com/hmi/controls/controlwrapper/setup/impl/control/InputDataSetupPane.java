package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.control;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import parozzz.github.com.hmi.attribute.impl.control.InputDataAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.impl.textinput.InputWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.stream.Stream;

public class InputDataSetupPane extends SetupPane<InputDataAttribute>
{
    private static void parseSpinnerEditor(Spinner<?> spinner)
    {
        var editor = spinner.getEditor();
        editor.setAlignment(Pos.CENTER);
        editor.setFont(Font.font(17));
    }

    @FXML private ChoiceBox<InputWrapper.Type> inputTypeChoiceBox;
    @FXML private AnchorPane dataAnchorPane;

    private final VBox mainVBox;

    private final DataPane integerDataPane;
    private final DataPane realDataPane;
    private final DataPane characterDataPane;

    public InputDataSetupPane(ControlWrapperSetupStage setupStage) throws IOException
    {
        super(setupStage, "InputDataSetupPane", "InputData", InputDataAttribute.class);

        mainVBox = (VBox) FXUtil.loadFXML("setup/input/inputDataSetupPane.fxml", this);

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

        inputTypeChoiceBox.setConverter(new EnumStringConverter<>(InputWrapper.Type.class));
        inputTypeChoiceBox.getItems().addAll(InputWrapper.Type.values());
        inputTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue != null)
            {
                var children = dataAnchorPane.getChildren();
                children.clear();
                switch (newValue)
                {
                    case INTEGER:
                        children.add(integerDataPane.getAnchorPane());
                        break;
                    case REAL:
                        children.add(realDataPane.getAnchorPane());
                        break;
                    case STRING:
                        children.add(characterDataPane.getAnchorPane());
                        break;
                }
            }
        });
        inputTypeChoiceBox.setValue(InputWrapper.Type.INTEGER);

        super.getAttributeChangerList().create(inputTypeChoiceBox.valueProperty(), InputDataAttribute.TYPE);

        super.computeGlobalProperties();
    }

    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }

    private static abstract class DataPane
    {
        private final AnchorPane anchorPane;
        public DataPane(String resource) throws IOException
        {
            anchorPane = (AnchorPane) FXUtil.loadFXML(resource, this);
        }

        public AnchorPane getAnchorPane()
        {
            return anchorPane;
        }

        public abstract void setup(SetupPaneAttributeChangerList<InputDataAttribute> attributeChangerList);
    }

    private static final class IntegerDataPane extends DataPane
    {
        @FXML private Spinner<Integer> maxValueSpinner;
        @FXML private Spinner<Integer> minValueSpinner;

        public IntegerDataPane() throws IOException
        {
            super("setup/input/numericInputDataPane.fxml");
        }

        @Override
        public void setup(SetupPaneAttributeChangerList<InputDataAttribute> attributeChangerList)
        {
            var maxValueFactory = this.parseSpinner(maxValueSpinner);
            maxValueFactory.minProperty().bind(minValueSpinner.valueProperty());

            var minValueFactory = this.parseSpinner(minValueSpinner);
            minValueFactory.maxProperty().bind(maxValueSpinner.valueProperty());

            attributeChangerList.create(maxValueFactory.valueProperty(), InputDataAttribute.INTEGER_MAX_VALUE)
                    .create(minValueFactory.valueProperty(), InputDataAttribute.INTEGER_MIN_VALUE);
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
        }

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
        @FXML private Spinner<Integer> characterLimitSpinner;

        public CharacterDataPane() throws IOException
        {
            super("setup/input/stringDataPane.fxml");
        }

        @Override
        public void setup(SetupPaneAttributeChangerList<InputDataAttribute> attributeChangerList)
        {
            var valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 250, 1);
            characterLimitSpinner.setValueFactory(valueFactory);
            characterLimitSpinner.setEditable(true);
            parseSpinnerEditor(characterLimitSpinner);

            attributeChangerList.create(valueFactory.valueProperty(), InputDataAttribute.CHARACTER_LIMIT);
        }
    }
}
