package parozzz.github.com.simpleplcpanel.hmi.main.others;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.IOException;

public class MessagesListStage extends HMIStage<VBox>
{
    @FXML private ListView<String> messagesListView;
    @FXML private Button clearMessagesButton;
    @FXML private Button refreshButton;

    public MessagesListStage() throws IOException
    {
        super("messagesVBox.fxml", VBox.class);
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

    public boolean areMessagesPresent()
    {
        return !messagesListView.getItems().isEmpty() ||
                !MainLogger.getInstance().getMessageQueue().isEmpty();
    }

    private void refreshList()
    {
        var listViewItems = messagesListView.getItems();
        listViewItems.clear();

        var messageQueue = MainLogger.getInstance().getMessageQueue();
        listViewItems.addAll(messageQueue);
    }
}
