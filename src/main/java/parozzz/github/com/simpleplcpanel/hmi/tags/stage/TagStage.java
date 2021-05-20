package parozzz.github.com.simpleplcpanel.hmi.tags.stage;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.converter.DefaultStringConverter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializable;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONData;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.FolderTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.LocalCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.StringAddressDataCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TagStage extends HMIStage<VBox>
{
    public static TagStage showStandalone(TagsManager tagsManager, CommunicationDataHolder communicationDataHolder,
            HMIStage<?> hmiStage)
    {
        var tagStage = new TagStage(tagsManager, communicationDataHolder);
        tagStage.setup();
        tagStage.setAsSubWindow(hmiStage);
        tagStage.showStage();
        return tagStage;
    }

    public static TagStage showAsInput(TagsManager tagsManager, CommunicationDataHolder communicationDataHolder,
            HMIStage<?> hmiStage, Consumer<CommunicationTag> selectTagConsumer)
    {
        var tagStage = new TagStage(tagsManager, communicationDataHolder);
        tagStage.setup();
        tagStage.setAsSubWindow(hmiStage);
        tagStage.showAsSelection(selectTagConsumer);
        return tagStage;
    }

    private final TagsManager tagsManager;
    private final CommunicationDataHolder communicationDataHolder;

    private final TreeTableView<Tag> treeTableView;
    private final TagStageSelectionHandler selectionHandler;

    private TagStage(TagsManager tagsManager, CommunicationDataHolder communicationDataHolder)
    {
        super(new VBox());

        this.tagsManager = tagsManager;
        this.communicationDataHolder = communicationDataHolder;

        this.treeTableView = new TreeTableView<>();
        this.addFXChild(this.selectionHandler = new TagStageSelectionHandler(this));
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStageSetter()
                .initModality(Modality.APPLICATION_MODAL)
                .initStyle(StageStyle.UTILITY)
                .setAlwaysOnTop(true)
                .setResizable(true)
                .setOnWindowCloseRequest(event -> selectionHandler.setSelectTagConsumer(null));//.stopWidthResize();

        var topLabel = new Label();
        topLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        topLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        topLabel.setText("Tags");
        topLabel.setAlignment(Pos.CENTER);
        topLabel.setBackground(FXUtil.createBackground(Color.GRAY));
        topLabel.setFont(Font.font(null, FontWeight.BOLD, FontPosture.REGULAR, 20));
        topLabel.setTextFill(Color.WHITESMOKE);
        super.parent.getChildren().add(topLabel);

        var nameColumn = TagStageUtil.createNameColumn();
        var localColumn = TagStageUtil.createLocalColumn(communicationDataHolder);
        var addressColumn = TagStageUtil.createAddressColumn(communicationDataHolder);

        treeTableView.setRowFactory(tTableView ->
        {
            var tableRow = new TreeTableRow<Tag>();
            tableRow.setPrefHeight(20);
            tableRow.setPadding(new Insets(0));
            tableRow.treeItemProperty().addListener((observable, oldValue, newValue) ->
            {
                if(newValue == null || newValue.getValue() == null)
                {
                    tableRow.setContextMenu(null);
                    return;
                }

                tableRow.setContextMenu(newValue.getValue().createContextMenu());
            });
            return tableRow;
        });

        treeTableView.setOnKeyPressed(event ->
                TagStageUtil.moveUpDownEventHandler(treeTableView, event)
        );

        treeTableView.setEditable(true);
        treeTableView.setMinSize(Region.USE_PREF_SIZE, 0);
        treeTableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        treeTableView.setShowRoot(true);
        treeTableView.setRoot(tagsManager.getRootItem());
        treeTableView.getColumns().addAll(nameColumn, localColumn, addressColumn);
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        var stackPane = new StackPane(treeTableView);
        stackPane.setMinSize(0, 0);
        stackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(treeTableView, Pos.CENTER);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        super.parent.getChildren().add(stackPane);

    }

    TreeTableView<Tag> getTreeTableView()
    {
        return treeTableView;
    }

    @Override
    public void showStage()
    {
        selectionHandler.setSelectTagConsumer(null);

        var selectionHandlerParent = selectionHandler.getMainParent();
        super.parent.getChildren().remove(selectionHandlerParent);

        super.showStage();
    }

    public void showAsSelection(Consumer<CommunicationTag> selectTagConsumer)
    {
        selectionHandler.setSelectTagConsumer(selectTagConsumer);

        var selectionHandlerParent = selectionHandler.getMainParent();

        var children = super.parent.getChildren();
        if(!children.contains(selectionHandlerParent))
        {
            children.add(selectionHandlerParent);
        }

        super.showStage();
    }
}
