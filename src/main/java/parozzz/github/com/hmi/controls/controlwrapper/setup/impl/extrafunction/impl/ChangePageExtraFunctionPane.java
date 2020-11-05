package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.extrafunction.impl;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.extrafunction.ExtraFunctionPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.extrafunction.ExtraFunctionSetupPane;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public class ChangePageExtraFunctionPane extends ExtraFunctionPane
{
    @FXML public ComboBox<String> selectPageComboBox;

    private final ExtraFunctionSetupPane setupPane;
    private final AnchorPane mainAnchorPane;

    public ChangePageExtraFunctionPane(ExtraFunctionSetupPane setupPane) throws IOException
    {
        this.setupPane = setupPane;
        mainAnchorPane = (AnchorPane) FXUtil.loadFXML("setup/extra/changePagePane.fxml", this);
    }

    @Override
    public void init()
    {
        selectPageComboBox.getEditor().setAlignment(Pos.CENTER);
        selectPageComboBox.setOnShowing(event ->
        {
            var items = selectPageComboBox.getItems();
            items.clear();

            setupPane.getSetupStage().getControlMainPage().getControlContainerDatabase().getPageList()
                    .stream().map(ControlContainerPane::getName)
                    .forEach(items::add);
        });
    }

    @Override
    public AnchorPane getAnchorPane()
    {
        return mainAnchorPane;
    }
}
