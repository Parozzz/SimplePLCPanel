package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.attributechanger;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoManager;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.util.Objects;
import java.util.function.Function;

public final class SetupPaneAttributeChanger<A extends Attribute>
        implements Loggable
{
    private final SetupPane<A> setupPane;
    private final PropertyBis<?, ?> propertyBis;
    private A attribute;
    private boolean ignoreAttributeUpdate;

    public <V> SetupPaneAttributeChanger(SetupPane<A> setupPane,
            Property<V> property,
            AttributeProperty<V> attributeProperty)
    {
        this(setupPane, property, attributeProperty, Function.identity(), Function.identity());
    }

    public <V, H> SetupPaneAttributeChanger(SetupPane<A> setupPane,
            Property<H> property,
            AttributeProperty<V> attributeProperty,
            Function<H, V> propertyToAttributeConvert,
            Function<V, H> attributeToPropertyConvert)
    {
        this.setupPane = setupPane;
        this.propertyBis = new PropertyBis<>(attributeProperty, property, propertyToAttributeConvert, attributeToPropertyConvert);

        property.addListener((tObservableValue, oldValue, newValue) ->
        {
            if(attribute != null)
            {
                ignoreAttributeUpdate = true;
                propertyBis.setDataToAttribute(attribute);

                //This will set all the changed values to every selected control wrappers. It will be here to avoid
                //updating secondary if an attribute is not set (During strange transitions / states)
                for(var secondaryWrapperState : setupPane.getSetupStage().getSecondarySelectedWrapperStateSet())
                {
                    var secondaryAttribute = secondaryWrapperState.getAttributeMap().get(setupPane.getAttributeType());
                    if(secondaryAttribute != null)
                    {
                        propertyBis.setDataToAttribute(secondaryAttribute);
                    }
                }

                ignoreAttributeUpdate = false;
            }
        });
    }

    public PropertyBis<?, ?> getPropertyBis()
    {
        return propertyBis;
    }

    public Property<?> getProperty()
    {
        return propertyBis.property;
    }

    public void bindToAttribute(A attribute)
    {
        this.attribute = attribute;
        if(attribute == null)
        {
            return;
        }

        propertyBis.copyDataFromAttribute(attribute);
        attribute.addAttributeUpdaterRunnable(this, () ->
        {
            if(this.attribute == null || ignoreAttributeUpdate)
            {
                return;
            }

            propertyBis.copyDataFromAttribute(attribute);
        });
    }

    public void unbind()
    {
        if(attribute != null)
        {
            attribute.removeAttributeUpdaterRunnable(this);
            attribute = null;
        }
        //Better call this after the current attribute is cleared otherwise everything will be reset...
        propertyBis.clear();
    }

    public void copyDataFromAttribute(A attribute)
    {
        propertyBis.copyDataFromAttribute(attribute);
    }

    public void setDataToAttribute(A attribute)
    {
        propertyBis.setDataToAttribute(attribute);
    }

    @Override
    public String log()
    {
        return "SetupPane: " + setupPane.getFXObjectName();
    }

    public final class PropertyBis<V, H> implements Loggable
    {
        private final AttributeProperty<V> attributeProperty;
        private final Property<H> property;
        private final Function<H, V> propertyToAttributeConvert;
        private final Function<V, H> attributeToPropertyConvert;

        public PropertyBis(AttributeProperty<V> attributeProperty, Property<H> property,
                Function<H, V> propertyToAttributeConvert,
                Function<V, H> attributeToPropertyConvert)
        {
            this.attributeProperty = attributeProperty;
            this.property = property;
            this.propertyToAttributeConvert = propertyToAttributeConvert;
            this.attributeToPropertyConvert = attributeToPropertyConvert;
        }

        public void clear()
        {
            UndoRedoManager.getInstance().startIgnoringNextActions(); //Ignore all the actions while copying data to an SetupPane

            var defaultValue = attributeProperty.getDefaultValue();
            property.setValue(attributeToPropertyConvert.apply(defaultValue));

            UndoRedoManager.getInstance().stopIgnoringNextActions();
        }

        public void revertToDefaultValues(A attribute)
        {
            try
            {
                UndoRedoManager.getInstance().startIgnoringNextActions();

                var defaultValue = attributeProperty.getDefaultValue();
                attribute.setValue(attributeProperty, defaultValue);
                property.setValue(attributeToPropertyConvert.apply(defaultValue));
            }
            catch(Exception exception)
            {
                MainLogger.getInstance().warning(
                        "Error while reverting to a default value. AttributeName "
                                + attribute.getFXObjectName()
                        , exception
                );
            }
            finally
            {
                UndoRedoManager.getInstance().stopIgnoringNextActions();
            }
        }

        public void copyDataFromAttribute(A attribute)
        {
            try
            {
                UndoRedoManager.getInstance().startIgnoringNextActions();

                var attributeValue = attribute.getValue(attributeProperty);
                if(attributeValue == null)
                {
                    if(attributeProperty.allowNullValues())
                    {
                        property.setValue(null);
                    }
                }
                else
                {
                    var attributeConvertedValue = attributeToPropertyConvert.apply(attributeValue);
                    if(attributeConvertedValue != null)
                    { //This is to avoid problems where the value has changed and get set immediately again causing glitching on controls
                        property.setValue(attributeConvertedValue);
                    }
                }
            }
            catch(Exception exception)
            {
                MainLogger.getInstance().warning(
                        "Error while setting data to an attribute. AttributeName "
                                + attribute.getFXObjectName()
                        , exception
                );
            }
            finally
            {
                UndoRedoManager.getInstance().stopIgnoringNextActions();
            }
        }

        public void setDataToAttribute(A attribute)
        {
            Objects.requireNonNull(attribute, "Trying to set data to an attribute that has value null");

            try
            {
                var propertyValue = property.getValue();
                if(propertyValue == null)
                {
                    if(attributeProperty.allowNullValues())
                    {
                        attribute.setValue(attributeProperty, null);
                    }
                }
                else
                {
                    var propertyConvertedValue = propertyToAttributeConvert.apply(propertyValue);
                    if(propertyConvertedValue != null)
                    {
                        attribute.setValue(attributeProperty, propertyConvertedValue);
                    }
                }
            }
            catch(Exception exception)
            {
                MainLogger.getInstance().warning(
                        "Error while getting data to an attribute. AttributeName "
                                + attribute.getFXObjectName(),
                        exception);
            }
        }

        @Override
        public String log()
        {
            return "SetupPane: " + setupPane.getFXObjectName();
        }
    }
}
