package parozzz.github.com.hmi.controls.controlwrapper.others;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;

public final class ControlWrapperContextMenuController extends FXObject
{
    private final ControlWrapper<?> controlWrapper;
    private final Control control;
    private final ControlContainerPane controlContainerPane;
    private final ContextMenu contextMenu;

    public ControlWrapperContextMenuController(ControlWrapper<?> controlWrapper, Control control,
            ControlContainerPane controlContainerPane)
    {
        super("ControlWrapperContextMenu");

        this.controlWrapper = controlWrapper;
        this.control = control;
        this.controlContainerPane = controlContainerPane;
        this.contextMenu = new ContextMenu();
    }

    @Override
    public void setup()
    {
        super.setup();

        var containerStackPane = controlWrapper.getContainerPane();

        var toFrontMenuItem = FXUtil.createMenuItem("To Front", containerStackPane::toFront);
        var toBackMenuItem = FXUtil.createMenuItem("To Back", containerStackPane::toBack);
        var deleteMenuItem = FXUtil.createMenuItem("Delete",
                () -> controlContainerPane.deleteControlWrapper(controlWrapper));
        var setupMenuItem = FXUtil.createMenuItem("Setup",
                () -> controlContainerPane.getSetupStage().showStageFor(controlWrapper));

        var layoutXMenuItem = this.createLayoutContextMenuItem("X", containerStackPane.layoutXProperty());
        var layoutYMenuItem = this.createLayoutContextMenuItem("Y", containerStackPane.layoutYProperty());

        var centerXMenuItem = FXUtil.createMenuItem("Center X", () ->
        {
            var newLayoutX = (int) Math.floor((controlContainerPane.getMainAnchorPane().getWidth() / 2) - (containerStackPane.getWidth() / 2));
            containerStackPane.setLayoutX(newLayoutX);
        });

        var centerYMenuItem = FXUtil.createMenuItem("Center Y", () ->
        {
            var newLayoutY = (int) Math.floor((controlContainerPane.getMainAnchorPane().getHeight() / 2) - (containerStackPane.getHeight() / 2));
            containerStackPane.setLayoutY(newLayoutY);
        });

        contextMenu.getItems().addAll(setupMenuItem, new SeparatorMenuItem(),
                toFrontMenuItem, toBackMenuItem,
                new SeparatorMenuItem(), layoutXMenuItem, layoutYMenuItem,
                new SeparatorMenuItem(), centerXMenuItem, centerYMenuItem,
                new SeparatorMenuItem(), deleteMenuItem);

        control.setContextMenu(contextMenu);
    }

    public void set()
    {
        control.setContextMenu(contextMenu);
    }

    public void remove()
    {
        control.setContextMenu(null);
    }

    private MenuItem createLayoutContextMenuItem(String labelName, DoubleProperty layoutProperty)
    {
        var customMenuItem = new CustomMenuItem();
        customMenuItem.setHideOnClick(false);

        var label = new Label(labelName);
        label.setAlignment(Pos.CENTER);
        label.setContentDisplay(ContentDisplay.RIGHT);
        label.setGraphicTextGap(12);

        var textField = new TextField();
        textField.setAlignment(Pos.CENTER);
        textField.setPrefSize(50, 20);

        label.setGraphic(textField);

        textField.setText("" + (int) layoutProperty.get());

        textField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        textField.textProperty().addListener(
                (observableValue, oldValue, newValue) -> layoutProperty.set(Double.parseDouble(textField.getText())));

        layoutProperty.addListener((observableValue, oldValue, newValue) -> textField
                .setText("" + (int) Math.floor(newValue.doubleValue())));

        customMenuItem.setContent(label);

        return customMenuItem;
    }

}
