package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.ChangePageAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public final class ChangePageSetupPane extends SetupPane<ChangePageAttribute>
{
    @FXML private ComboBox<String> selectPageComboBox;
    @FXML private CheckBox enabledCheckBox;

    private final ControlWrapperSetupStage setupStage;
    private final VBox vBox;

    public ChangePageSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "ChangePageSetupPane", "Change Page", AttributeType.CHANGE_PAGE);

        this.setupStage = setupPage;
        this.vBox = (VBox) FXUtil.loadFXML("setup/changePagePane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        selectPageComboBox.setOnShowing(event ->
        {
            var items = selectPageComboBox.getItems();
            items.clear();

            setupStage.getMainEditStage().getControlContainerDatabase().getPageList()
                    .stream().map(ControlContainerPane::getName)
                    .forEach(items::add);
        });

        super.getAttributeChangerList().create(enabledCheckBox.selectedProperty(), ChangePageAttribute.ENABLED)
                .create(selectPageComboBox.valueProperty(), ChangePageAttribute.PAGE_NAME);

        super.computeProperties();
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }
}
