package parozzz.github.com.hmi.main;

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
import parozzz.github.com.Main;
import parozzz.github.com.StartProperties;
import parozzz.github.com.hmi.comm.CommunicationStage;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.hmi.comm.siemens.SiemensPLCThread;
import parozzz.github.com.hmi.controls.ControlContainerCreationStage;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.ReadOnlyControlContainerStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.quicktext.ControlWrapperQuickTextEditorStage;
import parozzz.github.com.hmi.database.ControlContainerDatabase;
import parozzz.github.com.hmi.main.dragdrop.DragAndDropPane;
import parozzz.github.com.hmi.main.others.ControlWrapperCopyPasteHandler;
import parozzz.github.com.hmi.main.others.MessagesListStage;
import parozzz.github.com.hmi.main.picturebank.PictureBankStage;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupVBox;
import parozzz.github.com.hmi.main.settings.SettingsStage;
import parozzz.github.com.hmi.page.BorderPaneHMIStage;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.util.TrigBoolean;
import parozzz.github.com.util.Util;

import java.io.IOException;

public final class MainEditStage extends BorderPaneHMIStage
{
    @FXML private CheckBox setPageFullScreenCheckBox;
    @FXML private CheckBox setPageAtStartupCheckBox;
    @FXML private MenuItem startReadOnlyMenuItem;

    //Bottom
    @FXML private HBox bottomScrollingHBox;

    //File
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem settingsMenuItem;

    //ControlWrapperPage Data
    @FXML private MenuItem createPageMenuItem;
    @FXML private TextField zoomTextField;
    @FXML private TextField pageWidthTextField;
    @FXML private TextField pageHeightTextField;

    //Communication
    @FXML private Circle plcConnectedCircle;
    @FXML private MenuItem setupCommunicationMenuItem;

    //Tools Menu
    @FXML private MenuItem pictureBankMenuItem;

    //Messages Menu
    @FXML private Label messagePresentLabel;
    @FXML private MenuItem showMessageListMenuItem;

    @FXML private StackPane leftStackPane;
    @FXML private StackPane rightStackPane;

    //Center = Page Showing
    @FXML private Label centerTopLabel;
    @FXML private VBox centerMainVBox;
    @FXML private StackPane centerScrollStackPane;

    private final Runnable saveDataRunnable;

    private final ControlContainerDatabase controlContainerDatabase;
    private final ControlContainerCreationStage controlsPageCreationPage;
    private final ControlWrapperSetupStage controlWrapperSetupStage;
    private final ControlWrapperQuickTextEditorStage controlWrapperQuickTextEditorStage;
    private final DragAndDropPane dragAndDropPane;
    private final MainEditBottomScrollingPane bottomScrollingPane;
    private final QuickSetupVBox quickSetupVBox;
    private final SettingsStage settingsStage;
    private final PictureBankStage pictureBankStage;
    private final CommunicationStage communicationStage;
    private final MessagesListStage messagesListStage;
    private final ControlWrapperCopyPasteHandler copyPasteHandler;
    private final ReadOnlyControlContainerStage readOnlyControlMainPage;

    private final TrigBoolean messagePresentTrig;
    private ControlContainerPane shownControlContainerPane;

    public MainEditStage(SiemensPLCThread plcThread, ModbusTCPThread modbusTCPThread,
            Runnable saveDataRunnable) throws IOException
    {
        super("Menu", "mainEditPane.fxml");

        this.saveDataRunnable = saveDataRunnable;

        bottomScrollingPane = new MainEditBottomScrollingPane(bottomScrollingHBox);
        super.addFXChild(controlContainerDatabase = new ControlContainerDatabase(this, plcThread, modbusTCPThread))
                .addFXChild(controlsPageCreationPage = new ControlContainerCreationStage(controlContainerDatabase))
                .addFXChild(controlWrapperSetupStage = new ControlWrapperSetupStage(this))
                .addFXChild(controlWrapperQuickTextEditorStage = new ControlWrapperQuickTextEditorStage())
                .addFXChild(dragAndDropPane = new DragAndDropPane(this))
                .addFXChild(quickSetupVBox = new QuickSetupVBox())
                .addFXChild(settingsStage = new SettingsStage())
                .addFXChild(pictureBankStage = new PictureBankStage())
                .addFXChild(communicationStage = new CommunicationStage(plcThread, modbusTCPThread))
                .addFXChild(messagesListStage = new MessagesListStage())
                .addFXChild(copyPasteHandler = new ControlWrapperCopyPasteHandler(this))
                .addFXChild(readOnlyControlMainPage = new ReadOnlyControlContainerStage(this));

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
                    if(saveKeyCombination.match(keyEvent))
                    {
                        saveMenuItem.fire();
                        keyEvent.consume();
                    }
                });

        super.serializableDataSet.addString("PageHeight", pageHeightTextField.textProperty())
                .addString("PageWidth", pageWidthTextField.textProperty())
                .addBoolean("SetPageFullScreen", setPageFullScreenCheckBox.selectedProperty())
                .addBoolean("SetPageAtStartup", setPageAtStartupCheckBox.selectedProperty());

        settingsStage.getStageSetter().setResizable(true);

        //File Menu
        settingsMenuItem.setOnAction(actionEvent -> settingsStage.showStage());
        saveMenuItem.setOnAction(event -> saveDataRunnable.run());

        //Communication Menu
        setupCommunicationMenuItem.setOnAction(event -> communicationStage.showStage());

        //Setup Menu
        startReadOnlyMenuItem.setOnAction(actionEvent -> this.showReadOnlyControlMainPage(true, setPageFullScreenCheckBox.isSelected()));

        //Page Menu
        createPageMenuItem.setOnAction(actionEvent -> controlsPageCreationPage.showStage());

        //Tools Menu
        pictureBankMenuItem.setOnAction(actionEvent -> pictureBankStage.showStage());
        messagePresentLabel.setVisible(false);

        //Messages Menu
        showMessageListMenuItem.setOnAction(event -> messagesListStage.showStage());

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

            controlContainerDatabase.getPageList().forEach(controlMainPage -> controlMainPage.getMainAnchorPane().setPrefWidth(newWidth));
            if (shownControlContainerPane != null)
            {
                shownControlContainerPane.getMainAnchorPane().setPrefWidth(newWidth);
                //this.getStageSetter().get().sizeToScene();
            }
        });

        pageHeightTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(4));
        pageHeightTextField.textProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var newHeight = Util.parseInt(newValue, 480);

            controlContainerDatabase.getPageList().forEach(controlMainPage -> controlMainPage.getMainAnchorPane().setPrefHeight(newHeight));
            if (shownControlContainerPane != null)
            {
                shownControlContainerPane.getMainAnchorPane().setPrefHeight(newHeight);
                //this.getStageSetter().get().sizeToScene();
            }
        });

        leftStackPane.getChildren().add(dragAndDropPane.getMainVBox());
        rightStackPane.getChildren().add(quickSetupVBox.getMainParent());
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

    public QuickSetupVBox getQuickPropertiesVBox()
    {
        return quickSetupVBox;
    }

    public MainEditBottomScrollingPane getBottomScrollingPane()
    {
        return bottomScrollingPane;
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

    public boolean isReadOnlyShowing()
    {
        return readOnlyControlMainPage.getStageSetter().isShowing();
    }

    public void changeReadOnlyPage(ControlContainerPane controlContainerPane)
    {
        if (this.isReadOnlyShowing())
        {
            readOnlyControlMainPage.setControlMainPage(controlContainerPane);
        }
    }

    public void showReadOnlyControlMainPage(boolean isDebug, boolean fullScreen)
    {
        this.setShownControlContainerPane(null);

        var pageList = controlContainerDatabase.getPageList();
        if (!pageList.isEmpty())
        {
            if (isDebug)
            {
                readOnlyControlMainPage.setExitKeyCombination();
            }

            readOnlyControlMainPage.getStageSetter().setFullScreen(fullScreen, "", null);

            readOnlyControlMainPage.showStage();
            readOnlyControlMainPage.setControlMainPage(pageList.get(0));

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
            anchorPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            anchorPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            anchorPane.setPrefSize(this.getPageWidth(), this.getPageHeight());
        } else
        {
            centerTopLabel.setText(controlContainerPane.getName());
            anchorPane = shownControlContainerPane.getMainAnchorPane();
        }

        anchorPane.setPrefSize(this.getPageWidth(), this.getPageHeight());

        var children = centerScrollStackPane.getChildren();
        children.clear();
        children.add(anchorPane);
        //centerScrollPane.setContent(anchorPane);

        //this.getStageSetter().get().sizeToScene();
    }

    public boolean showPageFullScreen()
    {
        return setPageFullScreenCheckBox.isSelected();
    }

    public void start()
    {
        var startProperties = Main.START_PROPERTIES;
        if (startProperties.getBoolean(StartProperties.PropertyEnum.SHOW_EDIT_SCREEN))
        {
            this.showStage();
        } else
        {
            //Disable all the non essentials stuff
            controlsPageCreationPage.setDisabled(true);
            dragAndDropPane.setDisabled(true);
            quickSetupVBox.setDisabled(true);
            settingsStage.setDisabled(true);
            pictureBankStage.setDisabled(true);

            var fullScreen = startProperties.getBoolean(StartProperties.PropertyEnum.SHOW_FULL_SCREEN);
            this.showReadOnlyControlMainPage(false, fullScreen);
        }
    }
}