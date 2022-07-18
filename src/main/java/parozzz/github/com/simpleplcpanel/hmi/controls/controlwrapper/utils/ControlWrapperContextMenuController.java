package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.utils;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

public final class ControlWrapperContextMenuController extends FXObject
{
    private static String generateSVGPath(String imageName)
    {
        return "images/Alignment/" + imageName + ".svg";
    }

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

        ContextMenuBuilder.builder(contextMenu)
                .simple("Setup", () -> controlContainerPane.getSetupStage().setSelectedControlWrapper(controlWrapper))
                .simple("Text Editor", () -> controlContainerPane.getQuickTextEditorStage().setSelectedControlWrapper(controlWrapper))
                .spacer()
                .custom(this.createToBackFrontHBox(), false)
                .spacer()
                .custom(this.createCenterXYHBox(), false)
                .custom(this.createAlignRightLeftHBox(), false)
                .custom(this.createAlignTopBottomHBox(), false)
                .custom(this.createAlignCenterBottomHBox(), false)
                .spacer()
                .simple("Delete", () -> controlContainerPane.deleteControlWrapper(controlWrapper));

        var stylesheetURL = Util.getResource("stylesheets/ContextMenu/focusless.css").toExternalForm();
        controlWrapper.getContainerPane().getStylesheets().add(stylesheetURL);

/*
        contextMenu.showingProperty().addListener((observable, oldValue, newValue) ->
        {
            var containerPane = controlWrapper.getContainerPane();
            if(newValue == null || !newValue)
            {
                containerPane.setEffect(null);
            }
            else
            {
                containerPane.setEffect(new SepiaTone());
            }
        });
*/
        control.setOnContextMenuRequested(event ->
        {
            //Show only if selected!
            if (controlWrapper.getSelectionHandler().isSelected())
            {
                contextMenu.show(control, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private Node createToBackFrontHBox()
    {
        var toBackButton = createAlignImageButton("noun_move to back_181352.png",
                "Move to back",
                controlWrapper.getContainerPane()::toBack
        );
        var toFrontButton = createAlignImageButton("noun_Move To Front_181353.png",
                "Move to front",
                controlWrapper.getContainerPane()::toFront
        );
        return createHBox(toBackButton, toFrontButton);
    }

    private Node createCenterXYHBox()
    {
        var centerXButton = createAlignImageButton("center_x_align.png",
                "Center X",
                () -> controlWrapper.getPositionHandler().centerX(true)
        );
        var centerYButton = createAlignImageButton("center_x_align.png",
                "Center Y", 90,
                () -> controlWrapper.getPositionHandler().centerY(true)
        );
        return createHBox(centerXButton, centerYButton);
    }

    private Node createAlignRightLeftHBox()
    {
        var alignRightButton = createAlignImageButton("left_align.png",
                "Align right", 180,
                controlWrapper.getPositionHandler()::alignRight
        );
        var alignLeftButton = createAlignImageButton("left_align.png",
                "Align left", 0,
                controlWrapper.getPositionHandler()::alignLeft
        );
        return createHBox(alignRightButton, alignLeftButton);
    }

    private Node createAlignTopBottomHBox()
    {
        var alignTopButton = createAlignImageButton("left_align.png",
                "Align top", 90,
                controlWrapper.getPositionHandler()::alignTop
        );
        var alignBottomButton = createAlignImageButton("left_align.png",
                "Align bottom", 270,
                controlWrapper.getPositionHandler()::alignBottom
        );
        return createHBox(alignTopButton, alignBottomButton);
    }

    private Node createAlignCenterBottomHBox()
    {
        var alignTopButton = createAlignImageButton("align_horizontal.png",
                "Align horizontal",
                controlWrapper.getPositionHandler()::alignHorizontalCenter
        );
        var alignBottomButton = createAlignImageButton("align_horizontal.png",
                "Align vertical", 90,
                controlWrapper.getPositionHandler()::alignVerticalCenter
        );
        return createHBox(alignTopButton, alignBottomButton);
    }

    private HBox createHBox(Node... nodes)
    {
        var hbox = new HBox();
        hbox.setMinSize(0, 0);
        hbox.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        hbox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        hbox.setSpacing(4);
        hbox.getChildren().addAll(nodes);
        return hbox;
    }

    private Button createAlignImageButton(String imgName, String tooltipString, Runnable runnable)
    {
        return this.createAlignImageButton(imgName, tooltipString, 0, runnable);
    }

    private Button createAlignImageButton(String imgName, String tooltipString, double rotate,  Runnable runnable)
    {
        var button = new Button("");
        button.setMinSize(0, 0);
        button.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        button.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        button.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        button.setBorder(FXUtil.createBorder(Color.TRANSPARENT, 2));
        button.setPadding(new Insets(-1));
        button.setOnAction(event -> runnable.run());

        var tooltip = new Tooltip(tooltipString);
        tooltip.setAutoHide(true);
        tooltip.setHideDelay(Duration.seconds(1));
        button.setTooltip(tooltip);

        var imgView = new ImageView(Util.getResource("images/icons/align/" + imgName).toExternalForm());
        imgView.setFitHeight(25d);
        imgView.setFitWidth(25d);
        imgView.setPreserveRatio(false);
        imgView.setRotate(rotate);

        button.setGraphic(imgView);
        button.setContentDisplay(ContentDisplay.CENTER);
        button.setGraphicTextGap(0d);

        return button;
    }

    public boolean isShowing()
    {
        return contextMenu.isShowing();
    }

    public void hide()
    {
        contextMenu.hide();
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
}
