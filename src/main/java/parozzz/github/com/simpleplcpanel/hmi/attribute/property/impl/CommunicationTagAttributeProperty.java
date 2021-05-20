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
    private final boolean requireReading;
    public CommunicationTagAttributeProperty(String key, boolean requireReading)
    {
        super(key, null, true);

        this.requireReading = requireReading;
    }

    @Override
    public TagData createData(Attribute attribute)
    {
        return new TagData(attribute);
    }

    public class TagData
            extends AttributeProperty<CommunicationTag>.Data
            implements Taggable
    {
        private final Attribute attribute;
        protected TagData(Attribute attribute)
        {
            super();

            this.attribute = attribute;

            property.addListener((observable, oldValue, newValue) ->
            {
                if(oldValue != null)
                {
                    oldValue.removeTaggable(this);
                    oldValue.getReadIntermediate().removeNewValueRunnable(this);
                }

                if(newValue != null)
                {
                    newValue.addTaggable(this);
                    newValue.getReadIntermediate().addNewValueRunnable(
                            this,
                            () -> AttributePropertyManager.updateAttribute(attribute)
                    );
                }
            });
        }

        @Override
        public boolean requireReading()
        {
            return requireReading;
        }

        @Override
        public boolean isActive()
        {
            return attribute.getAttributeMap().getControlWrapper().isActive();
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
                    .getControlMainPage().getMainEditStage().getTagsManager();

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