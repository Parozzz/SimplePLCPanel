package parozzz.github.com.hmi.controls;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.others.ControlWrappersSelectionManager;
import parozzz.github.com.hmi.database.ControlContainerDatabase;
import parozzz.github.com.hmi.main.MainEditBottomScrollingPane;
import parozzz.github.com.hmi.main.MainEditStage;
import parozzz.github.com.hmi.serialize.JSONSerializables;
import parozzz.github.com.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.hmi.util.ContextMenuBuilder;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.hmi.util.specialfunction.FXSpecialFunctionManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControlContainerPane extends FXController
{
    private final AtomicInteger controlWrapperIdentifier = new AtomicInteger();

    private final ControlContainerDatabase controlContainerDatabase;
    private final MainEditStage mainEditStage;
    private final String name;
    private final Consumer<ControlWrapper<?>> newControlWrapperConsumer;
    private final Consumer<ControlWrapper<?>> deleteControlWrapperConsumer;

    private final AnchorPane mainAnchorPane;

    private final ControlWrapperSetupStage controlWrapperSetupStage;
    private final MainEditBottomScrollingPane.ImagePane mainEditBottomImagePane;

    private final Property<Color> backgroundColorProperty;
    private final Property<String> backgroundPictureNameProperty;

    private final Set<ControlWrapper<?>> controlWrapperSet;
    private final ControlWrappersSelectionManager controlWrappersSelectionManager;

    public ControlContainerPane(ControlContainerDatabase controlContainerDatabase, MainEditStage mainEditStage,
            String name,
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

        super.addFXChild(controlWrapperSetupStage = new ControlWrapperSetupStage(this))
                .addFXChild(controlWrappersSelectionManager = new ControlWrappersSelectionManager(this, mainAnchorPane));

        this.mainEditBottomImagePane = new MainEditBottomScrollingPane.ImagePane(this);

        this.backgroundColorProperty = new SimpleObjectProperty<>(Color.WHITE);
        this.backgroundPictureNameProperty = new SimpleObjectProperty<>("");

        this.controlWrapperSet = new HashSet<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        super.serializableDataSet.addParsable("BackgroundColor", backgroundColorProperty, JSONSerializables.COLOR)
                .addString("BackgroundPictureName", backgroundPictureNameProperty);

        mainEditStage.getStageSetter().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            if(controlWrappersSelectionManager.isEmpty())
            {
                return;
            }

            var keyCode = keyEvent.getCode();
            switch(keyCode)
            {
                case DELETE:
                    controlWrappersSelectionManager.deleteAll();
                    break;
                case RIGHT:
                case LEFT:
                    controlWrappersSelectionManager.moveAll(keyCode == KeyCode.RIGHT ? 1 : -1, 0d);
                    break;
                case UP:
                case DOWN:
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
            if(newValue)
            {
                var width = mainEditStage.getPageWidth();
                var height = mainEditStage.getPageHeight();
                mainAnchorPane.setPrefSize(width, height);
            }
        });

        var deletePageMenuItem = new MenuItem("Delete");
        deletePageMenuItem.setOnAction(actionEvent ->
        {
            var optional = new Alert(Alert.AlertType.CONFIRMATION).showAndWait();
            if(optional.isPresent() && optional.get() == ButtonType.OK)
            {
                controlContainerDatabase.deletePage(this);
            }
        });
/*
        var backgroundColorLabelMenuItem = FXUtil.createCustomMenuItem(() -> new Label("Background Color"), false);

        var backgroundColorMenuItem = FXUtil.createCustomMenuItem(() ->
        {
            var colorPicker = new ColorPicker();
            backgroundColorProperty.bindBidirectional(colorPicker.valueProperty());
            return colorPicker;
        }, false);

        var backgroundImageNameMenuItem = FXUtil.createCustomMenuItem(() ->
        {
            var textField = new TextField();
            backgroundPictureNameProperty.bindBidirectional(textField.textProperty());
            return textField;
        }, false);

        var backgroundImageSelectMenuItem = FXUtil.createCustomMenuItem(() ->
        {
            var button = new Button("Select Picture");
            button.setOnAction(actionEvent ->
            {
                var pictureBank = this.controlContainerDatabase.getMainEditStage().getPictureBankStage();
                pictureBank.startImageSelection(file -> backgroundPictureNameProperty.setValue(file.getName()));
            });
            return button;
        }, false);


        var menuBottomContextMenu = new ContextMenu(deletePageMenuItem,
                new SeparatorMenuItem(),
                backgroundColorLabelMenuItem, backgroundColorMenuItem,
                new SeparatorMenuItem(),
                backgroundImageSelectMenuItem, backgroundImageNameMenuItem);
*/
        var menuBottomContextMenu = ContextMenuBuilder.builder()
                .simple("Delete", () ->
                        new Alert(Alert.AlertType.CONFIRMATION).showAndWait()
                                .filter(ButtonType.OK::equals)
                                .ifPresent(buttonType -> controlContainerDatabase.deletePage(this)))
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
    public void setDefault()
    {
        super.setDefault();

        backgroundColorProperty.setValue(Color.WHITE);
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
        return controlWrapperSetupStage;
    }

    public AnchorPane getMainAnchorPane()
    {
        return mainAnchorPane;
    }

    public MainEditBottomScrollingPane.ImagePane getMenuBottomImagePane()
    {
        return mainEditBottomImagePane;
    }

    public Set<ControlWrapper<?>> getControlWrapperSet()
    {
        return Set.copyOf(controlWrapperSet);
    }

    public int getNextControlWrapperIdentifier()
    {
        return controlWrapperIdentifier.getAndAdd(1);
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
        if(setDefault)
        {
            controlWrapper.setDefault();
            controlWrapper.setupComplete();
        }

        this.addControlWrapper(controlWrapper);

        var undoRedoManager = this.controlContainerDatabase.getMainEditStage().getUndoRedoManager();
        undoRedoManager.addAction(() -> this.deleteControlWrapper(controlWrapper),
                () -> this.addControlWrapper(controlWrapper),
                this);

        return controlWrapper;
    }

    public void deleteControlWrapper(ControlWrapper<?> controlWrapper)
    {
        super.removeFXChild(controlWrapper);
        mainAnchorPane.getChildren().remove(controlWrapper.getContainerPane());
        controlWrapperSet.remove(controlWrapper);

        deleteControlWrapperConsumer.accept(controlWrapper);

        var undoRedoManager = this.controlContainerDatabase.getMainEditStage().getUndoRedoManager();
        undoRedoManager.addAction(() -> this.addControlWrapper(controlWrapper),
                () -> this.deleteControlWrapper(controlWrapper),
                this);
    }

    private void addControlWrapper(ControlWrapper<?> controlWrapper)
    {
        controlWrapperSet.add(controlWrapper);
        mainAnchorPane.getChildren().add(controlWrapper.getContainerPane());
        super.addFXChild(controlWrapper, false);

        newControlWrapperConsumer.accept(controlWrapper);
    }

    public void setBackgroundColor(Color color)
    {
        backgroundColorProperty.setValue(color);
    }

    public void setBackgroundPictureName(String pictureName)
    {
        backgroundPictureNameProperty.setValue(pictureName);
    }

    public void convertToReadOnly()
    {
        controlWrapperSet.forEach(ControlWrapper::convertToReadOnly);
    }

    public void convertToReadWrite()
    {
        controlWrapperSet.forEach(ControlWrapper::convertToReadWrite);
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
        if(controlJSONArray != null)
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
                        if(wrapperType != null)
                        {
                            var controlWrapper = this.createControlWrapper(wrapperType, false);
                            controlWrapper.deserialize(controlJSONDataMap);
                        }
                    });

            undoRedoManager.setIgnoreNew(false);
        }else
        {
            Logger.getLogger(ControlContainerPane.class.getSimpleName())
                    .log(Level.WARNING, "Controls JSONArray has not been found while de-serializing");
        }
    }

    private void updateBackground()
    {
        var color = backgroundColorProperty.getValue();
        var pictureName = backgroundPictureNameProperty.getValue();

        List<BackgroundImage> backgroundImageList = Collections.emptyList();
        if(pictureName != null && !pictureName.isEmpty())
        {
            var imageURI = this.controlContainerDatabase.getMainEditStage().getPictureBankStage().getImageURI(pictureName);
            if(imageURI != null)
            {
                var backgroundImage = new BackgroundImage(new Image(imageURI.toString()),
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                        new BackgroundSize(1.0, 1.0, true, true, false, false));
                backgroundImageList = Collections.singletonList(backgroundImage);
            }
        }

        List<BackgroundFill> backgroundFillList = Collections.emptyList();
        if(color != null)
        {
            backgroundFillList = Collections.singletonList(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
        }

        mainAnchorPane.setBackground(new Background(backgroundFillList, backgroundImageList));
    }
}
