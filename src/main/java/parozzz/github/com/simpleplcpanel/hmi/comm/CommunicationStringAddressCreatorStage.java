package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
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
    private AddressAttribute addressAttribute;
    private CommunicationTag communicationTag;
    public CommunicationStringAddressCreatorStage(CommunicationType<T> communicationType, String resource) throws IOException
    {
        super(resource, VBox.class);

        this.communicationType = communicationType;
    }

    @Override
    public void setup()
    {
        super.setup();

        this.getStageSetter()
                .setAlwaysOnTop(true)
                .initModality(Modality.APPLICATION_MODAL)
                //Clear the consumer on page close
                .addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event ->
                        addressAttribute = null
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

            if (addressAttribute != null)
            {
                var data = this.createDataFromActualValues();
                if(data != null && data.validate())
                {
                    addressAttribute.setValue(communicationType.getAttributeProperty(), data);
                    addressAttribute = null;
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
        if (addressAttribute == null && communicationTag == null)
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
        this.showAsInputTextAddress((AddressAttribute) null);
    }

    public void showAsInputTextAddress(AddressAttribute addressAttribute)
    {
        this.addressAttribute = addressAttribute;
        this.showStage();
    }

    public void showAsInputTextAddress(CommunicationTag communicationTag)
    {
        this.communicationTag = this.communicationTag;
        this.showStage();
    }

    public abstract void setReadOnly(boolean readOnly);

    public boolean loadAddressAttributeToActualValues(AddressAttribute addressAttribute)
    {
        if(addressAttribute == null)
        {
            return false;
        }

        var addressStringData = addressAttribute.getValue(this.communicationType.getAttributeProperty());
        if(addressStringData == null)
        {
            return false;
        }

        return this.loadStringDataToActualValues(addressStringData.getStringData());
    }

    public abstract boolean loadStringDataToActualValues(String stringData);

    public abstract T createDataFromActualValues();

    public abstract void updateTextConvertedAddress();

}
