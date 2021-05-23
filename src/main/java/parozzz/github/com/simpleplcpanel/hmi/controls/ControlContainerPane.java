package parozzz.github.com.simpleplcpanel.hmi.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.quicktext.ControlWrapperQuickTextEditorStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.others.ControlWrappersSelectionManager;
import parozzz.github.com.simpleplcpanel.hmi.database.ControlContainerDatabase;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.main.PageScrollingPane;
import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializables;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.specialfunction.FXSpecialFunctionManager;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ControlContainerPane extends FXController implements Loggable
{
    private final MainEditStage mainEditStage;
    private final ControlContainerDatabase controlContainerDatabase;
    private final String name;
    private final Consumer<ControlWrapper<?>> newControlWrapperConsumer;
    private final Consumer<ControlWrapper<?>> deleteControlWrapperConsumer;

    private final AnchorPane mainAnchorPane;
    private final PageScrollingPane.ImagePane mainEditBottomImagePane;

    private final Set<ControlWrapper<?>> controlWrapperSet;
    private final ControlWrappersSelectionManager controlWrappersSelectionManager;

    private final BooleanProperty activeProperty;
    private final Property<Color> backgroundColorProperty;
    private final Property<String> backgroundPictureNameProperty;

    public ControlContainerPane(MainEditStage mainEditStage,
            ControlContainerDatabase controlContainerDatabase, String name,
            Consumer<ControlWrapper<?>> newControlWrapperConsumer,
            Consumer<ControlWrapper<?>> deleteControlWrapperConsumer) throws IOException
    {
        super("ControlsPage_" + name);

        this.controlContainerDatabase = controlContainerDatabase;
        this.mainEditStage = mainEditStage;
        this.name = name;
        this.newControlWrapperConsumer = newControlWrapperConsumer;
        this.deleteControlWrapperConsumer = deleteControlWrapperConsumer;

        this.mainAnchorPane = new AnchorPane();

        this.mainEditBottomImagePane = new PageScrollingPane.ImagePane(this);

        super.addFXChild(controlWrappersSelectionManager = new ControlWrappersSelectionManager(this, mainAnchorPane));
        this.controlWrapperSet = new HashSet<>();

        this.activeProperty = new SimpleBooleanProperty(false);
        this.backgroundColorProperty = new SimpleObjectProperty<>(Color.WHITE);
        this.backgroundPictureNameProperty = new SimpleObjectProperty<>("");
    }

    @Override
    public void setup()
    {
        super.setup();

        super.serializableDataSet.addParsable("BackgroundColor", backgroundColorProperty, JSONSerializables.COLOR)
                .addString("BackgroundPictureName", backgroundPictureNameProperty);

        mainAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            if (controlWrappersSelectionManager.isEmpty())
            {
                return;
            }

            var keyCode = keyEvent.getCode();
            switch (keyCode)
            {
                case DELETE:
                    keyEvent.consume();
                    controlWrappersSelectionManager.deleteAll();
                    break;
                case RIGHT:
                case LEFT:
                    keyEvent.consume();
                    controlWrappersSelectionManager.moveAll(keyCode == KeyCode.RIGHT ? 1 : -1, 0d);
                    break;
                case UP:
                case DOWN:
                    keyEvent.consume();
                    controlWrappersSelectionManager.moveAll(0d, keyCode == KeyCode.UP ? -1 : 1);
                    break;
            }
        });

        mainAnchorPane.setUserData(this);
        mainAnchorPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        mainAnchorPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        mainAnchorPane.setPrefSize(mainEditStage.getPageWidth(), mainEditStage.getPageHeight());
        mainAnchorPane.setBorder(Border.EMPTY);
        mainAnchorPane.visibleProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (newValue)
            {
                var width = mainEditStage.getPageWidth();
                var height = mainEditStage.getPageHeight();
                mainAnchorPane.setPrefSize(width, height);
            }
        });

        var menuBottomContextMenu = ContextMenuBuilder.builder()
                .simple("Delete", () ->
                {
                    var alert = new Alert(Alert.AlertType.CONFIRMATION, "This operation cannot be undone", ButtonType.OK, ButtonType.CLOSE);
                    alert.setHeaderText("Delete page: " + name + "?");
                    alert.showAndWait()
                            .filter(ButtonType.OK::equals)
                            .ifPresent(buttonType -> controlContainerDatabase.deletePage(this));
                })
                .spacer()
                .custom(new Label("Background Color"), false)
                .colorPicker(false, colorPicker -> backgroundColorProperty.bindBidirectional(colorPicker.valueProperty()))
                .spacer()
                .button("Select Picture", false, () ->
                {
                    var pictureBank = this.controlContainerDatabase.getMainEditStage().getPictureBankStage();
                    pictureBank.startImageSelection(file -> backgroundPictureNameProperty.setValue(file.getName()));
                })
                .textField(false, textField -> backgroundPictureNameProperty.bindBidirectional(textField.textProperty()))
                .getContextMenu();

        var menuBottomImageAnchorPane = mainEditBottomImagePane.getAnchorPane();
        menuBottomImageAnchorPane.setOnContextMenuRequested(contextMenuEvent ->
        {
            var x = contextMenuEvent.getScreenX();
            var y = contextMenuEvent.getScreenY();
            menuBottomContextMenu.show(menuBottomImageAnchorPane, x, y);
        });
        FXSpecialFunctionManager.builder(menuBottomImageAnchorPane)
                .enableDoubleClick(MouseButton.PRIMARY, mainEditBottomImagePane,
                        () -> mainEditStage.setShownControlContainerPane(this))
                .bind();

        backgroundColorProperty.addListener((observableValue, oldColor, color) -> this.updateBackground());
        backgroundPictureNameProperty.addListener((observableValue, oldValue, newValue) -> this.updateBackground());
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        //This is required since the snapshot is not done correctly if the AnchorPane is not inside a
        //stage and showed (Says otherwise inside JavaDoc but background is always white).
        //Setting opacity to 0 allows to not see it flicker in the screen! (Cool Cool)
        var stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);

        var scene = new Scene(mainAnchorPane);
        stage.setScene(scene);
        stage.setOpacity(0d);
        stage.show();

        mainEditBottomImagePane.updateSnapshot();
        //I need to remove the AnchorPane as the root since it needs to be added to another later
        scene.setRoot(new AnchorPane());
        stage.hide();
    }

    public ControlContainerDatabase getControlContainerDatabase()
    {
        return controlContainerDatabase;
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    public ControlWrapperSetupStage getSetupStage()
    {
        return mainEditStage.getControlWrapperSetupStage();
    }

    public ControlWrapperQuickTextEditorStage getQuickTextEditorStage()
    {
        return mainEditStage.getControlWrapperQuickTextEditorStage();
    }

    public AnchorPane getMainAnchorPane()
    {
        return mainAnchorPane;
    }

    public PageScrollingPane.ImagePane getMenuBottomImagePane()
    {
        return mainEditBottomImagePane;
    }

    public Set<ControlWrapper<?>> getControlWrapperSet()
    {
        return Set.copyOf(controlWrapperSet);
    }

    public String getName()
    {
        return name;
    }

    public ControlWrappersSelectionManager getSelectionManager()
    {
        return controlWrappersSelectionManager;
    }

    public ControlWrapper<?> createControlWrapper(ControlWrapperType<?, ?> wrapperType)
    {
        return this.createControlWrapper(wrapperType, true);
    }

    private ControlWrapper<?> createControlWrapper(ControlWrapperType<?, ?> wrapperType, boolean setDefault)
    {
        var controlWrapper = wrapperType.createWrapper(this);
        controlWrapper.setup();
        if (setDefault)
        {
            controlWrapper.setDefault();
            controlWrapper.setupComplete();
        }

        this.addControlWrapper(controlWrapper);
        return controlWrapper;
    }

    public void addControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if (!controlWrapper.isSetupDone())
        {
            MainLogger.getInstance().error("Cannot add a not initialized ControlWrapper", this);
            return;
        }

        if (controlWrapper.getControlMainPage() != this)
        {
            MainLogger.getInstance().error("Cannot add a ControlWrapper inside the wrong ControlContainerPane", this);
            return;
        }

        if (!controlWrapperSet.add(controlWrapper))
        {
            MainLogger.getInstance().error("Cannot add a ControlWrapper twice inside the same ControlContainerPane", this);
            return;
        }

        super.addFXChild(controlWrapper, false);
        mainAnchorPane.getChildren().add(controlWrapper.getContainerPane());

        controlWrapper.setValid(true);

        newControlWrapperConsumer.accept(controlWrapper);

        var undoRedoManager = this.controlContainerDatabase.getMainEditStage().getUndoRedoManager();
        undoRedoManager.addAction(() -> this.deleteControlWrapper(controlWrapper),
                () -> this.addControlWrapper(controlWrapper),
                this);
    }

    public void deleteControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if (!controlWrapperSet.remove(controlWrapper))
        {
            return;
        }

        super.removeFXChild(controlWrapper);
        mainAnchorPane.getChildren().remove(controlWrapper.getContainerPane());

        deleteControlWrapperConsumer.accept(controlWrapper);

        controlWrapper.setValid(false);

        var undoRedoManager = this.controlContainerDatabase.getMainEditStage().getUndoRedoManager();
        undoRedoManager.addAction(() -> this.addControlWrapper(controlWrapper),
                () -> this.deleteControlWrapper(controlWrapper),
                this);
    }

    public void setBackgroundColor(Color color)
    {
        backgroundColorProperty.setValue(color);
    }

    public void setBackgroundPictureName(String pictureName)
    {
        backgroundPictureNameProperty.setValue(pictureName);
    }

    public boolean isActive()
    {
        return activeProperty.get();
    }

    public void setActive(boolean active)
    {
        activeProperty.set(active);
    }

    public BooleanProperty activeProperty()
    {
        return activeProperty;
    }

    public void convertToReadOnly()
    {
        controlWrapperSet.forEach(ControlWrapper::convertToReadOnly);
    }

    public void convertToReadWrite()
    {
        controlWrapperSet.forEach(ControlWrapper::convertToReadWrite);
    }

    public boolean hasControlWrapperNear(double layoutX, double layoutY, double offsetX, double offsetY)
    {
        for (var controlWrapper : controlWrapperSet)
        {
            var xDiff = Math.abs(controlWrapper.getLayoutX() - layoutX);
            var yDiff = Math.abs(controlWrapper.getLayoutY() - layoutY);

            if (xDiff <= offsetX && yDiff <= offsetY)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public JSONDataMap serialize()
    {
        var jsonData = super.serialize();

        jsonData.set("ControlWrapperPageName", name);
        jsonData.set("Controls", JSONDataArray.of(controlWrapperSet));

        return jsonData;
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        super.deserialize(jsonDataMap);

        var controlJSONArray = jsonDataMap.getArray("Controls");
        if (controlJSONArray != null)
        {
            //This is necessary otherwise you would be able to cancel stuff from serialization
            var undoRedoManager = this.getMainEditStage().getUndoRedoManager();
            undoRedoManager.setIgnoreNew(true);

            controlJSONArray.stream().filter(JSONObject.class::isInstance)
                    .map(JSONObject.class::cast)
                    .map(JSONDataMap::new)
                    .forEach(controlJSONDataMap ->
                    {
                        var wrapperTypeName = controlJSONDataMap.getString("WrapperType");

                        var wrapperType = ControlWrapperType.getFromName(wrapperTypeName);
                        if (wrapperType != null)
                        {
                            var controlWrapper = this.createControlWrapper(wrapperType, false);
                            controlWrapper.deserialize(controlJSONDataMap);
                        }
                    });

            undoRedoManager.setIgnoreNew(false);
        } else
        {
            MainLogger.getInstance()
                    .warning("Controls JSONArray has not been found while de-serializing", this);
        }
    }

    private void updateBackground()
    {
        var color = backgroundColorProperty.getValue();
        var pictureName = backgroundPictureNameProperty.getValue();

        List<BackgroundImage> backgroundImageList = Collections.emptyList();
        if (pictureName != null && !pictureName.isEmpty())
        {
            var imageURI = this.controlContainerDatabase.getMainEditStage().getPictureBankStage().getImageURI(pictureName);
            if (imageURI != null)
            {
                var backgroundImage = new BackgroundImage(new Image(imageURI.toString()),
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                        new BackgroundSize(1.0, 1.0, true, true, false, false));
                backgroundImageList = Collections.singletonList(backgroundImage);
            }
        }

        List<BackgroundFill> backgroundFillList = Collections.emptyList();
        if (color != null)
        {
            backgroundFillList = Collections.singletonList(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
        }

        mainAnchorPane.setBackground(new Background(backgroundFillList, backgroundImageList));
    }

    @Override
    public String log()
    {
        return "Name: " + name +
                "BackgroundColor: " + backgroundColorProperty.getValue() +
                "BackgroundPictureName: " + backgroundPictureNameProperty.getValue() +
                "ControlWrapperAmount: " + controlWrapperSet.size();
    }
}
