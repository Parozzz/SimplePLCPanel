package parozzz.github.com.simpleplcpanel.hmi.main;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerCreationStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.runtime.RuntimeControlContainerStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.quicktext.ControlWrapperQuickTextEditorStage;
import parozzz.github.com.simpleplcpanel.hmi.database.ControlContainerDatabase;
import parozzz.github.com.simpleplcpanel.hmi.main.dragdrop.DragAndDropPane;
import parozzz.github.com.simpleplcpanel.hmi.main.others.MessagesListStage;
import parozzz.github.com.simpleplcpanel.hmi.main.others.copypaste.ControlWrapperCopyPasteHandler;
import parozzz.github.com.simpleplcpanel.hmi.main.picturebank.PictureBankStage;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.settings.SettingsStage;
import parozzz.github.com.simpleplcpanel.hmi.pane.BorderPaneHMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.TrigBoolean;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;

public final class MainEditStage extends BorderPaneHMIStage
{
    @FXML
    private CheckBox runtimeFullScreenCheckBox;
    @FXML
    private CheckBox runtimeAtStartupCheckBox;
    @FXML
    private MenuItem startRuntimeMenuItem;

    //File
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem settingsMenuItem;

    //ControlWrapperPage Data
    @FXML
    private MenuItem createPageMenuItem;
    @FXML
    private TextField zoomTextField;
    @FXML
    private TextField pageWidthTextField;
    @FXML
    private TextField pageHeightTextField;

    //View Menu
    @FXML
    private MenuItem viewQuickSetupMenuItem;
    @FXML
    private MenuItem viewDragAndDropMenuItem;
    @FXML
    private MenuItem viewScrollingPagesMenuItem;

    //Communication
    @FXML
    private Circle plcConnectedCircle;
    @FXML
    private MenuItem setupCommunicationMenuItem;

    //Tools Menu
    @FXML
    private MenuItem pictureBankMenuItem;
    @FXML
    private MenuItem modbusTCPStringAddressMenuItem;
    @FXML private MenuItem siemensS7StringAddressMenuItem;

    //Messages Menu
    @FXML
    private Label messagePresentLabel;
    @FXML
    private MenuItem showMessageListMenuItem;

    //Sides Stack Panes
    @FXML
    private StackPane leftStackPane;
    @FXML
    private StackPane rightStackPane;
    @FXML
    private StackPane bottomStackPane;

    //Center = Page Showing
    @FXML
    private Label centerTopLabel;
    @FXML
    private VBox centerMainVBox;
    @FXML
    private StackPane centerScrollStackPane;

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
    private final CommunicationStage communicationStage;
    private final MessagesListStage messagesListStage;
    private final ControlWrapperCopyPasteHandler copyPasteHandler;
    private final RuntimeControlContainerStage runtimeControlMainPage;

    private final TrigBoolean messagePresentTrig;
    private ControlContainerPane shownControlContainerPane;

    public MainEditStage(SiemensS7Thread plcThread, ModbusTCPThread modbusTCPThread,
            Runnable saveDataRunnable) throws IOException
    {
        super("Menu", "mainEditPane.fxml");

        this.saveDataRunnable = saveDataRunnable;


        super   //HANDLERS AND VARIOUS
                .addFXChild(controlContainerDatabase = new ControlContainerDatabase(this, plcThread, modbusTCPThread))
                .addFXChild(copyPasteHandler = new ControlWrapperCopyPasteHandler(this))
                //SIDE PANES
                .addFXChild(dragAndDropPane = new DragAndDropPane(this)) //LEFT
                .addFXChild(quickSetupPane = new QuickSetupPane()) //RIGHT
                .addFXChild(pageScrollingPane = new PageScrollingPane()) //BOTTOM
                //CHILD STAGES
                .addFXChild((controlsPageCreationStage = new ControlContainerCreationStage(controlContainerDatabase)).setAsSubWindow(this))
                .addFXChild((controlWrapperSetupStage = new ControlWrapperSetupStage(this)).setAsSubWindow(this))
                .addFXChild((controlWrapperQuickTextEditorStage = new ControlWrapperQuickTextEditorStage()).setAsSubWindow(this))
                .addFXChild((settingsStage = new SettingsStage()).setAsSubWindow(this))
                .addFXChild((pictureBankStage = new PictureBankStage()).setAsSubWindow(this))
                .addFXChild((communicationStage = new CommunicationStage(plcThread, modbusTCPThread)).setAsSubWindow(this))
                .addFXChild((messagesListStage = new MessagesListStage()).setAsSubWindow(this))
                //OTHER STAGES
                .addFXChild(runtimeControlMainPage = new RuntimeControlContainerStage(this));

        this.messagePresentTrig = new TrigBoolean(false);
    }

    @Override
    public void setup()
    {
        super.setup();

        var saveKeyCombination = KeyCombination.keyCombination("CTRL+S");

        super.getStageSetter().setResizable(true)
                .setMaximized(true)
                .addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
                {
                    if (saveKeyCombination.match(keyEvent))
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
        setupCommunicationMenuItem.setOnAction(event -> communicationStage.showStage());

        //Runtime Menu
        startRuntimeMenuItem.setOnAction(actionEvent -> this.showRuntimeScene(true, runtimeFullScreenCheckBox.isSelected()));

        //Page Menu
        createPageMenuItem.setOnAction(actionEvent -> controlsPageCreationStage.showStage());

        //Tools Menu
        pictureBankMenuItem.setOnAction(actionEvent -> pictureBankStage.showStage());
        modbusTCPStringAddressMenuItem.setOnAction(event ->
                controlWrapperSetupStage.getModbusTCPStringAddressCreatorStage().showAsStandalone(MainEditStage.this)
        );
        siemensS7StringAddressMenuItem.setOnAction(event ->
                controlWrapperSetupStage.getSiemensS7StringAddressCreator().showAsStandalone(MainEditStage.this)
        );

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
            for (var controlContainerPage : controlContainerDatabase)
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
            for (var controlContainerPage : controlContainerDatabase)
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
            if (scrollEvent.isControlDown())
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
    public void setDefault()
    {
        super.setDefault();

        zoomTextField.setText("100");
        pageWidthTextField.setText("640");
        pageHeightTextField.setText("480");
    }

    @Override
    public void loop()
    {
        super.loop();

        if (super.every(1000))
        {
            var selectedCommunicationManager = communicationStage.getSelectedCommunicationManager();
            if (selectedCommunicationManager != null)
            {
                var isConnected = selectedCommunicationManager.getCommThread().isConnected();
                plcConnectedCircle.setFill(isConnected ? Color.GREEN : Color.RED);
            }

            messagePresentTrig.set(messagesListStage.areMessagesPresent());
            switch (messagePresentTrig.checkTrig())
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
    public void setupComplete()
    {
        super.setupComplete();

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

    public CommunicationStage getCommunicationStage()
    {
        return communicationStage;
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
        if (this.isRuntimeShowing())
        {
            runtimeControlMainPage.setControlMainPage(controlContainerPane);
        }
    }

    public void showRuntimeScene(boolean isDebug, boolean fullScreen)
    {
        var pageList = controlContainerDatabase.getPageList();
        if (!pageList.isEmpty())
        {
            //Disable all the non essentials stuff
            controlsPageCreationStage.setDisabled(true);
            dragAndDropPane.setDisabled(true);
            quickSetupPane.setDisabled(true);
            settingsStage.setDisabled(true);
            pictureBankStage.setDisabled(true);

            this.setShownControlContainerPane(null);

            if (isDebug)
            {
                runtimeControlMainPage.setExitKeyCombination();
            }

            runtimeControlMainPage.getStageSetter().setFullScreen(fullScreen, "", null);

            runtimeControlMainPage.showStage();
            runtimeControlMainPage.setControlMainPage(pageList.get(0));

            //Close all the pages
            this.getStageSetter().close();
            settingsStage.getStageSetter().close();
            controlWrapperSetupStage.getStageSetter().close();
        }
    }

    public void setShownControlContainerPane(ControlContainerPane controlContainerPane)
    {
        if (shownControlContainerPane != null)
        {
            //If i don't clear selections some bad things could happen, especially while debugging read only page
            shownControlContainerPane.getSelectionManager().clearSelections();
            shownControlContainerPane.getMenuBottomImagePane().updateSnapshot();
        }

        this.shownControlContainerPane = controlContainerPane;

        AnchorPane anchorPane;
        if (shownControlContainerPane == null)
        {
            centerTopLabel.setText("Not Selected");

            anchorPane = new AnchorPane();
            anchorPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        } else
        {
            centerTopLabel.setText(controlContainerPane.getName());
            anchorPane = shownControlContainerPane.getMainAnchorPane();
        }

        var pageWidth = this.getPageWidth();
        var pageHeight = this.getPageHeight();
        anchorPane.setPrefSize(pageWidth, pageHeight);
        anchorPane.setMaxSize(pageWidth, pageHeight);

        var children = centerScrollStackPane.getChildren();
        children.clear();
        children.add(anchorPane);
        //centerScrollPane.setContent(anchorPane);

        //this.getStageSetter().get().sizeToScene();
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
        if (!pageList.isEmpty())
        {
            this.setShownControlContainerPane(pageList.get(0));
        }

        super.showStage();
    }
}