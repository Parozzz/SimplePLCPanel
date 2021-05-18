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
import javafx.util.converter.DefaultStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.FolderTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;
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
                .setResizable(true);//.stopWidthResize();

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
            tableRow.setPrefHeight(25);
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
        treeTableView.setRoot(rootFolderTag.init(this));
        treeTableView.getColumns().addAll(nameColumn, localColumn, addressColumn);

        var stackPane = new StackPane(treeTableView);
        stackPane.setMinSize(0, 0);
        stackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(treeTableView, Pos.CENTER);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        super.parent.getChildren().add(stackPane);

        communicationDataHolder.getCommunicationStage().addCommunicationTypeListener(communicationType ->
                tagSet.stream()
                        .filter(CommunicationTag.class::isInstance)
                        .map(CommunicationTag.class::cast)
                        .forEach(communicationTag -> communicationTag.updateCommunicationType(communicationType))
        );

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
                folderTreeItem.getChildren().add(tag.init(this));
            }

            if(tag instanceof CommunicationTag)
            {
                ((CommunicationTag) tag).updateCommunicationType(communicationDataHolder.getCurrentCommunicationType());
            }

            tag.addDeleteRunnable(() ->
                    tagSet.remove(tag)
            );
        }
    }
}
