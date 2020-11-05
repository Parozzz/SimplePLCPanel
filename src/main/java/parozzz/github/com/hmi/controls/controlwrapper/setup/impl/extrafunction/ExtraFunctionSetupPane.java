package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.extrafunction;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.attribute.impl.ExtraFunctionAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.extra.ControlWrapperExtraFeature;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.extrafunction.impl.ChangePageExtraFunctionPane;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public final class ExtraFunctionSetupPane extends SetupPane<ExtraFunctionAttribute>
{
    @FXML private ChoiceBox<ControlWrapperExtraFeature.Type> functionChoiceBox;
    @FXML private AnchorPane bottomAnchorPane;

    private final VBox mainVBox;
    private final ChangePageExtraFunctionPane changePageExtraPane;

    public ExtraFunctionSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "ExtraFunctionSetupPane", "ExtraFunction", ExtraFunctionAttribute.class);

        this.mainVBox = (VBox) FXUtil.loadFXML("setup/extraSetupPane.fxml", this);

        this.changePageExtraPane = new ChangePageExtraFunctionPane(this);
    }

    @Override
    public void setup()
    {
        super.setup();

        changePageExtraPane.init();

        functionChoiceBox.setConverter(new EnumStringConverter<>(ControlWrapperExtraFeature.Type.class).setCapitalize());
        functionChoiceBox.getItems().addAll(ControlWrapperExtraFeature.Type.values());
        functionChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            AnchorPane anchorPane;
            switch (newValue)
            {
                default:
                case NONE:
                    anchorPane = new AnchorPane();
                    anchorPane.setPrefSize(400, 350);
                    break;
                case CHANGE_PAGE:
                    anchorPane = changePageExtraPane.getAnchorPane();
                    break;
            }
            var children = bottomAnchorPane.getChildren();
            children.clear();
            children.add(anchorPane);
        });
        functionChoiceBox.setValue(ControlWrapperExtraFeature.Type.NONE);

        /*
        functionChoiceBox.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change ->
        {
            while (change.next())
            {
                change.getAddedSubList().stream()
                        .filter(Label.class::isInstance)
                        .map(Label.class::cast)
                        .forEach(label -> label.setStyle("-fx-font-size: 22"));
            }
            System.out.println("COOL");
        });*/

        super.getAttributeChangerList().create(functionChoiceBox.valueProperty(), ExtraFunctionAttribute.TYPE)
                .create(changePageExtraPane.selectPageComboBox.getEditor().textProperty(), ExtraFunctionAttribute.PAGE_NAME);

        super.computeGlobalProperties();
    }

    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }
}
