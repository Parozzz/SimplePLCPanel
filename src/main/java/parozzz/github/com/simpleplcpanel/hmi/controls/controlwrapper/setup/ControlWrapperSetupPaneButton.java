package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.util.Objects;

public final class ControlWrapperSetupPaneButton extends FXController
{
    private static Button searchButton(Parent containerParent, String id)
    {
        for (var node : containerParent.getChildrenUnmodifiable())
        {
            if (node instanceof Button && node.getId().equals(id))
            {
                return (Button) node;
            }
        }

        return null;
    }

    private final ControlWrapperSetupStage setupStage;
    private final SetupPane<?> setupPane;
    private final Button button;

    public ControlWrapperSetupPaneButton(ControlWrapperSetupStage setupStage, Parent containerParent, String id,
            SetupPane<?> setupPane)
    {
        super("ControlWrapperSetupPaneButton-" + id);

        this.setupStage = setupStage;
        this.addFXChild(this.setupPane = setupPane);

        button = searchButton(containerParent, id);
        Objects.requireNonNull(button, "A button has not been found with id " + id + " inside the ControlWrapperSetupStage");
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        button.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        button.setBorder(FXUtil.createBorder(Color.GRAY, 2));

        var attributeNameTooltip = new Tooltip();
        attributeNameTooltip.setText(setupPane.getAttributeType().getName());
        attributeNameTooltip.setShowDelay(Duration.seconds(1));
        attributeNameTooltip.setHideDelay(Duration.seconds(1));
        attributeNameTooltip.setAutoHide(true);
        button.setTooltip(attributeNameTooltip);

        //The background will be set in the SetupStage called method.
        button.setOnMouseClicked(event ->
                setupStage.setActiveSetupPane(setupPane)
        );
    }

    public Button getButton()
    {
        return button;
    }

    public SetupPane<?> getSetupPane()
    {
        return setupPane;
    }

    public void showButtonAsSelected()
    {
        button.setBackground(FXUtil.createBackground(Color.LIGHTGREEN));
    }

    public void clearButtonSelection()
    {
        button.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
    }
}
