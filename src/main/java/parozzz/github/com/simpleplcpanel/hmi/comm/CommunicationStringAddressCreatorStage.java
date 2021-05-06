package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class CommunicationStringAddressCreatorStage<T>
        extends HMIStage<VBox>
{

    @FXML private StackPane confirmButtonStackPane;
    @FXML private Button confirmButton;

    @FXML protected TextField convertedAddressTextField;

    private Consumer<String> inputTextAddressConsumer;

    public CommunicationStringAddressCreatorStage(String resource) throws IOException
    {
        super(resource, VBox.class);
    }

    @Override
    public void setup()
    {
        super.setup();

        this.getStageSetter()
                //Clear the consumer on page close
                .addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event ->
                        inputTextAddressConsumer = null
                );

        confirmButton.setOnAction(event ->
        {
            var textAddress = convertedAddressTextField.getText();
            if (textAddress == null || textAddress.isEmpty())
            {
                return;
            }

            if (inputTextAddressConsumer != null)
            {
                inputTextAddressConsumer.accept(textAddress);
                inputTextAddressConsumer = null;
            }

            this.getStageSetter().close();
        });

    }

    @Override
    public void showStage()
    {
        this.updateTextConvertedAddress();

        var children = super.parent.getChildren();
        if (inputTextAddressConsumer == null)
        {
            children.remove(confirmButtonStackPane);
        } else
        {
            if (!children.contains(confirmButtonStackPane))
            {
                children.add(confirmButtonStackPane);
            }
        }

        super.showStage();
    }

    public void showAsStandalone(HMIStage<?> owner)
    {
        this.showAsInputTextAddress(owner, null);
    }

    public void showAsInputTextAddress(HMIStage<?> owner, Consumer<String> inputTextAddressConsumer)
    {
        this.setAsSubWindow(owner);

        this.inputTextAddressConsumer = inputTextAddressConsumer;
        this.showStage();
    }

    public abstract boolean loadStringDataToActualValues(String stringData);

    public abstract T createDataFromActualValues();

    public abstract void updateTextConvertedAddress();

}
