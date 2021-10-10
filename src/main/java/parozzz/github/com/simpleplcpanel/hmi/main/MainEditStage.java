package parozzz.github.com.simpleplcpanel.hmi.main;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerCreationStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.quicktext.ControlWrapperQuickTextEditorStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerDatabase;
import parozzz.github.com.simpleplcpanel.hmi.main.dragdrop.DragAndDropPane;
import parozzz.github.com.simpleplcpanel.hmi.main.others.MessagesListStage;
import parozzz.github.com.simpleplcpanel.hmi.main.others.copypaste.ControlWrapperCopyPasteHandler;
import parozzz.github.com.simpleplcpanel.hmi.main.picturebank.PictureBankStage;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.settings.SettingsStage;
import parozzz.github.com.simpleplcpanel.hmi.pane.BorderPaneHMIStage;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoManager;
import parozzz.github.com.simpleplcpanel.hmi.runtime.RuntimeControlContainerStage;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.TrigBoolean;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;

public final class MainEditStage extends BorderPaneHMIStage
{
    //Runtime
    @FXML private CheckBox runtimeFullScreenCheckBox;
    @FXML private CheckBox runtimeAtStartupCheckBox;
    @FXML private MenuItem startRuntimeMenuItem;

    //File
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem settingsMenuItem;

    //ControlWrapperPage Data
    @FXML private MenuItem createPageMenuItem;
    @FXML private TextField zoomTextField;
    @FXML private TextField pageWidthTextField;
    @FXML private TextField pageHeightTextField;

    //View Menu
    @FXML private MenuItem viewQuickSetupMenuItem;
    @FXML private MenuItem viewDragAndDropMenuItem;
    @FXML private MenuItem viewScrollingPagesMenuItem;

    //Communication
    @FXML private Circle plcConnectedCircle;
    @FXML private MenuItem setupCommunicationMenuItem;

    //Tools Menu
    @FXML private MenuItem pictureBankMenuItem;
    @FXML private MenuItem modbusTCPStringAddressMenuItem;
    @FXML private MenuItem siemensS7StringAddressMenuItem;
    @FXML private MenuItem tagsMenuItem;

    //Messages Menu
    @FXML private Label messagePresentLabel;
    @FXML private MenuItem showMessageListMenuItem;

    //Sides Stack Panes
    @FXML private StackPane leftStackPane;
    @FXML private StackPane rightStackPane;
    @FXML private StackPane bottomStackPane;

    //Center = Page Showing
    @FXML private Label centerTopLabel;
    @FXML private VBox centerMainVBox;
    @FXML private StackPane centerScrollStackPane;

    private final CommunicationDataHolder communicationDataHolder;
    private final Runnable saveDataRunnable;

    private final ControlContainerDatabase controlContainerDatabase;
    private final ControlContainerCreationStage controlsPageCreationStage;
    private final ControlWrapperSetupStage controlWrapperSetupStage;
    private final ControlWrapperQuickTextEditorStage controlWrapperQuickTextEditorStage;
    private final DragAndDropPane dragAndDropPane;
    private final PageScrollingPane pageScrollingPane;
    private final QuickSetupPane quickSetupPane;
    private final SettingsStage settingsStage;
    private final PictureBankStage pictureBankStage;
    private final MessagesListStage messagesListStage;
    private final ControlWrapperCopyPasteHandler copyPasteHandler;
    private final RuntimeControlContainerStage runtimeControlMainPage;
    private final TagsManager tagsManager;

    private final TrigBoolean messagePresentTrig;
    private ControlContainerPane shownControlContainerPane;

    public MainEditStage(TagsManager tagsManager,
            CommunicationDataHolder communicationDataHolder, Runnable saveDataRunnable) throws IOException
    {
        super("Menu", "mainEditPane.fxml");

        this.tagsManager = tagsManager;

        (this.communicationDataHolder = communicationDataHolder).getCommunicationStage().setAsSubWindow(this);
        this.saveDataRunnable = saveDataRunnable;

        super   //HANDLERS AND VARIOUS
                .addFXChild(controlContainerDatabase = new ControlContainerDatabase(this))
                .addFXChild(copyPasteHandler = new ControlWrapperCopyPasteHandler(this))
                //SIDE PANES
                .addFXChild(dragAndDropPane = new DragAndDropPane(this)) //LEFT
                .addFXChild(quickSetupPane = new QuickSetupPane(this, tagsManager, communicationDataHolder)) //RIGHT
                .addFXChild(pageScrollingPane = new PageScrollingPane()) //BOTTOM
                //CHILD STAGES
                .addFXChild((controlsPageCreationStage = new ControlContainerCreationStage(controlContainerDatabase)).setAsSubWindow(this))
                .addFXChild((controlWrapperSetupStage = new ControlWrapperSetupStage(this, tagsManager, communicationDataHolder)).setAsSubWindow(this))
                .addFXChild((controlWrapperQuickTextEditorStage = new ControlWrapperQuickTextEditorStage()).setAsSubWindow(this))
                .addFXChild((settingsStage = new SettingsStage()).setAsSubWindow(this))
                .addFXChild((pictureBankStage = new PictureBankStage()).setAsSubWindow(this))
                .addFXChild((messagesListStage = new MessagesListStage()).setAsSubWindow(this))
                //OTHER STAGES
                .addFXChild(runtimeControlMainPage = new RuntimeControlContainerStage(this));


        this.messagePresentTrig = new TrigBoolean(false);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        var saveKeyCombination = KeyCombination.keyCombination("CTRL+S");

        super.getStageSetter().setResizable(true)
                .setMaximized(true)
                .addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
                {
                    if(saveKeyCombination.match(keyEvent))
                    {
                        saveMenuItem.fire();
                        keyEvent.consume();
                    }
                });

        super.serializableDataSet.addString("PageHeight", pageHeightTextField.textProperty(), "640")
                .addString("PageWidth", pageWidthTextField.textProperty(), "480")
                .addBoolean("RuntimeFullScreen", runtimeFullScreenCheckBox.selectedProperty(), false)
                .addBoolean("RuntimeAtStartup", runtimeAtStartupCheckBox.selectedProperty(), false);

        settingsStage.getStageSetter().setResizable(true);

        //File Menu
        settingsMenuItem.setOnAction(actionEvent -> settingsStage.showStage());
        saveMenuItem.setOnAction(event -> saveDataRunnable.run());

        //Communication Menu
        setupCommunicationMenuItem.setOnAction(event -> communicationDataHolder.getCommunicationStage().showStage());

        //Runtime Menu
        startRuntimeMenuItem.setOnAction(actionEvent -> this.showRuntimeScene(true, runtimeFullScreenCheckBox.isSelected()));

        //Page Menu
        createPageMenuItem.setOnAction(actionEvent -> controlsPageCreationStage.showStage());

        //Tools Menu
        pictureBankMenuItem.setOnAction(actionEvent -> pictureBankStage.showStage());
        modbusTCPStringAddressMenuItem.setOnAction(event -> {
            var stringAddressCreator = CommunicationType.MODBUS_TCP.supplyStringAddressCreatorStage();
            if(stringAddressCreator != null)
            {
                stringAddressCreator.setAsSubWindow(MainEditStage.this);
                stringAddressCreator.showAsStandalone();
            }
        });
        siemensS7StringAddressMenuItem.setOnAction(event -> {
            var stringAddressCreator = CommunicationType.SIEMENS_S7.supplyStringAddressCreatorStage();
            if(stringAddressCreator != null)
            {
                stringAddressCreator.setAsSubWindow(MainEditStage.this);
                stringAddressCreator.showAsStandalone();
            }
        });
        this.tagsMenuItem.setOnAction(event -> TagStage.showStandalone(tagsManager, communicationDataHolder, MainEditStage.this));

        //Messages Menu
        showMessageListMenuItem.setOnAction(event -> messagesListStage.showStage());
        messagePresentLabel.setVisible(false);

        zoomTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));
        zoomTextField.textProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var zoom = Util.parseInt(newValue, 100);
            zoom = Math.max(0, Math.min(400, zoom)); //Max double the scale.

            centerScrollStackPane.setScaleX(zoom / 100d);
            centerScrollStackPane.setScaleY(zoom / 100d);

            zoomTextField.setText(Integer.toString(zoom));
        });

        pageWidthTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        pageWidthTextField.textProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var newWidth = Util.parseInt(newValue, 640);
            for(var controlContainerPage : controlContainerDatabase)
            {
                var anchorPane = controlContainerPage.getMainAnchorPane();
                anchorPane.setPrefWidth(newWidth);
                anchorPane.setMaxWidth(newWidth);
            }

            runtimeControlMainPage.getStageSetter().setWidth(newWidth);
        });

        pageHeightTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        pageHeightTextField.textProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var newHeight = Util.parseInt(newValue, 480);
            for(var controlContainerPage : controlContainerDatabase)
            {
                var anchorPane = controlContainerPage.getMainAnchorPane();
                anchorPane.setPrefHeight(newHeight);
                anchorPane.setMaxHeight(newHeight);
            }

            runtimeControlMainPage.getStageSetter().setHeight(newHeight);
        });

        //Needs to be on the vbox in case the zoom is too low
        centerMainVBox.addEventFilter(ScrollEvent.SCROLL, scrollEvent ->
        {
            if(scrollEvent.isControlDown())
            {
                var delta = scrollEvent.getDeltaY();

                var zoom = Util.parseInt(zoomTextField.getText(), 100);
                zoom += (delta / 10d);
                zoomTextField.setText(String.valueOf(zoom));
                //This is to avoid the scroll pane to scroll before zooming
                scrollEvent.consume();
            }
        });


        //Setup for Bottom Scrolling Pane
        pageScrollingPane.bindVisibilityToMenuItem(
                viewScrollingPagesMenuItem,
                () -> bottomStackPane.getChildren().add(pageScrollingPane.getMainParent()),
                () -> bottomStackPane.getChildren().remove(pageScrollingPane.getMainParent())
        );

        //Setup for DRAG AND DROP
        dragAndDropPane.bindVisibilityToMenuItem(
                viewDragAndDropMenuItem,
                () -> leftStackPane.getChildren().add(dragAndDropPane.getMainParent()),
                () -> leftStackPane.getChildren().remove(dragAndDropPane.getMainParent())
        );

        //Setup for Quick Setup
        quickSetupPane.bindVisibilityToMenuItem(
                viewQuickSetupMenuItem,
                () -> rightStackPane.getChildren().add(quickSetupPane.getMainParent()),
                () -> rightStackPane.getChildren().remove(quickSetupPane.getMainParent())
        );
    }

    @Override
    public void onSetDefault()
    {
        super.onSetDefault();

        zoomTextField.setText("100");
        pageWidthTextField.setText("640");
        pageHeightTextField.setText("480");
    }

    @Override
    public void onLoop()
    {
        super.onLoop();

        if(super.every(1000))
        {
            var currentCommThread = communicationDataHolder.getCurrentCommThread();
            if(currentCommThread != null)
            {
                plcConnectedCircle.setFill(currentCommThread.isConnected() ? Color.GREEN : Color.RED);
            }

            messagePresentTrig.set(messagesListStage.areMessagesPresent());
            switch(messagePresentTrig.checkTrig())
            {
                case RISING:
                    messagePresentLabel.setVisible(true);
                    break;
                case FALLING:
                    messagePresentLabel.setVisible(false);
                    break;
            }
        }
    }

    @Override
    public void onSetupComplete()
    {
        super.onSetupComplete();

        //On setup complete, if there is any page, open the first.
        /*
        var pageList = controlContainerDatabase.getPageList();
        if (!pageList.isEmpty())
        {
            this.setShownControlContainerPane(pageList.get(0));
        }*/
    }

    public ControlContainerDatabase getControlContainerDatabase()
    {
        return controlContainerDatabase;
    }

    public ControlWrapperSetupStage getControlWrapperSetupStage()
    {
        return controlWrapperSetupStage;
    }

    public ControlWrapperQuickTextEditorStage getControlWrapperQuickTextEditorStage()
    {
        return controlWrapperQuickTextEditorStage;
    }

    public CommunicationDataHolder getCommunicationDataHolder()
    {
        return communicationDataHolder;
    }

    public QuickSetupPane getQuickPropertiesVBox()
    {
        return quickSetupPane;
    }

    public PageScrollingPane getPageScrollingPane()
    {
        return pageScrollingPane;
    }

    public SettingsStage getSettingsStage()
    {
        return settingsStage;
    }

    public PictureBankStage getPictureBankStage()
    {
        return pictureBankStage;
    }

    public ControlContainerPane getShownControlContainerPane()
    {
        return shownControlContainerPane;
    }

    public TagsManager getTagsManager()
    {
        return tagsManager;
    }

    public int getPageWidth()
    {
        return Integer.parseInt(pageWidthTextField.getText());
    }

    public int getPageHeight()
    {
        return Integer.parseInt(pageHeightTextField.getText());
    }

    public boolean isRuntimeShowing()
    {
        return runtimeControlMainPage.getStageSetter().isShowing();
    }

    public void changeRuntimePage(ControlContainerPane controlContainerPane)
    {
        if(this.isRuntimeShowing())
        {
            runtimeControlMainPage.setControlMainPage(controlContainerPane);
        }
    }

    public void showRuntimeScene(boolean isDebug, boolean fullScreen)
    {
        var pageList = controlContainerDatabase.getPageList();
        if(!pageList.isEmpty())
        {
            //Disable all the non essentials stuff
            controlsPageCreationStage.setDisabled(true);
            dragAndDropPane.setDisabled(true);
            quickSetupPane.setDisabled(true);
            settingsStage.setDisabled(true);
            pictureBankStage.setDisabled(true);

            this.setShownControlContainerPane(null);

            if(isDebug)
            {
                runtimeControlMainPage.setExitKeyCombination();
            }

            runtimeControlMainPage.getStageSetter().setFullScreen(fullScreen, "", null);

            runtimeControlMainPage.showStage();
            runtimeControlMainPage.setControlMainPage(pageList.get(0));

            //Close all the pages
            this.hideStage();
            settingsStage.hideStage();
            controlWrapperSetupStage.hideStage();
            UndoRedoManager.getInstance().clear();
        }
    }

    public void setShownControlContainerPane(ControlContainerPane controlContainerPane)
    {
        if(this.shownControlContainerPane != null)
        {
            //If i don't clear selections some bad things could happen, especially while debugging read only page
            this.shownControlContainerPane.setActive(false);
            this.shownControlContainerPane.getMultipleSelectionManager().clearSelections();
            this.shownControlContainerPane.getMenuBottomImagePane().updateSnapshot();
        }

        this.shownControlContainerPane = controlContainerPane;

        AnchorPane anchorPane;
        if(controlContainerPane != null)
        {
            controlContainerPane.setActive(true);

            centerTopLabel.setText(controlContainerPane.getName());
            anchorPane = shownControlContainerPane.getMainAnchorPane();
        }
        else
        {
            centerTopLabel.setText("Not Selected");

            anchorPane = new AnchorPane();
            anchorPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        }

        var pageWidth = this.getPageWidth();
        var pageHeight = this.getPageHeight();
        anchorPane.setPrefSize(pageWidth, pageHeight);
        anchorPane.setMaxSize(pageWidth, pageHeight);

        var children = centerScrollStackPane.getChildren();
        children.clear();
        children.add(anchorPane);
    }

    public boolean runtimeFullScreen()
    {
        return runtimeFullScreenCheckBox.isSelected();
    }

    public boolean runtimeAtStartup()
    {
        return runtimeAtStartupCheckBox.isSelected();
    }

    @Override
    public void showStage()
    {
        //Enable all the stuff
        controlsPageCreationStage.setDisabled(false);
        dragAndDropPane.setDisabled(false);
        quickSetupPane.setDisabled(false);
        settingsStage.setDisabled(false);
        pictureBankStage.setDisabled(false);

        var pageList = this.getControlContainerDatabase().getPageList();
        if(!pageList.isEmpty())
        {
            this.setShownControlContainerPane(pageList.get(0));
        }

        super.showStage();
    }
}