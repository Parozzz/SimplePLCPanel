package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.utils;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

public final class ControlWrapperContextMenuController extends FXObject
{
    private final ControlWrapper<?> controlWrapper;
    private final Control control;
    private final ControlContainerPane controlContainerPane;
    private final ContextMenu contextMenu;

    public ControlWrapperContextMenuController(ControlWrapper<?> controlWrapper, Control control,
            ControlContainerPane controlContainerPane)
    {
        this.controlWrapper = controlWrapper;
        this.control = control;
        this.controlContainerPane = controlContainerPane;
        this.contextMenu = new ContextMenu();
    }

    @Override
    public void onSetup()
    {
        super.onSetup();


        var containerStackPane = controlWrapper.getContainerPane();
        ContextMenuBuilder.builder(contextMenu)
                .simple("Setup", () -> controlContainerPane.getSetupStage().setSelectedControlWrapper(controlWrapper))
                .simple("Text Editor", () -> controlContainerPane.getQuickTextEditorStage().setSelectedControlWrapper(controlWrapper))
                .spacer()
                .simple("To Front", containerStackPane::toFront)
                .simple("To Back", containerStackPane::toBack)
                .spacer()
                .custom(this.createLayoutLabel("X", containerStackPane.layoutXProperty()), false)
                .custom(this.createLayoutLabel("Y", containerStackPane.layoutYProperty()), false)
                .spacer()
                .simple("Center X", this::centerX)
                .simple("Center Y", this::centerY)
                .spacer()
                .simple("Delete", () -> controlContainerPane.deleteControlWrapper(controlWrapper));


        control.setOnContextMenuRequested(event ->
        {
            //Show only if selected!
            if (controlWrapper.isSelected())
            {
                contextMenu.show(control, event.getScreenX(), event.getScreenY());
            }
        });
    }

    public void set()
    {
        control.setContextMenu(contextMenu);
    }

    public void remove()
    {
        control.setContextMenu(null);
    }

    private Label createLayoutLabel(String labelName, DoubleProperty layoutProperty)
    {
        var label = new Label(labelName);
        label.setAlignment(Pos.CENTER);
        label.setContentDisplay(ContentDisplay.RIGHT);
        label.setGraphicTextGap(12);

        var textField = new TextField();
        textField.setAlignment(Pos.CENTER);
        textField.setPrefSize(50, 20);

        label.setGraphic(textField);

        textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(4));
        textField.textProperty().addListener((observableValue, oldValue, newValue) ->
                layoutProperty.set(newValue == null
                        ? 0
                        : Util.parseDouble(newValue, 0))
        );

        textField.setText("" + (int) layoutProperty.get());
        layoutProperty.addListener((observableValue, oldValue, newValue) ->
                textField.setText("" + newValue.intValue())
        );

        return label;
    }

    private void centerX()
    {
        var containerStackPane = controlWrapper.getContainerPane();

        var newLayoutX = (int) Math.floor(
                (controlContainerPane.getMainAnchorPane().getWidth() / 2) - (containerStackPane.getWidth() / 2)
        );
        containerStackPane.setLayoutX(newLayoutX);
    }

    private void centerY()
    {
        var containerStackPane = controlWrapper.getContainerPane();

        var newLayoutY = (int) Math.floor(
                (controlContainerPane.getMainAnchorPane().getHeight() / 2) - (containerStackPane.getHeight() / 2)
        );
        containerStackPane.setLayoutY(newLayoutY);
    }
}
