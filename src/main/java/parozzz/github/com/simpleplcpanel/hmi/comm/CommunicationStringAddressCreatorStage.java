package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;

import java.io.IOException;

public abstract class CommunicationStringAddressCreatorStage<T extends CommunicationStringAddressData>
        extends HMIStage<VBox>
{
    @FXML private StackPane confirmButtonStackPane;
    @FXML private Button confirmButton;

    @FXML protected TextField convertedAddressTextField;

    private final CommunicationType<T> communicationType;
    private CommunicationTag communicationTag;
    public CommunicationStringAddressCreatorStage(CommunicationType<T> communicationType, String resource) throws IOException
    {
        super(resource, VBox.class);

        this.communicationType = communicationType;
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        this.getStageSetter()
                .setAlwaysOnTop(true)
                .initModality(Modality.APPLICATION_MODAL)
                //Clear the consumer on page close
                .addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event ->
                        communicationTag = null
                );

        confirmButton.setOnAction(event ->
        {
            if(communicationTag != null)
            {
                var data = this.createDataFromActualValues();
                if(data != null && data.validate())
                {
                    communicationTag.setStringAddressData(data);
                    communicationTag = null;
                }
            }

            this.getStageSetter().close();
        });

    }

    @Override
    public void showStage()
    {
        this.updateTextConvertedAddress();

        var children = super.parent.getChildren();
        if (communicationTag == null)
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

    public void showAsStandalone()
    {
        this.showAsInputTextAddress(null);
    }

    public void showAsInputTextAddress(CommunicationTag communicationTag)
    {
        this.communicationTag = communicationTag;
        this.showStage();
    }

    public abstract boolean loadStringDataToActualValues(String stringData);

    public abstract boolean loadStringDataToActualValues(T stringData);

    public abstract T createDataFromActualValues();

    public abstract void updateTextConvertedAddress();

}
