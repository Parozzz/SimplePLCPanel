package parozzz.github.com.simpleplcpanel.hmi.tags.stage;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.skin.TreeTableCellSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.converter.DefaultStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.LocalCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.StringAddressDataCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Ref;

class TagStageUtil
{
    private final static Field TEXT_FIELD_TREE_CELL = ReflectionUtil.getField(TextFieldTreeTableCell.class, "textField");
    private final static Method LEFT_LABEL_PADDING_TREE_CELL_SKIN = ReflectionUtil.getMethod(TreeTableCellSkin.class, "leftLabelPadding");

    public static TreeTableColumn<Tag, String> createNameColumn()
    {
        var column = new TreeTableColumn<Tag, String>();
        column.setText("Name");
        column.setPrefWidth(250);
        column.setMinWidth(250);
        column.setSortable(false);
        column.setEditable(true);
        column.setCellValueFactory(features ->
        {
            Tag tag;
            if(features.getValue() == null || (tag = features.getValue().getValue()) == null)
            {
                return new SimpleObjectProperty<>();
            }

            return tag.keyValueProperty();
        });
        column.setCellFactory(tColumn ->
        {
            var cell = new TextFieldTreeTableCell<Tag, String>()
            {
                private boolean textFieldChanged = false;

                @Override
                public void startEdit()
                {
                    super.startEdit();

                    if(textFieldChanged)
                    {
                        return;
                    }

                    var textField = ReflectionUtil.getFieldValue(TEXT_FIELD_TREE_CELL, this, TextField.class);
                    if(textField != null)
                    {
                        textFieldChanged = true;

                        var skin = this.getSkin();
                        if(skin instanceof TreeTableCellSkin)
                        {
                            var leftPadding = ReflectionUtil.invokeMethod(LEFT_LABEL_PADDING_TREE_CELL_SKIN, skin, Double.class);
                            if(leftPadding != null)
                            {
                                textField.setPadding(new Insets(0,0,0,leftPadding));
                            }
                        }

                        textField.setBorder(null);
                        textField.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
                    }
                }
            };

            cell.setEditable(true);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(Insets.EMPTY);
            cell.setConverter(new DefaultStringConverter());
            return cell;
        });

        return column;
    }


    public static TreeTableColumn<Tag, Boolean> createLocalColumn(CommunicationDataHolder communicationDataHolder)
    {
        var column = new TreeTableColumn<Tag, Boolean>();
        column.setText("Local");
        column.setPrefWidth(40);
        column.setMinWidth(40);
        column.setSortable(false);
        column.setCellFactory(tColumn ->
        {
            var cellFactoryHandler = new LocalCellFactoryHandler(communicationDataHolder);
            cellFactoryHandler.init();
            return cellFactoryHandler.getCell();
        });
        return column;
    }

    public static TreeTableColumn<Tag, CommunicationStringAddressData> createAddressColumn(CommunicationDataHolder communicationDataHolder)
    {
        var column = new TreeTableColumn<Tag, CommunicationStringAddressData>();
        column.setText("Address");
        column.setPrefWidth(170);
        column.setMinWidth(170);
        column.setSortable(false);
        column.setCellFactory(tColumn ->
        {
            var cellFactoryHandler = new StringAddressDataCellFactoryHandler(communicationDataHolder);
            cellFactoryHandler.init();
            return cellFactoryHandler.getCell();
        });
        return column;
    }

    public static void moveUpDownEventHandler(TreeTableView<Tag> treeTableView, KeyEvent event)
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
    }

    private TagStageUtil() {}
}
