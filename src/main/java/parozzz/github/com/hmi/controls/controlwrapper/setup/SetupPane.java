package parozzz.github.com.hmi.controls.controlwrapper.setup;


import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.hmi.controls.controlwrapper.setup.extra.ControlWrapperSetupUtil;
import parozzz.github.com.hmi.util.ContextMenuBuilder;
import parozzz.github.com.hmi.util.FXUtil;

public abstract class SetupPane<A extends Attribute> extends FXObject implements SetupButtonSelectable
{
    private final ControlWrapperSetupStage setupStage;
    private final Class<A> attributeClass;
    private final SetupPaneAttributeChangerList<A> attributeChangerList;

    private final Button selectButton;
    private final boolean stateBased;

    public SetupPane(ControlWrapperSetupStage setupStage, String name, String buttonText, Class<A> attributeClass, boolean stateBased)
    {
        this(setupStage, name, new Button(buttonText), attributeClass, stateBased);
    }

    public SetupPane(ControlWrapperSetupStage setupStage, String name, Button selectButton, Class<A> attributeClass, boolean stateBased)
    {
        super(name);

        this.setupStage = setupStage;
        this.attributeClass = attributeClass;
        this.attributeChangerList = new SetupPaneAttributeChangerList<>(this, attributeClass);
        this.selectButton = selectButton;
        this.stateBased = stateBased;
    }

    @Override
    public void setup()
    {
        super.setup();

        selectButton.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        //selectButton.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
        selectButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        selectButton.setUserData(this);
        selectButton.setOnAction(actionEvent -> setupStage.showSelectable(this));

        if(stateBased)
        {
            ContextMenuBuilder.builder()
                    .simple("Write to All", this::writeToAllStates)
                    .setTo(selectButton);
        }
    }

    public ControlWrapperSetupStage getSetupStage()
    {
        return setupStage;
    }

    @Override
    public Button getSelectButton()
    {
        return selectButton;
    }

    @Override
    public abstract Parent getParent();

    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasAttribute(attributeClass);
    }

    public SetupPaneAttributeChangerList<A> getAttributeChangerList()
    {
        return attributeChangerList;
    }

    public void revertToDefaultValues()
    {
        var selectedControlWrapper = setupStage.getSelectedControlWrapper();
        if(selectedControlWrapper != null)
        {
            //Set the changed data to ALL the states of the wrapper
            selectedControlWrapper.getStateMap().forEach(wrapperState ->
            {
                var attribute = AttributeFetcher.fetch(wrapperState, attributeClass);
                if(attribute != null)
                {
                    attributeChangerList.forEach(attributeChanger ->
                            attributeChanger.getPropertyBis().revertToDefaultValues(attribute)
                    );
                }
            });
        }
    }

    public void writeToAllStates()
    {
        var selectedControlWrapper = this.setupStage.getSelectedControlWrapper();
        if(selectedControlWrapper != null)
        {
            ControlWrapperSetupUtil.writeAttributeChangerListToAllStates(selectedControlWrapper, attributeClass, attributeChangerList);
        }
    }

    protected void computeGlobalProperties()
    {
        this.computeProperties(false, true);
    }

    protected void computeProperties()
    {
        this.computeProperties(true, true);
    }

    protected void computeProperties(boolean addContextMenu, boolean addUndoRedo)
    {
        var propertySet = attributeChangerList.getPropertySet();

        if(addUndoRedo)
        {
            setupStage.getUndoRedoManager().addProperties(propertySet, this);
        }

        propertySet.forEach(property ->
        {
            var bean = property.getBean();
            if(bean instanceof Control)
            {
                var control = (Control) bean;

                if(addContextMenu)
                {
                    var setDataToAllStateMenuItem = new MenuItem("Write to All");
                    setDataToAllStateMenuItem.setOnAction(actionEvent ->
                    {
                        var selectedControlWrapper = setupStage.getSelectedControlWrapper();
                        if(selectedControlWrapper != null)
                        {
                            ControlWrapperSetupUtil.writeSingleAttributeChangerToAllStates(selectedControlWrapper, attributeClass,
                                    attributeChangerList, property);
                        }
                    });

                    var contextMenu = new ContextMenu();
                    contextMenu.getItems().addAll(setDataToAllStateMenuItem);
                    control.setContextMenu(contextMenu);
                }
            }
        });
    }
}
