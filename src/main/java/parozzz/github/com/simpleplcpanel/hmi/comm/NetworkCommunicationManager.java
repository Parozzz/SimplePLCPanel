package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.converter.NumberStringConverter;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;

import java.io.IOException;
import java.util.stream.Stream;

public abstract class NetworkCommunicationManager<P extends CommunicationConnectionParams> extends FXObject
{
    //These values here are needed for every network setup!
    @FXML private TextField ip1TextField;
    @FXML private TextField ip2TextField;
    @FXML private TextField ip3TextField;
    @FXML private TextField ip4TextField;

    @FXML private CheckBox alwaysRetryConnectionCheckBox;
    @FXML private TextField timeBetweenRetriesTextField;
    @FXML private Button connectButton;

    private final IntegerProperty ip1;
    private final IntegerProperty ip2;
    private final IntegerProperty ip3;
    private final IntegerProperty ip4;
    private final BooleanProperty alwaysRetryConnection;
    private final IntegerProperty timeBetweenRetries;
    private final Property<P> connectionParams;

    public NetworkCommunicationManager(String name) throws IOException
    {
        super(name);

        this.ip1 = new SimpleIntegerProperty();
        this.ip2 = new SimpleIntegerProperty();
        this.ip3 = new SimpleIntegerProperty();
        this.ip4 = new SimpleIntegerProperty();
        this.alwaysRetryConnection = new SimpleBooleanProperty(false);
        this.timeBetweenRetries = new SimpleIntegerProperty(10);
        this.connectionParams = new SimpleObjectProperty<>();
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        serializableDataSet.addBoolean("AlwaysRetryConnection", alwaysRetryConnection)
                .addInt("TimeBetweenRetries", timeBetweenRetries, 10)
                .addInt("Ip1", ip1).addInt("Ip2", ip2).addInt("Ip3", ip3).addInt("Ip4", ip4);

        alwaysRetryConnection.bindBidirectional(alwaysRetryConnectionCheckBox.selectedProperty());

        timeBetweenRetriesTextField.textProperty().bindBidirectional(timeBetweenRetries, new NumberStringConverter());
        timeBetweenRetriesTextField.setTextFormatter(
                FXTextFormatterUtil.integerBuilder().min(1).digitCount(4).getTextFormatter()
        );

        ip1TextField.textProperty().bindBidirectional(ip1, new NumberStringConverter());
        ip2TextField.textProperty().bindBidirectional(ip2, new NumberStringConverter());
        ip3TextField.textProperty().bindBidirectional(ip3, new NumberStringConverter());
        ip4TextField.textProperty().bindBidirectional(ip4, new NumberStringConverter());

        Stream.of(ip1TextField, ip2TextField, ip3TextField, ip4TextField).forEach(textField ->
                textField.setTextFormatter(FXTextFormatterUtil.positiveInteger(3))
        );

        connectButton.setOnAction(event ->
        {
            var newConnectionParams = this.createConnectionParams();
            if(newConnectionParams != null)
            {
                this.connectionParams.setValue(newConnectionParams);
            }
        });
    }

    @Override
    public void onSetupComplete()
    {
        super.onSetupComplete();
        connectButton.fire(); //This allows the connection parameters to update on startup!
        //This won't cause a connection with the communication type not selected because the thread WON'T be active!
    }

    public abstract Parent getParent();

    public boolean isAlwaysRetryConnection()
    {
        return alwaysRetryConnection.get();
    }

    public BooleanProperty alwaysRetryConnectionProperty()
    {
        return alwaysRetryConnection;
    }

    public int getTimeBetweenRetries()
    {
        return timeBetweenRetries.get();
    }

    public IntegerProperty timeBetweenRetriesProperty()
    {
        return timeBetweenRetries;
    }

    @Nullable
    public P getConnectionParams()
    {
        return connectionParams.getValue();
    }

    public Property<P> connectionParamsProperty()
    {
        return connectionParams;
    }

    @Nullable
    public String getIpAddress()
    {
        return CommUtils.validateAndCreateIpAddress(ip1TextField, ip2TextField, ip3TextField, ip4TextField);
    }

    @Nullable
    protected abstract P createConnectionParams();
}
