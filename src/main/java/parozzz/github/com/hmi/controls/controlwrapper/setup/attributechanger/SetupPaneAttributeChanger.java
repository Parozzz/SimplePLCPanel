package parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SetupPaneAttributeChanger<A extends Attribute>
{
    private final static Logger logger = Logger.getLogger(SetupPaneAttributeChanger.class.getSimpleName());

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
            if (oldValue == null)
            {
                if (newValue != null)
                {
                    dataChanged = true;
                }
            } else if (!oldValue.equals(newValue))
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

    public final class PropertyBis<V, H>
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
            try
            {
                var undoRedoManager = setupPane.getSetupStage().getUndoRedoManager();

                //Ignore all the actions while copying data to an SetupPane
                undoRedoManager.setIgnoreNew(true);

                var defaultValue = attributeProperty.getDefaultValue();

                attribute.setValue(attributeProperty, defaultValue);
                property.setValue(attributeToPropertyConvert.apply(defaultValue));

                undoRedoManager.setIgnoreNew(false);
            } catch (Exception exception)
            {
                logger.log(Level.WARNING,
                        "Error while reverting to a default value. AttributeName " + attribute.getFXObjectName() +
                                ", SetupPane " + setupPane.getFXObjectName()
                        , exception);
            }
        }

        public void copyDataFromAttribute(A attribute)
        {
            try
            {
                var undoRedoManager = setupPane.getSetupStage().getUndoRedoManager();

                //Ignore all the actions while copying data to an SetupPane
                undoRedoManager.setIgnoreNew(true);

                var attributeValue = attribute.getValue(attributeProperty);
                //It might happen is null, especially for cases where two equal attributes can have different properties
                if (attributeValue == null)
                {
                    undoRedoManager.setIgnoreNew(false);
                    return;
                }

                property.setValue(attributeToPropertyConvert.apply(attributeValue));

                undoRedoManager.setIgnoreNew(false);
            } catch (Exception exception)
            {
                logger.log(Level.WARNING,
                        "Error while setting data to an attribute. AttributeName " + attribute.getFXObjectName() +
                                ", SetupPane " + setupPane.getFXObjectName()
                        , exception);
            }
        }

        public void setDataToAttribute(A attribute)
        {
            try
            {
                var propertyValue = property.getValue();
                attribute.setValue(attributeProperty, propertyToAttributeConvert.apply(propertyValue));
                //In case i set data to the attribute, i update all the internal values
                attribute.update();
            } catch (Exception exception)
            {
                logger.log(Level.WARNING,
                        "Error while getting data to an attribute. AttributeName: " + attribute.getFXObjectName() +
                                "SetupPane: " + setupPane.getFXObjectName(),
                        exception);
            }
        }

    }
}
