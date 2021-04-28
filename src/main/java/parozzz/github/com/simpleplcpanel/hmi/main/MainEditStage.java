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
import parozzz.github.com.simpleplcpanel.Main;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensPLCThread;
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
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupVBox;
import parozzz.github.com.simpleplcpanel.hmi.main.settings.SettingsStage;
import parozzz.github.com.simpleplcpanel.hmi.page.BorderPaneHMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.util.TrigBoolean;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;

public final class MainEditStage extends BorderPaneHMIStage
{
    @FXML
    private CheckBox setPageFullScreenCheckBox;
    @FXML
    private CheckBox setPageAtStartupCheckBox;
    @FXML
    private MenuItem startReadOnlyMenuItem;

    //Bottom
    @FXML
    private HBox bottomScrollingHBox;

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

    //Messages Menu
    @FXML
    private Label messagePresentLabel;
    @FXML
    private MenuItem showMessageListMenuItem;

    @FXML
    private StackPane leftStackPane;
    @FXML
    private StackPane rightStackPane;

    //Center = Page Showing
    @FXML
    private Label centerTopLabel;
    @FXML
    private VBox centerMainVBox;
    @FXML
    private StackPane centerScrollStackPane;

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
    private final RuntimeControlContainerStage runtimeControlMainPage;

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
        modbusTCPStringAddressMenuItem.setOnAction(event ->
                ModbusTCPStringAddressCreatorStage.getInstance().showAsStandalone()
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
            for(var controlContainerPage : controlContainerDatabase)
            {
                var anchorPane = controlContainerPage.getMainAnchorPane();
                anchorPane.setPrefWidth(newWidth);
                anchorPane.setMaxWidth(newWidth);
            }
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
        });

        leftStackPane.getChildren().add(dragAndDropPane.getMainVBox());
        rightStackPane.getChildren().add(quickSetupVBox.getMainParent());
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

        if(super.every(1000))
        {
            var selectedCommunicationManager = communicationStage.getSelectedCommunicationManager();
            if(selectedCommunicationManager != null)
            {
                var isConnected = selectedCommunicationManager.getCommThread().isConnected();
                plcConnectedCircle.setFill(isConnected ? Color.GREEN : Color.RED);
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
        return runtimeControlMainPage.getStageSetter().isShowing();
    }

    public void changeReadOnlyPage(ControlContainerPane controlContainerPane)
    {
        if(this.isReadOnlyShowing())
        {
            runtimeControlMainPage.setControlMainPage(controlContainerPane);
        }
    }

    public void showReadOnlyControlMainPage(boolean isDebug, boolean fullScreen)
    {
        //Disable all the non essentials stuff
        controlsPageCreationPage.setDisabled(true);
        dragAndDropPane.setDisabled(true);
        quickSetupVBox.setDisabled(true);
        settingsStage.setDisabled(true);
        pictureBankStage.setDisabled(true);

        this.setShownControlContainerPane(null);

        var pageList = controlContainerDatabase.getPageList();
        if(!pageList.isEmpty())
        {
            if(isDebug)
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
        if(shownControlContainerPane != null)
        {
            //If i don't clear selections some bad things could happen, especially while debugging read only page
            shownControlContainerPane.getSelectionManager().clearSelections();
            shownControlContainerPane.getMenuBottomImagePane().updateSnapshot();
        }

        this.shownControlContainerPane = controlContainerPane;

        AnchorPane anchorPane;
        if(shownControlContainerPane == null)
        {
            centerTopLabel.setText("Not Selected");

            anchorPane = new AnchorPane();
            anchorPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        }else
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

    public boolean showPageFullScreen()
    {
        return setPageFullScreenCheckBox.isSelected();
    }

    public void start()
    {
        var startProperties = Main.START_PROPERTIES;
        this.showStage();
    }
}