package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup;


import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChanger;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.extra.ControlWrapperSetupUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

public abstract class SetupPane<A extends Attribute> extends FXObject implements SetupSelectable
{
    private final ControlWrapperSetupStage setupStage;
    private final AttributeType<A> attributeType;
    private final SetupPaneAttributeChangerList<A> attributeChangerList;

    private final Button selectButton;

    public SetupPane(ControlWrapperSetupStage setupStage, String name, String buttonText,
            AttributeType<A> attributeType)
    {
        this(setupStage, name, new Button(buttonText), attributeType);
    }

    public SetupPane(ControlWrapperSetupStage setupStage, String name, Button selectButton,
            AttributeType<A> attributeType)
    {
        super(name);

        this.setupStage = setupStage;
        this.attributeType = attributeType;
        this.attributeChangerList = new SetupPaneAttributeChangerList<>(this, attributeType);
        this.selectButton = selectButton;
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        selectButton.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        //selectButton.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
        selectButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        selectButton.setUserData(this);
        selectButton.setOnAction(actionEvent -> setupStage.setShownSelectable(this));
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

    public final AttributeType<A> getAttributeType()
    {
        return attributeType;
    }

    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasType(attributeType);
    }

    public SetupPaneAttributeChangerList<A> getAttributeChangerList()
    {
        return attributeChangerList;
    }

    public void bindAll(ControlWrapper<?> controlWrapper)
    {
        var attribute = AttributeFetcher.fetch(controlWrapper, this.attributeType);
        if(attribute != null)
        {
            this.attributeChangerList.forEach(attributeChanger ->
                    attributeChanger.bindToAttribute(attribute)
            );
        }

        if(controlWrapper.getAttributeTypeManager().isGlobal(attributeType))
        {
            this.setAsGlobal();
        }
        else
        {
            this.setAsState();
        }
    }

    public void unbindAll()
    {
        this.attributeChangerList.forEach(SetupPaneAttributeChanger::unbind);
    }

    public void clearControlWrapper()
    {

    }

    protected void setAsState()
    {
        ContextMenuBuilder.builder()
                .simple("Write to All", this::writeToAllStates)
                .setTo(selectButton);

        var propertySet = attributeChangerList.getPropertySet();
        propertySet.forEach(property ->
        {
            var bean = property.getBean();
            if (bean instanceof Control)
            {
                ContextMenuBuilder.builder()
                        .simple("Write to All", () ->
                        {
                            var selectedControlWrapper = setupStage.getSelectedControlWrapper();
                            if (selectedControlWrapper != null)
                            {
                                ControlWrapperSetupUtil.writeSingleAttributeChangerToAllStates(
                                        selectedControlWrapper, attributeChangerList, property
                                );
                            }
                        })
                        .setTo((Control) bean);
            }
        });
    }

    protected void setAsGlobal()
    {
        selectButton.setContextMenu(null);

        var propertySet = attributeChangerList.getPropertySet();
        propertySet.forEach(property ->
        {
            var bean = property.getBean();
            if (bean instanceof Control)
            {
                ((Control) bean).setContextMenu(null);
            }
        });
    }

    public void revertToDefaultValues()
    {
        var selectedControlWrapper = setupStage.getSelectedControlWrapper();
        if (selectedControlWrapper != null)
        {
            //Set the changed data to ALL the states of the wrapper
            selectedControlWrapper.getStateMap().forEach(wrapperState ->
            {
                var attribute = wrapperState.getAttributeMap().get(attributeType);
                if (attribute != null)
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
        if (selectedControlWrapper != null)
        {
            ControlWrapperSetupUtil.writeAttributeChangerListToAllStates(
                    selectedControlWrapper, attributeChangerList
            );
        }
    }

    protected void computeProperties()
    {
        var propertySet = attributeChangerList.getPropertySet();
        setupStage.getUndoRedoManager().addProperties(propertySet, this);
    }
}
