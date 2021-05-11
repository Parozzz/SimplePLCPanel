package parozzz.github.com.simpleplcpanel.hmi.util;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableRow;

import java.util.function.Consumer;

public final class FXTableUtil
{
    public static <S, T> TreeTableCell<S, T> createTreeTableCell(TableCellConsumer<S, T> tableCellConsumer)
    {
        return new TreeTableCell<>()
        {
            @Override
            public void updateItem(T item, boolean empty)
            {
                super.updateItem(item, empty);
                tableCellConsumer.accept(this, item, empty);
            }
        };
    }

    public static <S, T> TreeTableCell<S, T> createTreeTableCell(
            TableCellConsumer<S, T> tableCellConsumer, Consumer<TreeTableCell<S, T>> layoutConsumer
    )
    {
        return new TreeTableCell<>()
        {
            @Override
            public void updateItem(T item, boolean empty)
            {
                super.updateItem(item, empty);
                tableCellConsumer.accept(this, item, empty);
            }

            @Override
            protected void layoutChildren() {
                layoutConsumer.accept(this);

                super.layoutChildren();
            }
        };
    }

    public static <S> TreeTableCell<S, String> createSimpleTreeTableCell()
    {
        return new TreeTableCell<>()
        {
            @Override
            public void updateItem(String item, boolean empty)
            {
                if (empty || item == null)
                {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(item);
            }
        };
    }

    public static <R> TreeTableRow<R> createTreeTableRow(TableRowConsumer<R> consumer)
    {
        return new TreeTableRow<>()
        {
            @Override
            public void updateItem(R item, boolean empty)
            {
                super.updateItem(item, empty);

                consumer.accept(this, item, empty);
            }
        };
    }

    public interface TableRowConsumer<R>
    {
        void accept(TreeTableRow<R> row, R item, boolean empty);
    }

    public interface TableCellConsumer<S,T>
    {
        void accept(TreeTableCell<S, T> cell, T item, boolean empty);
    }

    private FXTableUtil()
    {}
}
