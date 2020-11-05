package parozzz.github.com.hmi.comm;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPCommunicationManager;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.hmi.comm.siemens.SiemensPLCCommunicationManager;
import parozzz.github.com.hmi.comm.siemens.SiemensPLCThread;
import parozzz.github.com.hmi.page.HMIStage;
import parozzz.github.com.hmi.util.EnumStringConverter;

import java.io.IOException;
import java.util.function.Consumer;

public final class CommunicationStage extends HMIStage<VBox>
{
    @FXML private ChoiceBox<CommunicationType> commTypeChoiceBox;

    @FXML private AnchorPane commManagerAnchorPane;

    private final SiemensPLCCommunicationManager siemensPLCCommunicationManager;
    private final ModbusTCPCommunicationManager modbusTCPCommunicationManager;
    private DeviceCommunicationManager<?> selectedCommunicationManager;

    public CommunicationStage(SiemensPLCThread plcThread, ModbusTCPThread modbusTCPThread) throws IOException
    {
        super("CommunicationPage", "communicationPage.fxml", VBox.class);

        super.addFXChild(siemensPLCCommunicationManager = new SiemensPLCCommunicationManager(plcThread))
                .addFXChild(modbusTCPCommunicationManager = new ModbusTCPCommunicationManager(modbusTCPThread));
    }

    @Override
    public void setup()
    {
        super.setup();

        serializableDataSet.addEnum("CommunicationType", commTypeChoiceBox.valueProperty(), CommunicationType.class);

        commTypeChoiceBox.setConverter(new EnumStringConverter<>(CommunicationType.class));
        commTypeChoiceBox.getItems().addAll(CommunicationType.values());
        commTypeChoiceBox.valueProperty().addListener((observableValue, oldValue, newValue) ->
        {
            var children = commManagerAnchorPane.getChildren();
            if (selectedCommunicationManager != null)
            {
                selectedCommunicationManager.setActive(false);
                selectedCommunicationManager = null;

                children.clear();
            }

            if (newValue != null)
            {
                selectedCommunicationManager = this.getCommunicationManager(newValue);
                selectedCommunicationManager.setActive(true);

                children.add(selectedCommunicationManager.getParent());
            }
        });
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        commTypeChoiceBox.setValue(CommunicationType.SIEMENS);
    }

    public DeviceCommunicationManager<?> getSelectedCommunicationManager()
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

    private DeviceCommunicationManager<?> getCommunicationManager(CommunicationType communicationType)
    {
        switch (communicationType)
        {
            default:
            case SIEMENS:
                return siemensPLCCommunicationManager;
            case MODBUS_TCP:
                return modbusTCPCommunicationManager;
        }
    }
}
