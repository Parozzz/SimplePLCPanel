package parozzz.github.com.hmi.main.others;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import parozzz.github.com.hmi.page.HMIStage;
import parozzz.github.com.logger.MainLogger;

import java.io.IOException;

public class MessagesListStage extends HMIStage<VBox>
{
    @FXML private ListView<String> messagesListView;
    @FXML private Button clearMessagesButton;
    @FXML private Button refreshButton;

    public MessagesListStage() throws IOException
    {
        super("MessagesListStage", "messagesVBox.fxml", VBox.class);
    }

    @Override
    public void setup()
    {
        super.setup();

        var stageSetter = this.getStageSetter()
                .setResizable(true)
                .setTitle("Messages");

        messagesListView.setEditable(false);
        stageSetter.addEventHandler(WindowEvent.WINDOW_SHOWING, windowEvent -> this.refreshList());

        refreshButton.setOnAction(actionEvent -> this.refreshList());

        clearMessagesButton.setOnAction(actionEvent ->
        {
            MainLogger.getInstance().getMessageQueue().clear();
            this.refreshList();
        });
    }

    private void refreshList()
    {
        var listViewItems = messagesListView.getItems();
        listViewItems.clear();

        var messageQueue = MainLogger.getInstance().getMessageQueue();
        listViewItems.addAll(messageQueue);
    }
}
