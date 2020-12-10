package parozzz.github.com.hmi.main.settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.main.settings.impl.LanguageSettingsPane;
import parozzz.github.com.hmi.page.HMIStage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class SettingsStage extends HMIStage<StackPane>
{
    @FXML private Label topLabel;

    @FXML private Button languagePageButton;

    @FXML private StackPane centerStackPane;

    private final Set<Button> pageButtonSet;
    private final LanguageSettingsPane languageSettingsPane;

    public SettingsStage() throws IOException
    {
        super("settings/mainSettingsPage.fxml", StackPane.class);

        this.pageButtonSet = new HashSet<>();
        this.addFXChild(languageSettingsPane = new LanguageSettingsPane());
    }

    @Override
    public void setup()
    {
        super.setup();

        topLabel.setText("Settings");

        this.initButton(languagePageButton, languageSettingsPane);
    }

    @Override
    public void setupComplete()
    {
        super.setDefault();

        languagePageButton.getOnAction().handle(new ActionEvent());
    }

    public LanguageSettingsPane getLanguage()
    {
        return languageSettingsPane;
    }

    private void initButton(Button button, SettingsPane settingsPane)
    {
        pageButtonSet.add(button);
        button.setOnAction(actionEvent ->
        {
            var children = centerStackPane.getChildren();
            children.clear();
            children.add(settingsPane.getMainParent());

            button.setBackground(new Background(new BackgroundFill(Color.web("#8ba3a0"), new CornerRadii(1), new Insets(0))));

            pageButtonSet.stream()
                    .filter(Predicate.not(button::equals))
                    .forEach(otherButton ->
                            button.setBackground(new Background(new BackgroundFill(Color.web("#f4f4f4"), new CornerRadii(1), new Insets(0))))
                    );
        });
    }
}
