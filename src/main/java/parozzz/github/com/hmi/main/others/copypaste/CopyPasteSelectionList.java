package parozzz.github.com.hmi.main.others.copypaste;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CopyPasteSelectionList implements java.io.Serializable
{
    private List<JSONObject> serializedControlWrapperList;
    public CopyPasteSelectionList()
    {
        this.serializedControlWrapperList = new ArrayList<>();
    }

    public void add(ControlWrapper<?> controlWrapper)
    {
        var json = controlWrapper.serialize().getJson();
        serializedControlWrapperList.add(json);
    }

    public List<ControlWrapper<?>> getDeserializedControlWrapperList(ControlContainerPane controlContainerPane)
    {
        if(serializedControlWrapperList.isEmpty())
        {
            return List.of();
        }

        var controlWrapperList = new ArrayList<ControlWrapper<?>>();
        for(var jsonObject : serializedControlWrapperList)
        {
            var wrapperTypeString = jsonObject.get("WrapperType");

            var wrapperType = ControlWrapperType.getFromName(wrapperTypeString.toString());
            if(wrapperType == null)
            {
                continue;
            }

            var controlWrapper = wrapperType.createWrapper(controlContainerPane);
            controlWrapper.setup();
            controlWrapper.deserialize(new JSONDataMap(jsonObject));
            controlWrapperList.add(controlWrapper);
        }
        return controlWrapperList;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        var stringArray = serializedControlWrapperList.stream()
                .map(JSONAware::toJSONString)
                .toArray(String[]::new);
        out.writeObject(stringArray);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        if(serializedControlWrapperList == null)
        {
            serializedControlWrapperList = new ArrayList<>();
        }

        var readObject = in.readObject();
        if(readObject instanceof String[])
        {
            var jsonParser = new JSONParser();

            var array = (String[]) readObject;
            for(var jsonString : array)
            {
                try
                {
                    var parsedObject = jsonParser.parse(jsonString);
                    if(parsedObject instanceof JSONObject)
                    {
                        serializedControlWrapperList.add((JSONObject) parsedObject);
                    }
                } catch (ParseException e) {
                }
            }
        }

    }

    private void readObjectNoData() throws ObjectStreamException
    {

    }
}
