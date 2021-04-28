package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPCommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensPLCCommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensPLCThread;
import parozzz.github.com.simpleplcpanel.hmi.page.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.EnumStringConverter;

import java.io.IOException;
import java.util.function.Consumer;

public final class CommunicationStage extends HMIStage<VBox>
{
    @FXML private ChoiceBox<CommunicationType> commTypeChoiceBox;

    @FXML private StackPane commManagerStackPane;

    private final SiemensPLCCommunicationManager siemensPLCCommunicationManager;
    private final ModbusTCPCommunicationManager modbusTCPCommunicationManager;
    private DeviceCommunicationManager<?> selectedCommunicationManager;

    public CommunicationStage(SiemensPLCThread plcThread, ModbusTCPThread modbusTCPThread) throws IOException
    {
        super("communicationPage.fxml", VBox.class);

        super.addFXChild(siemensPLCCommunicationManager = new SiemensPLCCommunicationManager(plcThread))
                .addFXChild(modbusTCPCommunicationManager = new ModbusTCPCommunicationManager(modbusTCPThread));
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStageSetter()
                .setResizable(true);

        serializableDataSet.addEnum("CommunicationType", commTypeChoiceBox.valueProperty(), CommunicationType.class);

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
            case SIEMENS_S7:
                return siemensPLCCommunicationManager;
            case MODBUS_TCP:
                return modbusTCPCommunicationManager;
        }
    }
}
