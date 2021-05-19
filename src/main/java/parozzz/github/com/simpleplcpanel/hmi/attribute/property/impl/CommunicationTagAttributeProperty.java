package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributePropertyManager;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Taggable;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CommunicationTagAttributeProperty
        extends AttributeProperty<CommunicationTag>
{
    private final boolean write;
    public CommunicationTagAttributeProperty(String key, boolean write)
    {
        super(key, null);

        this.write = write;
    }

    @Override
    public TagData createData(Attribute attribute)
    {
        return new TagData(attribute);
    }

    public class TagData
            extends AttributeProperty.Data<CommunicationTag>
            implements Taggable
    {
        private final Attribute attribute;
        private final Runnable tagValueChangeConsumer;

        protected TagData(Attribute attribute)
        {
            super(CommunicationTagAttributeProperty.this);

            this.attribute = attribute;

            tagValueChangeConsumer = () ->
                    AttributePropertyManager.updateAttribute(attribute);

            property.addListener((observable, oldValue, newValue) ->
            {
                if(write)
                {
                    return;
                }

                if(oldValue != null)
                {
                    oldValue.getReadIntermediate().removeNewValueRunnable(tagValueChangeConsumer);
                }

                if(newValue != null)
                {
                    newValue.getReadIntermediate().addNewValueRunnable(tagValueChangeConsumer);
                }
            });
        }

        @Override
        public boolean requireReading()
        {
            return !write;
        }

        @Override
        public boolean isActive()
        {
            return attribute.getAttributeMap().getControlWrapper()
                    .getContainerPane().isVisible();
        }

        @Override
        public void serializeInto(JSONDataMap jsonDataMap)
        {
            var tag = property.getValue();
            if(tag != null)
            {
                jsonDataMap.set("TagID", tag.getInternalId());
            }
        }

        @Override
        public void deserializeFrom(JSONDataMap jsonDataMap)
        {
            var tagStage = attribute.getAttributeMap().getControlWrapper()
                    .getControlMainPage().getMainEditStage().getTagStage();

            var tagId = jsonDataMap.getNumber("TagID");
            if(tagId != null)
            {
                var tag = tagStage.getTagFromId(tagId.intValue());
                if(tag instanceof CommunicationTag)
                {
                    super.setValue((CommunicationTag) tag);
                }
            }
        }
    }

}