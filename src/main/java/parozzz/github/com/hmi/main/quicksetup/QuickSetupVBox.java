package parozzz.github.com.hmi.main.quicksetup;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.main.quicksetup.impl.*;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class QuickSetupVBox extends FXController
{
    private final ScrollPane mainScrollPane;

    private final StateSelectionQuickSetupPane stateSelectionQuickSetupPane;
    private final GenericQuickSetupPane genericQuickSetupPane;
    private final BaseQuickSetupPane baseQuickSetupPane;
    private final BackgroundQuickSetupPane backgroundQuickSetupPane;
    private final TextQuickSetupPane textQuickSetupPane;

    private final QuickSetupStateBinder stateBinder;
    private final Set<QuickSetupPane> quickSetupPaneSet;
    private ControlWrapper<?> selectedControlWrapper;

    public QuickSetupVBox() throws IOException
    {
        super("QuickProperties");

        this.stateBinder = new QuickSetupStateBinder(() ->
        {
            if(selectedControlWrapper != null)
            {
                selectedControlWrapper.applyAttributes();
            }
        });
        this.mainScrollPane = new ScrollPane();
        this.quickSetupPaneSet = new HashSet<>();

        this.addFXChild(this.stateSelectionQuickSetupPane = new StateSelectionQuickSetupPane())
                .addFXChild(this.genericQuickSetupPane = new GenericQuickSetupPane())
                .addFXChild(this.baseQuickSetupPane = new BaseQuickSetupPane())
                .addFXChild(this.backgroundQuickSetupPane = new BackgroundQuickSetupPane())
                .addFXChild(this.textQuickSetupPane = new TextQuickSetupPane());
    }

    @Override
    public void setup()
    {
        super.setup();

        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setBackground(FXUtil.createBackground(Color.TRANSPARENT));

        var scrollVBox = new VBox();
        this.computeQuickSetupPane(scrollVBox,
                stateSelectionQuickSetupPane, genericQuickSetupPane,
                baseQuickSetupPane, backgroundQuickSetupPane, textQuickSetupPane
        );

        scrollVBox.setMinSize(0, 0);
        scrollVBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        mainScrollPane.setContent(scrollVBox);

        stateSelectionQuickSetupPane.setStateChangeConsumer(wrapperState ->
        {
            if(selectedControlWrapper == null || wrapperState == null)
            {
                stateBinder.clear();
                return;
            }

            selectedControlWrapper.getStateMap().changeCurrentState(wrapperState);

            stateBinder.setBoundWrapperState(wrapperState);
            quickSetupPaneSet.forEach(quickSetupPane -> quickSetupPane.onNewWrapperState(wrapperState));
        });
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        this.setSelected(null); //Start with a null value (Also set everything invisible)
    }

    public Parent getMainParent()
    {
        return mainScrollPane;
    }

    public void refreshValuesIfSelected(ControlWrapper<?> controlWrapper)
    {
        if(selectedControlWrapper != null && selectedControlWrapper == controlWrapper)
        {
            stateBinder.refreshValues();
        }
    }

    public void setSelected(ControlWrapper<?> controlWrapper)
    {
        this.selectedControlWrapper = controlWrapper;
        if(selectedControlWrapper == null)
        {
            stateBinder.clear(); //Without this, values on the control state are cleared on selection change
            quickSetupPaneSet.forEach(quickSetupPane ->
            {
                quickSetupPane.getMainParent().setVisible(false);
                quickSetupPane.clear();
            });
            return;
        }

        quickSetupPaneSet.forEach(quickSetupPane ->
        {
            quickSetupPane.getMainParent().setVisible(true); //Uphere is better. Below it could hide it again.
            quickSetupPane.onNewControlWrapper(controlWrapper);
        });
    }

    private void computeQuickSetupPane(VBox vBox, QuickSetupPane... quickSetupPanes)
    {
        for(var quickSetupPane : quickSetupPanes)
        {
            quickSetupPane.addBinders(stateBinder);

            quickSetupPaneSet.add(quickSetupPane);
            vBox.getChildren().add(quickSetupPane.getMainParent());
        }
    }
}
