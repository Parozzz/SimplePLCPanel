package parozzz.github.com.simpleplcpanel.hmi.main.settings.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import parozzz.github.com.simpleplcpanel.hmi.main.settings.SettingsPane;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public class LanguageSettingsPane extends SettingsPane
{
    @FXML private TextField onBooleanPlaceholderTextField;
    @FXML private TextField offBooleanPlaceholderTextField;

    private final AnchorPane mainAnchorPane;

    public LanguageSettingsPane() throws IOException
    {
        super("Language");

        this.mainAnchorPane = (AnchorPane) FXUtil.loadFXML("settings/languageSettingsPane.fxml", this);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        super.serializableDataSet.addString("ONBooleanPlaceholder", onBooleanPlaceholderTextField.textProperty(), "On")
                .addString("OFFBooleanPlaceholder", offBooleanPlaceholderTextField.textProperty(), "Off");
    }

    @Override
    public void onSetDefault()
    {
        super.onSetDefault();

        onBooleanPlaceholderTextField.setText("On");
        offBooleanPlaceholderTextField.setText("Off");
    }

    @Override
    public Parent getMainParent()
    {
        return mainAnchorPane;
    }

    public String getONBooleanPlaceholder()
    {
        return onBooleanPlaceholderTextField.getText();
    }

    public String getOFFBooleanPlaceholder()
    {
        return offBooleanPlaceholderTextField.getText();
    }
}
