package parozzz.github.com.simpleplcpanel.hmi.controls.setup;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.setup.panes.SetupPane;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.Util;
import parozzz.github.com.simpleplcpanel.util.XmlTools;

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
    private final Region svgImageRegion;
    private final String svgImageResourcePath;

    public ControlWrapperSetupPaneButton(ControlWrapperSetupStage setupStage, Button button, String svgImageResourcePath,
            SetupPane<?> setupPane)
    {
        super("ControlWrapperSetupPaneButton-" + button.getId());

        this.setupStage = setupStage;
        this.addFXChild(this.setupPane = setupPane);

        this.button = button;
        this.svgImageRegion = new Region();
        this.svgImageResourcePath = svgImageResourcePath;
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        button.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        //button.setBorder(FXUtil.createBorder(Color.TRANSPARENT, 2));

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

        if (svgImageResourcePath != null && !svgImageResourcePath.isEmpty())
        {
            var svgScrapData = XmlTools.svgScrap(Util.getResource(svgImageResourcePath));
            if(svgScrapData == null)
            {
                MainLogger.getInstance().error("Invalid svg scrap data for " + svgImageResourcePath, this);
            }
            else
            {
                var svgPath = new SVGPath();
                svgPath.setContent(svgScrapData.getPath());

                svgImageRegion.setBorder(FXUtil.createBorder(Color.BLACK, 1));
                svgImageRegion.setShape(svgPath);

                button.setGraphic(svgImageRegion);
            }

        }
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
        svgImageRegion.setBackground(FXUtil.createBackground(Color.LIGHTGREEN));
        //button.setBackground(FXUtil.createBackground(Color.LIGHTGREEN));
    }

    public void clearButtonSelection()
    {
        svgImageRegion.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        //button.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
    }
}
