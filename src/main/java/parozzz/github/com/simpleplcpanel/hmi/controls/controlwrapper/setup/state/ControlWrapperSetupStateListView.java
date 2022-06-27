package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.state;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.WrapperStateCreationPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStageSetter;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.ListMultipleSelectionUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.IOException;

public final class ControlWrapperSetupStateListView extends FXController
{
    private final static Color MAIN_SELECTION_BORDER_COLOR = Color.LIGHTGREEN;
    private final static Color SECONDARY_SELECTION_BORDER_COLOR = Color.LIGHTGRAY;
    private final static int BORDER_WIDTH = 2;

    private final static Insets BORDERLESS_PADDING = new Insets(5);
    private final static Insets BORDER_PADDING = new Insets(BORDERLESS_PADDING.getTop() - BORDER_WIDTH);

    private final ControlWrapperSetupStage setupStage;
    private final ListView<WrapperState> listView;
    private final WrapperStateCreationPane wrapperStateCreationPane;
    private HMIStageSetter createStateStage;

    public ControlWrapperSetupStateListView(ControlWrapperSetupStage setupStage,
            ListView<WrapperState> listView) throws IOException
    {
        this.setupStage = setupStage;
        this.listView = listView;

        this.addFXChild(
                this.wrapperStateCreationPane = new WrapperStateCreationPane(setupStage,
                        this::onSuccessfulStateCreation)
        );
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        createStateStage = new HMIStageSetter(wrapperStateCreationPane.getParent())
                .setAlwaysOnTop(true)
                .setResizable(false)
                .initModality(Modality.WINDOW_MODAL)
                .initStyle(StageStyle.UTILITY)
                .initOwner(setupStage)
                .setTitle("Create new state");

        listView.setContextMenu(ContextMenuBuilder.builder()
                .simpleWithConsumer("Add", menuItem ->
                {
                    Robot robot = new Robot();

                    createStateStage.setPos(robot.getMouseX(), robot.getMouseY());
                    createStateStage.show();
                }).simple("Delete", () ->
                {
                    var selectedControlWrapper = setupStage.getSelectedControlWrapper();
                    if (selectedControlWrapper == null)
                    {
                        return;
                    }

                    setupStage.getStateSelection().forEach(selectedWrapperState ->
                            selectedControlWrapper.getStateMap().removeState(selectedWrapperState)
                    );

                    this.loadStates(); //Reload states after having them changed.

                }).simple("Select all", () ->
                {
                    for (var wrapperState : listView.getItems())
                    {
                        setupStage.getStateSelection().addSecondaryState(wrapperState);
                    }

                    this.loadStates(); //For reloading borders
                })
                .getContextMenu()
        );

        listView.setFocusTraversable(false);
        listView.setSelectionModel(new NoSelectionModel<>());
        listView.setEditable(false);
        listView.setCellFactory(tListView ->
        {
            var listCell = new ListCell<WrapperState>()
            {
                @Override
                protected void updateItem(WrapperState item, boolean empty)
                {
                    super.updateItem(item, empty);

                    String text = null;
                    Border border = null;
                    Insets padding = BORDERLESS_PADDING;

                    if (!(item == null || empty))
                    {
                        text = item.getStringVersion();

                        var stateSelection = setupStage.getStateSelection();
                        switch (stateSelection.getSelectionTypeOf(item))
                        {
                            case MAIN ->
                            {
                                padding = BORDER_PADDING;
                                border = FXUtil.createBorder(MAIN_SELECTION_BORDER_COLOR, BORDER_WIDTH);
                            }
                            case SECONDARY ->
                            {
                                padding = BORDER_PADDING;
                                border = FXUtil.createBorder(SECONDARY_SELECTION_BORDER_COLOR, BORDER_WIDTH);
                            }
                        }
                    }

                    this.setText(text);
                    this.setBorder(border);
                    this.setPadding(padding);
                }
            };

            listCell.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
            listCell.setOnMouseClicked(event ->
            {
                var selectedControlWrapper = setupStage.getSelectedControlWrapper();
                if (selectedControlWrapper == null)
                {
                    MainLogger.getInstance()
                            .warning("The StateList is still populated while the selected control wrapper is null",
                                    this);
                    this.clearStates(); //This should not ever happen
                    return;
                }

                var item = listCell.getItem();
                if (item == null)
                {
                    return;
                }

                var stateSelection = setupStage.getStateSelection();
                if (event.getButton() == MouseButton.PRIMARY)
                {
                    if (event.isControlDown())
                    {
                        stateSelection.addSecondaryState(item);
                    }
                    else if (event.isAltDown() || event.isShiftDown())
                    {
                        // Multiple selection while keep alt pressed down
                        var mainSelectedState = stateSelection.getMainState();
                        if (mainSelectedState == null)
                        {
                            return;
                        }

                        var selectedItemList = ListMultipleSelectionUtil.getAllMiddleValues(
                                listView.getItems(), mainSelectedState, item
                        );
                        if (selectedItemList == null)
                        {
                            return;
                        }

                        selectedItemList.forEach(stateSelection::addSecondaryState);
                    }
                    else
                    {
                        stateSelection.setMainState(item); //Set as main selection if ctrl is not pressed
                    }
                }
                else if (event.getButton() == MouseButton.SECONDARY)
                {
                    //If there is only one it means only the main is selected.
                    //In that case you change the main to the one you right click.
                    if (stateSelection.getCount() == 1)
                    {
                        stateSelection.setMainState(item);
                    }
                }

                this.loadStates(); //Reload state list (For the background)
            });

            return listCell;
        });
    }

    public void clearStates()
    {
        listView.setItems(null);
    }

    public void loadStates()
    {
        //This below, other than loading state also refresh the listview
        this.clearStates();

        var controlWrapper = setupStage.getSelectedControlWrapper();
        if (controlWrapper == null)
        {
            return;
        }

        ObservableList<WrapperState> stateList = FXCollections.observableArrayList();
        controlWrapper.getStateMap().forEach(stateList::add);
        listView.setItems(stateList);
    }

    private void onSuccessfulStateCreation()
    {
        createStateStage.hide();
    }
}
