package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.CommunicationTypeCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.LocalCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.StringAddressDataCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.util.HashSet;
import java.util.Set;

public final class TagStage extends HMIStage<VBox>
{
    private final CommunicationDataHolder communicationDataHolder;

    private final TreeTableView<Tag> treeTableView;
    private final Tag rootFolderTag;

    private final Set<Tag> tagSet;

    public TagStage(CommunicationDataHolder communicationDataHolder)
    {
        super(new VBox());

        this.communicationDataHolder = communicationDataHolder;

        this.treeTableView = new TreeTableView<>();
        this.rootFolderTag = new FolderTag("root");

        this.tagSet = new HashSet<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStageSetter()
                .setAlwaysOnTop(true)
                .setResizable(true).stopWidthResize();

        var topLabel = new Label();
        topLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        topLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        topLabel.setText("Tags");
        topLabel.setAlignment(Pos.CENTER);
        topLabel.setBackground(FXUtil.createBackground(Color.GRAY));
        topLabel.setFont(Font.font(null, FontWeight.BOLD, FontPosture.REGULAR, 20));
        topLabel.setTextFill(Color.WHITESMOKE);
        super.parent.getChildren().add(topLabel);

        TreeTableColumn<Tag, String> nameColumn = new TreeTableColumn<>();
        nameColumn.setText("Name");
        nameColumn.setPrefWidth(250);
        nameColumn.setSortable(false);
        nameColumn.setCellValueFactory(features ->
        {
            var treeItem = features.getValue();
            Tag tag;
            if(treeItem == null || (tag = treeItem.getValue()) == null)
            {
                return new SimpleObjectProperty<>();
            }

            return tag.keyValueProperty();
        });
        nameColumn.setCellFactory(tColumn ->
        {
            TreeTableCell<Tag, String> cell = new TextFieldTreeTableCell<>();
            cell.setEditable(true);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(Insets.EMPTY);
            return cell;
        });

        TreeTableColumn<Tag, Boolean> localColumn = new TreeTableColumn<>();
        localColumn.setText("Local");
        localColumn.setPrefWidth(50);
        localColumn.setSortable(false);
        localColumn.setCellFactory(tColumn ->
        {
            var cellFactoryHandler = new LocalCellFactoryHandler(communicationDataHolder);
            cellFactoryHandler.init();
            return cellFactoryHandler.getCell();
        });

        TreeTableColumn<Tag, CommunicationStringAddressData> addressColumn = new TreeTableColumn<>();
        addressColumn.setText("Address");
        addressColumn.setPrefWidth(170);
        addressColumn.setSortable(false);

        addressColumn.setCellFactory(tColumn ->
        {
            var cellFactoryHandler = new StringAddressDataCellFactoryHandler(communicationDataHolder);
            cellFactoryHandler.init();
            return cellFactoryHandler.getCell();
        });


        treeTableView.setOnKeyPressed(event ->
        {
            var selectedIndex = treeTableView.getSelectionModel().getSelectedIndex();
            if(selectedIndex < 0 || !event.isAltDown())
            {
                return;
            }

            var treeItem = treeTableView.getTreeItem(selectedIndex);
            if(treeItem == null || treeItem.getParent() == null)
            {
                return;
            }

            var index = treeItem.getParent().getChildren().indexOf(treeItem);
            if(index < 0)
            {
                return;
            }

            var children = treeItem.getParent().getChildren();
            switch(event.getCode())
            {
                case UP:
                    if(index != 0)
                    {
                        children.remove(treeItem);
                        children.add(index - 1, treeItem);
                    }
                    break;
                case DOWN:
                    if(index != children.size() - 1)
                    {
                        children.remove(treeItem);
                        children.add(index + 1, treeItem);
                    }
                    break;
            }

            var selectionModel = treeTableView.getSelectionModel();
            selectionModel.select(treeItem);
            selectionModel.focus(selectionModel.getSelectedIndex());
        });
        treeTableView.setRowFactory(tTableView ->
        {
            var tableRow = new TreeTableRow<Tag>();
            tableRow.setPrefHeight(20);
            tableRow.setPadding(new Insets(0));
            return tableRow;
        });

        treeTableView.setMinSize(Region.USE_PREF_SIZE, 0);
        treeTableView.setMaxSize(Region.USE_PREF_SIZE, Double.MAX_VALUE);
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        treeTableView.setShowRoot(true);
        treeTableView.setRoot(rootFolderTag.createTreeItem());
        treeTableView.getColumns().addAll(nameColumn, localColumn, addressColumn);

        var stackPane = new StackPane(treeTableView);
        stackPane.setMinSize(0, 0);
        stackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(treeTableView, Pos.CENTER);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        super.parent.getChildren().add(stackPane);

        communicationDataHolder.getCommunicationStage().addCommunicationTypeListener(communicationType ->
        {
            tagSet.stream()
                    .filter(CommunicationTag.class::isInstance)
                    .map(CommunicationTag.class::cast)
                    .forEach(communicationTag -> communicationTag.updateCommunicationType(communicationType));
        });

        this.addTag(new CommunicationTag("Tag1"));
        this.addTag(new CommunicationTag("Tag2"));
        this.addTag(new CommunicationTag("Tag3"));
        this.addTag(new CommunicationTag("Tag4"));

        var aFolder = new FolderTag("Folder1!");
        this.addTag(aFolder);

        this.addTag(aFolder, new CommunicationTag("Tag5"));
        this.addTag(aFolder, new CommunicationTag("Tag6"));
        this.addTag(aFolder, new CommunicationTag("Tag7"));
        this.addTag(aFolder, new CommunicationTag("Tag8"));

        var aSecondFolder = new FolderTag("Folder2!");
        this.addTag(aSecondFolder);

        this.addTag(aSecondFolder, new CommunicationTag("Tag9"));
        this.addTag(aSecondFolder, new CommunicationTag("Tag10"));
        this.addTag(aSecondFolder, new CommunicationTag("Tag11"));
        this.addTag(aSecondFolder, new CommunicationTag("Tag12"));

    }

    public void addTag(Tag tag)
    {
        this.addTag(null, tag);
    }

    public void addTag(Tag folderTag, Tag tag)
    {
        if(tag.getTreeItem() == null && tagSet.add(tag))
        {
            folderTag = folderTag == null ? rootFolderTag : folderTag;

            var folderTreeItem = folderTag.getTreeItem();
            if(folderTreeItem != null)
            {
                folderTreeItem.getChildren().add(tag.createTreeItem());
            }

            if(tag instanceof CommunicationTag)
            {
                ((CommunicationTag) tag).updateCommunicationType(communicationDataHolder.getCurrentCommunicationType());
            }
        }
    }
}
