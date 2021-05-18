package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Taggable;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CommunicationTagAttributeProperty
        extends FunctionAttributeProperty<CommunicationTag>
{
    public CommunicationTagAttributeProperty(String key, CommunicationTag defaultValue,
            Function<CommunicationTag, Object> serializeParseFunction,
            BiFunction<JSONDataMap, String, CommunicationTag> deserializeFunction)
    {
        super(key, defaultValue, serializeParseFunction, deserializeFunction);
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

        protected TagData(Attribute attribute)
        {
            super(CommunicationTagAttributeProperty.this);

            this.attribute = attribute;

            property.addListener((observable, oldValue, newValue) ->
            {

            });
        }

        @Override
        public boolean isActive()
        {
            return attribute.getAttributeMap().getControlWrapper().getContainerPane().isVisible();
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