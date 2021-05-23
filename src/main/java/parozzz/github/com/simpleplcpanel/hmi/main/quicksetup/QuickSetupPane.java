package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperGenericAttributeUpdateConsumer;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl.*;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIPane;
import parozzz.github.com.simpleplcpanel.hmi.pane.HidablePane;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class QuickSetupPane
        extends FXController
        implements HMIPane, ControlWrapperSpecific, HidablePane
{
    private final ScrollPane scrollPane;
    private final VBox mainVBox;
    private final VBox paneVBox;

    private final GenericQuickSetupPanePart genericQuickSetupPanePart;
    private final StateSelectionQuickSetupPanePart stateSelectionQuickSetupPanePart;
    private final SizeQuickSetupPanePart sizeQuickSetupPanePart;
    private final FontQuickSetupPanePart fontQuickSetupPanePart;
    private final BackgroundQuickSetupPanePart backgroundQuickSetupPanePart;
    private final TextQuickSetupPanePart textQuickSetupPanePart;
    private final AddressQuickSetupPanePart readAddressQuickSetupPanePart;
    private final AddressQuickSetupPanePart writeAddressQuickSetupPanePart;

    private final QuickSetupStateBinder stateBinder;
    private final List<QuickSetupPanePart> quickSetupPanePartList;

    private final ChangeListener<Boolean> validControlWrapperListener;
    private final ControlWrapperGenericAttributeUpdateConsumer attributeUpdatedConsumer;
    private final ChangeListener<WrapperState> wrapperStateChangeListener;
    private final ListChangeListener<WrapperState> wrapperStateListChangeListener;

    //private final WrapperStateChangedConsumer stateChangeConsumer;

    private final BooleanProperty visible;

    private ControlWrapper<?> selectedControlWrapper;

    public QuickSetupPane(MainEditStage mainEditStage,
            TagsManager tagsManager, CommunicationDataHolder communicationDataHolder) throws IOException
    {
        this.stateBinder = new QuickSetupStateBinder(this);
        this.mainVBox = new VBox(
                this.scrollPane = new ScrollPane(
                        this.paneVBox = new VBox()
                )
        );

        this.addFXChild(this.genericQuickSetupPanePart = new GenericQuickSetupPanePart())
                .addFXChild(this.stateSelectionQuickSetupPanePart = new StateSelectionQuickSetupPanePart(this))
                .addFXChild(this.sizeQuickSetupPanePart = new SizeQuickSetupPanePart())
                .addFXChild(this.fontQuickSetupPanePart = new FontQuickSetupPanePart())
                .addFXChild(this.backgroundQuickSetupPanePart = new BackgroundQuickSetupPanePart())
                .addFXChild(this.textQuickSetupPanePart = new TextQuickSetupPanePart())
                .addFXChild(this.readAddressQuickSetupPanePart = new AddressQuickSetupPanePart(
                        mainEditStage, this, tagsManager, communicationDataHolder, true
                ))
                .addFXChild(this.writeAddressQuickSetupPanePart = new AddressQuickSetupPanePart(
                        mainEditStage, this, tagsManager, communicationDataHolder, false
                ));

        this.quickSetupPanePartList = new ArrayList<>();

        this.validControlWrapperListener = (observableValue, oldValue, newValue) ->
        {
            if (!newValue)
            {
                this.setSelectedControlWrapper(null);
            }
        };
        this.attributeUpdatedConsumer = updateData ->
                updateData.getAttributeTypeCollection().forEach(stateBinder::loadValueFromControlWrapperOf);

        this.wrapperStateChangeListener = (observable, oldValue, newValue) ->
        {
            if (newValue != null)
            {
                stateSelectionQuickSetupPanePart.changeState(newValue);
            }
        };

        this.wrapperStateListChangeListener = change ->
                stateSelectionQuickSetupPanePart.loadStatesOf(selectedControlWrapper);
        this.visible = new SimpleBooleanProperty(true);
    }

    @Override
    public BooleanProperty visibleProperty()
    {
        return visible;
    }

    @Override
    public void setup()
    {
        super.setup();

        //Hide VBox Button at the TOP
        mainVBox.getChildren().add(0, this.createHideParent(Pos.TOP_RIGHT));
        mainVBox.setMinSize(0, 0);
        mainVBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setBackground(FXUtil.createBackground(Color.TRANSPARENT));

        paneVBox.setMinSize(0, 0);
        paneVBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.computeQuickSetupPane(genericQuickSetupPanePart, stateSelectionQuickSetupPanePart,
                readAddressQuickSetupPanePart, writeAddressQuickSetupPanePart,
                sizeQuickSetupPanePart, fontQuickSetupPanePart, backgroundQuickSetupPanePart, textQuickSetupPanePart
        );
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        this.setSelectedControlWrapper(null); //Start with a null value (Also set everything invisible)
    }

    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }

    public WrapperState getSelectedWrapperState()
    {
        return stateSelectionQuickSetupPanePart.getSelectedWrapperState();
    }

    public void refreshValuesIfSelected(ControlWrapper<?> controlWrapper)
    {
        if (selectedControlWrapper != null && selectedControlWrapper == controlWrapper)
        {
            stateBinder.loadAllValuesFromControlWrapper();
        }
    }

    @Override
    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if (selectedControlWrapper != null)
        {
            selectedControlWrapper.validProperty().removeListener(validControlWrapperListener);
            selectedControlWrapper.getAttributeUpdater().removeGenericUpdateConsumer(attributeUpdatedConsumer);

            var stateMap = selectedControlWrapper.getStateMap();
            stateMap.currentWrapperStateProperty().removeListener(wrapperStateChangeListener);
            stateMap.wrapperStateListProperty().removeListener(wrapperStateListChangeListener);
        }

        this.selectedControlWrapper = controlWrapper;
        if (controlWrapper == null)
        {
            var children = paneVBox.getChildren();
            children.clear();
            children.add(genericQuickSetupPanePart.getParent());

            quickSetupPanePartList.forEach(QuickSetupPanePart::clearControlWrapper);
            return;
        }

        controlWrapper.validProperty().addListener(validControlWrapperListener);
        controlWrapper.getAttributeUpdater().addGenericUpdateConsumer(attributeUpdatedConsumer);

        var stateMap = controlWrapper.getStateMap();
        stateMap.currentWrapperStateProperty().addListener(wrapperStateChangeListener);
        stateMap.wrapperStateListProperty().addListener(wrapperStateListChangeListener);

        var children = paneVBox.getChildren();
        children.clear();
        for (var quickSetupPane : quickSetupPanePartList)
        {
            if (quickSetupPane.validateControlWrapper(controlWrapper))
            {
                children.add(quickSetupPane.getParent());
            }
        }

        this.loadAllValuesFromControlWrapper();
    }

    @Override
    public ControlWrapper<?> getSelectedControlWrapper()
    {
        return selectedControlWrapper;
    }

    public void loadAllValuesFromControlWrapperOf(AttributeType<?> attributeType)
    {
        stateBinder.loadValueFromControlWrapperOf(attributeType);
    }

    public void loadAllValuesFromControlWrapper()
    {
        stateBinder.loadAllValuesFromControlWrapper();
    }

    private void computeQuickSetupPane(QuickSetupPanePart... quickSetupPaneParts)
    {
        for (var quickSetupPane : quickSetupPaneParts)
        {
            quickSetupPane.addBinders(stateBinder);
            quickSetupPanePartList.add(quickSetupPane);
        }
    }
}
