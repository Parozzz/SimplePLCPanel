package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.attributechanger;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.util.function.Function;

public final class SetupPaneAttributeChanger<A extends Attribute> implements Loggable
{
    private final SetupPane<A> setupPane;
    private final PropertyBis<?, ?> propertyBis;
    private boolean dataChanged = false;

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

        bindValueChangedObservableValue(property);
    }

    public PropertyBis<?, ?> getPropertyBis()
    {
        return propertyBis;
    }

    public Property<?> getProperty()
    {
        return propertyBis.property;
    }

    public void resetDataChanged()
    {
        dataChanged = false;
    }

    public boolean isDataChanged()
    {
        return dataChanged;
    }

    private void bindValueChangedObservableValue(ObservableValue<?> observableValue)
    {
        observableValue.addListener((tObservableValue, oldValue, newValue) ->
        {
            if(oldValue == null)
            {
                if(newValue != null)
                {
                    dataChanged = true;
                }
            }else if(!oldValue.equals(newValue))
            {
                dataChanged = true;
            }
        });
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

        public void revertToDefaultValues(A attribute)
        {
            var undoRedoManager = setupPane.getSetupStage().getUndoRedoManager();
            undoRedoManager.setIgnoreNew(true); //Ignore all the actions while copying data to an SetupPane

            try
            {
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

            undoRedoManager.setIgnoreNew(false);
        }

        public void copyDataFromAttribute(A attribute)
        {
            var undoRedoManager = setupPane.getSetupStage().getUndoRedoManager();
            undoRedoManager.setIgnoreNew(true); //Ignore all the actions while copying data to an SetupPane

            try
            {
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
                    var propertyValue = property.getValue();
                    var attributeConvertedValue = attributeToPropertyConvert.apply(attributeValue);
                    if(attributeConvertedValue != null && !attributeConvertedValue.equals(propertyValue)) //This way to avoid propertyValue NullPointerException
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
            
            undoRedoManager.setIgnoreNew(false);
        }

        public void setDataToAttribute(A attribute)
        {
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
                    attribute.setValue(attributeProperty, propertyConvertedValue);
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
