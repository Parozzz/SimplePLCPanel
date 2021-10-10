package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import org.controlsfx.control.PopOver;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
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

    private PopOver createStatePopOver;

    public ControlWrapperSetupStateListView(ControlWrapperSetupStage setupStage,
            ListView<WrapperState> listView) throws IOException
    {
        this.setupStage = setupStage;
        this.listView = listView;

        this.addFXChild(
                this.wrapperStateCreationPane = new WrapperStateCreationPane(setupStage)
        );
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        createStatePopOver = new PopOver();
        createStatePopOver.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        createStatePopOver.setDetachable(false);
        createStatePopOver.setContentNode(wrapperStateCreationPane.getParent());
        createStatePopOver.setHideOnEscape(true);

        //This part here is to address a bug in the PopOver where the animation will try to play even after closing
        //parent window keeping it alive and cause a fatal error which make the app crash :'C
        setupStage.getStageSetter().setOnWindowCloseRequest(event -> {
            createStatePopOver.setAnimated(false);
        });

        listView.setContextMenu(ContextMenuBuilder.builder()
                .simpleWithConsumer("Add", menuItem ->
                {
                    Robot robot = new Robot();

                    createStatePopOver.setAnimated(true);
                    createStatePopOver.show(listView, robot.getMouseX(), robot.getMouseY());
                }).simple("Select all", () ->
                {
                    for (var wrapperState : listView.getItems())
                    {
                        setupStage.addSecondarySelectedState(wrapperState);
                    }

                    this.loadStates(); //For reloading borders
                })
                .getContextMenu()
        );

        listView.setFocusTraversable(false);
        listView.setSelectionModel(new NoMultipleSelectionModel<>());
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
                        if (setupStage.getMainSelectedState() == item) //I WANT IDENTITY HERE!
                        {
                            padding = BORDER_PADDING;
                            border = FXUtil.createBorder(MAIN_SELECTION_BORDER_COLOR, BORDER_WIDTH);
                        } else if (setupStage.isSecondarySelectedState(item))
                        {
                            padding = BORDER_PADDING;
                            border = FXUtil.createBorder(SECONDARY_SELECTION_BORDER_COLOR, BORDER_WIDTH);
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
                if (event.getButton() != MouseButton.PRIMARY)
                {
                    return;
                }

                var selectedControlWrapper = setupStage.getSelectedControlWrapper();
                if (selectedControlWrapper == null)
                {
                    MainLogger.getInstance().warning("The StateList is still populated while the selected control wrapper is null", this);
                    this.clearStates(); //This should not ever happen
                    return;
                }

                var item = listCell.getItem();
                if (item != null)
                {
                    if (event.isControlDown())
                    {
                        //This will set the main if not be selected initially.
                        setupStage.addSecondarySelectedState(item);
                    } else
                    {
                        setupStage.setMainSelectedState(item); //Set as main selection if ctrl is not pressed
                    }

                    this.loadStates(); //Reload state list (For the background)
                }

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

    private static class NoMultipleSelectionModel<T> extends MultipleSelectionModel<T>
    {
        @Override
        public void clearAndSelect(int index)
        {

        }

        @Override
        public void select(int index)
        {

        }

        @Override
        public void select(T obj)
        {

        }

        @Override
        public void clearSelection(int index)
        {

        }

        @Override
        public void clearSelection()
        {

        }

        @Override
        public boolean isSelected(int index)
        {
            return false;
        }

        @Override
        public boolean isEmpty()
        {
            return true;
        }

        @Override
        public void selectPrevious()
        {

        }

        @Override
        public void selectNext()
        {

        }

        @Override
        public ObservableList<Integer> getSelectedIndices()
        {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<T> getSelectedItems()
        {
            return FXCollections.emptyObservableList();
        }

        @Override
        public void selectIndices(int index, int... indices)
        {

        }

        @Override
        public void selectAll()
        {

        }

        @Override
        public void selectFirst()
        {

        }

        @Override
        public void selectLast()
        {

        }
    }
}
