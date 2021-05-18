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
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.LocalCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.StringAddressDataCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TagStage extends HMIStage<VBox>
{
    public static int LAST_INTERNAL_ID = 1; //0 is ALWAYS the root

    private final CommunicationDataHolder communicationDataHolder;

    private final TreeTableView<Tag> treeTableView;
    private final Tag rootFolderTag;

    private final Set<Tag> tagSet;
    private final Map<Integer, Tag> tagMap;

    public TagStage(CommunicationDataHolder communicationDataHolder)
    {
        super(new VBox());

        this.communicationDataHolder = communicationDataHolder;

        this.treeTableView = new TreeTableView<>();
        this.rootFolderTag = new FolderTag("root", 0);

        this.tagSet = new HashSet<>();
        this.tagMap = new HashMap<>();
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
/*
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
        this.addTag(aSecondFolder, new CommunicationTag("Tag12"));*/
    }

    public Tag getTagFromId(int internalId)
    {
        return tagMap.get(internalId);
    }

    public void addTag(Tag tag)
    {
        this.addTag(null, tag);
    }

    public void addTag(Tag folderTag, Tag tag)
    {
        if(tag.getTreeItem() == null && tagSet.add(tag))
        {
            tagMap.put(tag.getInternalId(), tag);

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
                    this.removeTag(tag)
            );
        }
    }

    private void removeTag(Tag tag)
    {
        tagSet.remove(tag);
        tagMap.remove(tag.getInternalId());
    }

    @Override
    public JSONDataMap serialize()
    {
        var jsonDataMap = new JSONDataMap();
        jsonDataMap.set("LastInternalID", TagStage.LAST_INTERNAL_ID);

        var rootTreeItem = rootFolderTag.getTreeItem();
        if(rootTreeItem != null)
        {
            var rootJSONDataArray = new JSONDataArray();
            this.serializeTreeItem(rootTreeItem, rootJSONDataArray);
            jsonDataMap.set("Root", rootJSONDataArray);
        }

        return jsonDataMap;
    }

    @Nullable
    private void serializeTreeItem(TreeItem<Tag> treeItem,
            JSONDataArray folderJSONDataArray)
    {
        if(treeItem == null)
        {
            return;
        }

        for(var child : treeItem.getChildren())
        {
            var childTag = child.getValue();
            if(childTag == null)
            {
                continue;
            }

            var childJSONDataMap = new JSONDataMap();
            childJSONDataMap.set("InternalID", childTag.getInternalId());
            childJSONDataMap.set("Key", childTag.getKey());

            if(childTag instanceof FolderTag)
            {
                childJSONDataMap.set("Folder", true);

                var childFolderJSONDataArray = new JSONDataArray();
                this.serializeTreeItem(childTag.getTreeItem(), childFolderJSONDataArray);
                childJSONDataMap.set("SubKeys", childFolderJSONDataArray);
            }
            else if(childTag instanceof CommunicationTag)
            {
                var commTag = (CommunicationTag) childTag;

                childJSONDataMap.set("Local", commTag.isLocal());

                var stringAddressDataJSONDataMap = new JSONDataMap();
                for(var communicationType : CommunicationType.values())
                {
                    var stringAddressData = commTag.getCommunicationTypeStringAddressData(communicationType);
                    if(stringAddressData != null)
                    {
                        stringAddressDataJSONDataMap.set(communicationType.getName(), stringAddressData.getStringData());
                    }
                }
                childJSONDataMap.set("CommData", stringAddressDataJSONDataMap);
            }

            folderJSONDataArray.add(childJSONDataMap);
        }
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        if(jsonDataMap == null || jsonDataMap.size() == 0)
        {
            return;
        }

        LAST_INTERNAL_ID = jsonDataMap.getNumberOrZero("LastInternalID").intValue();

        var rootTreeItem = rootFolderTag.getTreeItem();
        var rootJSONDataArray = jsonDataMap.getArray("Root");
        if(rootTreeItem == null || rootJSONDataArray == null)
        {
            return;
        }

        for(var obj : rootJSONDataArray)
        {
            if(!(obj instanceof JSONObject))
            {
                continue;
            }

            var childJSONDataMap = new JSONDataMap((JSONObject) obj);
            this.parseTag(rootFolderTag, childJSONDataMap);
        }
    }

    private Tag parseTag(Tag folderTag, JSONDataMap jsonDataMap)
    {
        var internalID = jsonDataMap.getNumber("InternalID");
        var key = jsonDataMap.getString("Key");

        if(internalID == null || key == null)
        {
            return null;
        }

        if(jsonDataMap.getBoolean("Folder"))
        {
            var childFolderTag = new FolderTag(key, internalID.intValue());
            this.addTag(folderTag, childFolderTag);

            var folderTreeItem = childFolderTag.getTreeItem();
            if(folderTreeItem == null)
            {
                return null;
            }

            var subKeysJSONDataArray = jsonDataMap.getArray("SubKeys");
            if(subKeysJSONDataArray != null)
            {
                for(var obj : subKeysJSONDataArray)
                {
                    if(!(obj instanceof JSONObject))
                    {
                        continue;
                    }

                    var childJSONDataMap = new JSONDataMap((JSONObject) obj);
                    this.parseTag(childFolderTag, childJSONDataMap);
                }
            }

            return childFolderTag;
        }
        else
        {
            var tag = new CommunicationTag(key, internalID.intValue());
            tag.setLocal(jsonDataMap.getBoolean("Local"));

            var commDataJSONDataMap = jsonDataMap.getMap("CommData");
            if(commDataJSONDataMap != null)
            {
                for(var communicationType : CommunicationType.values())
                {
                    var stringData = commDataJSONDataMap.getString(communicationType.getName());
                    if(stringData != null && !stringData.isEmpty())
                    {
                        var addressStringData = communicationType.parseStringAddressData(stringData);
                        if(addressStringData != null)
                        {
                            tag.setCommunicationTypeStringAddressData(communicationType, addressStringData);
                        }
                    }
                }
            }

            //This needs to be done after because it bounds values together and then
            //WON'T be able to set the whole "CommData" above!
            this.addTag(folderTag, tag);
            return tag;
        }
    }
}
