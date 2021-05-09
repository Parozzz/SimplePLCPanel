package parozzz.github.com.simpleplcpanel.hmi.tags;

import com.sun.source.tree.Scope;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class TagSplitPaneTest extends HMIStage<VBox>
{
    private final ScrollPane scrollPane;
    private final VBox tagSplitPaneVBox;

    private final List<TagSplitPane> tagSplitPanesList;

    public TagSplitPaneTest()
    {
        super(new VBox());

        this.scrollPane = new ScrollPane(
                tagSplitPaneVBox = new VBox()
        );

        this.tagSplitPanesList = new ArrayList<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        this.getStageSetter()
                .setResizable(true);


        var topLabel = new Label("Main Tag List");
        topLabel.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
        topLabel.setAlignment(Pos.CENTER);
        topLabel.setFont(Font.font(null, FontWeight.BOLD, FontPosture.REGULAR, 16));
        topLabel.setMinSize(0, 0);
        topLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        super.parent.getChildren().add(topLabel);

        tagSplitPaneVBox.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        tagSplitPaneVBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tagSplitPaneVBox.setFillWidth(true);

        scrollPane.setMinSize(0, 0);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        super.parent.getChildren().add(scrollPane);

        this.addTag(new Tag("Wow1", CommunicationType.SIEMENS_S7));
        this.addTag(new Tag("Wow123456", CommunicationType.MODBUS_TCP));
        this.addTag(new Tag("Wow123456789123456", CommunicationType.NONE));
    }

    public void addTag(Tag tag)
    {
        if(tag.getSplitPane() != null)
        {
            return;
        }

        try
        {
            var tabSplitPane = new TagSplitPane(tag);
            tabSplitPane.setup();

            int index = 0;
            for(var divider : tabSplitPane.getMainParent().getDividers())
            {
                final int fIndex = index++;
                divider.positionProperty().addListener((observableValue, oldValue, newValue) ->
                        tagSplitPanesList.stream()
                                .filter(Predicate.not(tabSplitPane::equals))
                                .map(TagSplitPane::getMainParent)
                                .map(SplitPane::getDividers)
                                .flatMap(List::stream)
                                .skip(fIndex)
                                .forEach(lDivider -> lDivider.setPosition(newValue.doubleValue()))
                );
            }

            this.tagSplitPanesList.add(tabSplitPane);
            tag.setSplitPane(tabSplitPane);

            var splitPane = tabSplitPane.getMainParent();
            tag.addDeleteRunnable(() -> super.parent.getChildren().remove(splitPane));
            tagSplitPaneVBox.getChildren().add(splitPane);

        } catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
