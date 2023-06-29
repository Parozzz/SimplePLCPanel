package parozzz.github.com.simpleplcpanel.hmi.controls;

import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.IOException;
import java.util.*;

public final class ControlContainerDatabase extends FXController implements Iterable<ControlContainerPane>, Loggable
{
    private final MainEditStage mainEditStage;

    private final Map<String, ControlContainerPane> controlsPageMap;
    private final Set<ControlWrapper<?>> controlWrapperSet;
    private Set<ControlWrapper<?>> immutableControlWrapperSet;

    public ControlContainerDatabase(MainEditStage mainEditStage)
    {
        this.mainEditStage = mainEditStage;

        this.controlsPageMap = new HashMap<>();
        this.controlWrapperSet = new HashSet<>();
        this.immutableControlWrapperSet = Collections.unmodifiableSet(controlWrapperSet);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();
    }

    @Override
    public void onLoop()
    {
        super.onLoop();
    }

    @Override
    public void onSetupComplete()
    {
        super.onSetupComplete();
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    public Set<ControlWrapper<?>> getControlWrapperSet()
    {
        return immutableControlWrapperSet;
    }

    public ControlContainerPane getByName(String name)
    {
        Objects.requireNonNull(name);
        return controlsPageMap.get(name.toLowerCase());
    }

    public boolean doNameExists(String name)
    {
        Objects.requireNonNull(name);
        return controlsPageMap.containsKey(name.toLowerCase());
    }

    public List<ControlContainerPane> getPageList()
    {
        return List.copyOf(controlsPageMap.values());
    }

    public ControlContainerPane create(String name)
    {
        return create(name, true);
    }

    private ControlContainerPane create(String name, boolean setDefault)
    {
        name = name.toLowerCase();
        if(doNameExists(name))
        {
            return null;
        }

        try
        {
            var controlContainerPanelMainPage = new ControlContainerPane(mainEditStage,
                    this, name,
                    controlWrapper ->
                    {
                        controlWrapperSet.add(controlWrapper);
                        immutableControlWrapperSet = Collections.unmodifiableSet(controlWrapperSet);
                    },
                    controlWrapper ->
                    {
                        controlWrapperSet.remove(controlWrapper);
                        immutableControlWrapperSet = Collections.unmodifiableSet(controlWrapperSet);
                    });
            controlContainerPanelMainPage.onSetup();
            if(setDefault)
            {
                controlContainerPanelMainPage.onSetDefault();
            }

            this.addPage(controlContainerPanelMainPage);
            return controlContainerPanelMainPage;
        }
        catch(IOException exception)
        {
            MainLogger.getInstance().warning("Something went wrong while create a new Page", exception, this);
            return null;
        }
    }

    public void addPage(ControlContainerPane controlContainerPane)
    {
        if(!controlContainerPane.isSetupDone())
        {
            MainLogger.getInstance().warning("Cannot add a non initialized ControlContainerPane", this);
            return;
        }

        if(controlsPageMap.containsKey(controlContainerPane.getName()))
        {
            MainLogger.getInstance().warning("Trying to add a ControlContainerPane twice", this);
            return;
        }

        super.addFXChild(controlContainerPane, false);
        controlsPageMap.put(controlContainerPane.getName(), controlContainerPane);
        mainEditStage.getPageScrollingPane().addImagePane(controlContainerPane.getMenuBottomImagePane());
    }

    public void deletePage(ControlContainerPane controlContainer)
    {
        if(controlsPageMap.remove(controlContainer.getName(), controlContainer))
        {
            mainEditStage.getPageScrollingPane().removeImagePane(controlContainer.getMenuBottomImagePane());
            mainEditStage.setShownControlContainerPane(null);
        }
    }

    @Override
    public JSONDataMap serialize()
    {
        var jsonDataMap = super.serialize();
        jsonDataMap.set("Pages", JSONDataArray.of(controlsPageMap.values()));
        return jsonDataMap;
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        super.deserialize(jsonDataMap);

        var pagesJSONArray = jsonDataMap.getArray("Pages");
        if(pagesJSONArray == null)
        {
            MainLogger.getInstance().warning("Pages JSONArray has not been found while de-serializing", this);
            return;
        }

        pagesJSONArray.stream().filter(JSONObject.class::isInstance)
                .map(JSONObject.class::cast)
                .map(JSONDataMap::new)
                .forEach(pageJSONDataMap ->
                {
                    var pageName = pageJSONDataMap.getString("ControlWrapperPageName");
                    if(pageName != null)
                    {
                        var controlWrapperPage = this.create(pageName, false);
                        if(controlWrapperPage != null)
                        {
                            controlWrapperPage.deserialize(pageJSONDataMap);
                            //No setupComplete here, is called automagically inside the FXController
                        }else
                        {
                            MainLogger.getInstance().warning("Creating while deserializing a ControlMainPage has returned null.", this);
                        }
                    }
                });
    }

    @Override
    public Iterator<ControlContainerPane> iterator()
    {
        return controlsPageMap.values().iterator();
    }

    @Override
    public String log()
    {
        return "Pages: " + Arrays.toString(controlsPageMap.keySet().toArray(String[]::new)) +
                "ControlWrapperAmount: " + controlWrapperSet.size();
    }
}
