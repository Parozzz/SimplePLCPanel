package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPCommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7CommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;

import java.io.IOException;
import java.util.function.Consumer;

public final class CommunicationStage extends HMIStage<VBox>
{
    @FXML private ChoiceBox<CommunicationType> commTypeChoiceBox;

    @FXML private StackPane commManagerStackPane;

    private final SiemensS7CommunicationManager siemensS7CommunicationManager;
    private final ModbusTCPCommunicationManager modbusTCPCommunicationManager;
    private NetworkCommunicationManager<?> selectedCommunicationManager;

    public CommunicationStage( SiemensS7Thread plcThread, ModbusTCPThread modbusTCPThread) throws IOException
    {
        super("communicationPage.fxml", VBox.class);

        super.addFXChild(siemensS7CommunicationManager = new SiemensS7CommunicationManager(plcThread))
                .addFXChild(modbusTCPCommunicationManager = new ModbusTCPCommunicationManager(modbusTCPThread));
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStageSetter().setResizable(true);

        serializableDataSet.addEnum("CommunicationType", commTypeChoiceBox.valueProperty(), CommunicationType.class, CommunicationType.SIEMENS_S7);

        commTypeChoiceBox.setConverter(new EnumStringConverter<>(CommunicationType.class).setCapitalize());
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

    private NetworkCommunicationManager<?> getCommunicationManager(CommunicationType communicationType)
    {
        switch (communicationType)
        {
            default:
            case SIEMENS_S7:
                return siemensS7CommunicationManager;
            case MODBUS_TCP:
                return modbusTCPCommunicationManager;
        }
    }
}
