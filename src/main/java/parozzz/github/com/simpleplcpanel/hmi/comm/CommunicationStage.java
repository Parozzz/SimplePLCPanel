package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.util.function.Consumer;

public final class CommunicationStage extends HMIStage<VBox>
{
    @FXML private ChoiceBox<CommunicationType> commTypeChoiceBox;

    @FXML private StackPane commManagerStackPane;

    private final CommunicationDataHolder communicationDataHolder;
    private NetworkCommunicationManager<?> selectedCommunicationManager;

    public CommunicationStage(CommunicationDataHolder communicationDataHolder) throws IOException
    {
        super("communicationPage.fxml", VBox.class);

        this.communicationDataHolder = communicationDataHolder;
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStageSetter().setResizable(true);

        serializableDataSet.addFunction("CommunicationType", commTypeChoiceBox.valueProperty(),
                CommunicationType::getByName, CommunicationType::getName);

        commTypeChoiceBox.setConverter(new StringConverter<>()
        {
            @Override
            public String toString(CommunicationType communicationType)
            {
                return Util.capitalizeWithUnderscore(communicationType.getName());
            }

            @Override
            public CommunicationType fromString(String s)
            {
                return CommunicationType.getByName(s);
            }
        });
        commTypeChoiceBox.getItems().addAll(CommunicationType.values());
        commTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var children = commManagerStackPane.getChildren();
            children.clear();

            if (selectedCommunicationManager != null)
            {
                selectedCommunicationManager.setActive(false);
                selectedCommunicationManager = null;
            }

            if (newValue != null)
            {
                selectedCommunicationManager = communicationDataHolder.getCommunicationManager(newValue);
                if(selectedCommunicationManager != null)
                {
                    selectedCommunicationManager.setActive(true);
                    children.add(selectedCommunicationManager.getParent());
                }
            }
        });
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        commTypeChoiceBox.setValue(CommunicationType.SIEMENS_S7);
    }

    public NetworkCommunicationManager<?> getSelectedCommunicationManager()
    {
        return selectedCommunicationManager;
    }

    public void addCommunicationTypeListener(Consumer<CommunicationType> consumer)
    {
        commTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) -> consumer.accept(newValue));
    }

    public CommunicationType getCommunicationType()
    {
        return commTypeChoiceBox.getValue();
    }
}
