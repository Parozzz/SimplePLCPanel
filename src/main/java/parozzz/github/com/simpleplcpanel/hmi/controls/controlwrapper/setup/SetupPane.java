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
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoManager;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.util.Objects;

public abstract class SetupPane<A extends Attribute> extends FXObject
{
    private final ControlWrapperSetupStage setupStage;
    private final AttributeType<A> attributeType;
    private final SetupPaneAttributeChangerList<A> attributeChangerList;

    public SetupPane(ControlWrapperSetupStage setupStage, String name, AttributeType<A> attributeType)
    {
        super(name);

        this.setupStage = Objects.requireNonNull(setupStage, "SetupStage cannot be null inside a SetupPane");
        this.attributeType = attributeType;
        this.attributeChangerList = new SetupPaneAttributeChangerList<>(this, attributeType);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();
    }

    public ControlWrapperSetupStage getSetupStage()
    {
        return setupStage;
    }

    public abstract Parent getParent();

    public final AttributeType<A> getAttributeType()
    {
        return attributeType;
    }

    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasType(attributeType);
    }

    public boolean hasAttribute(ControlWrapper<?> controlWrapper)
    {
        return AttributeFetcher.hasAttribute(controlWrapper, this.attributeType);
    }

    public SetupPaneAttributeChangerList<A> getAttributeChangerList()
    {
        return attributeChangerList;
    }

    /**
     * Bind the attribute from the current state to this setup pane
     *
     * @param controlWrapper The control wrapper to obtain the state
     * @return {@code true} if the ControlWrapper is not null and has the attribute in the current state.
     */
    public boolean bindAll(ControlWrapper<?> controlWrapper)
    {
        if(controlWrapper == null)
        {
            return false;
        }

        var attribute = AttributeFetcher.fetch(controlWrapper, this.attributeType);
        if(attribute == null)
        {
            return false;
        }

        this.attributeChangerList.forEach(attributeChanger -> attributeChanger.bindToAttribute(attribute));
        if (controlWrapper.getAttributeTypeManager().isGlobal(attributeType))
        {
            this.setAsGlobal();
        } else
        {
            this.setAsState();
        }

        return true;
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
        /*
        var propertySet = attributeChangerList.getPropertySet();
        propertySet.forEach(property ->
                UndoRedoManager.getInstance().registerProperty(setupStage, property)
        );*/
    }
}
