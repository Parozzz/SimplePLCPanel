package parozzz.github.com.simpleplcpanel.hmi.database;

import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.Cooldown;

import java.io.IOException;
import java.util.*;

public final class ControlContainerDatabase extends FXController implements Iterable<ControlContainerPane>, Loggable
{
    private final MainEditStage mainEditStage;

    private final ControlDataUpdater<?> siemensPLCDataUpdater;
    private final ControlDataUpdater<?> modbusTCPDataUpdater;
    private ControlDataUpdater<?> selectedControlDataUpdater;
    private ControlDataUpdater<?> nextControlDataUpdater;

    private final Cooldown nextDataUpdateCooldown;
    private boolean parseUpdatedData;

    private final Map<String, ControlContainerPane> controlsPageMap;
    private final Set<ControlWrapper<?>> controlWrapperSet;
    private Set<ControlWrapper<?>> immutableControlWrapperSet;

    public ControlContainerDatabase(MainEditStage mainEditStage, SiemensS7Thread plcThread,
            ModbusTCPThread modbusTCPThread)
    {
        this.mainEditStage = mainEditStage;

        this.siemensPLCDataUpdater = new SiemensPLCDataUpdater(this, plcThread);
        this.modbusTCPDataUpdater = new ModbusTCPDataUpdater(this, modbusTCPThread);
        this.nextDataUpdateCooldown = new Cooldown(200);

        this.controlsPageMap = new HashMap<>();
        this.controlWrapperSet = new HashSet<>();
        this.immutableControlWrapperSet = Collections.unmodifiableSet(controlWrapperSet);
    }

    @Override
    public void setup()
    {
        super.setup();

        mainEditStage.getCommunicationStage().addCommunicationTypeListener(this::updateCommunicationManager);
    }

    @Override
    public void loop()
    {
        super.loop();

        this.changeSelectedDataUpdater();
        this.updateSelectedDataUpdater();
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        this.updateCommunicationManager(mainEditStage.getCommunicationStage().getCommunicationType());
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    protected Set<ControlWrapper<?>> getControlWrapperSet()
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

                        if(selectedControlDataUpdater != null)
                        {
                            selectedControlDataUpdater.bindControlWrapper(controlWrapper);
                        }
                    },
                    controlWrapper ->
                    {
                        controlWrapperSet.remove(controlWrapper);
                        immutableControlWrapperSet = Collections.unmodifiableSet(controlWrapperSet);

                        if(selectedControlDataUpdater != null)
                        {
                            selectedControlDataUpdater.unbindControlWrapper(controlWrapper);
                        }
                    });
            controlContainerPanelMainPage.setup();
            if(setDefault)
            {
                controlContainerPanelMainPage.setDefault();
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
        mainEditStage.getBottomScrollingPane().addImagePane(controlContainerPane.getMenuBottomImagePane());
    }

    public void deletePage(ControlContainerPane controlContainer)
    {
        if(controlsPageMap.remove(controlContainer.getName(), controlContainer))
        {
            mainEditStage.getBottomScrollingPane().removeImagePane(controlContainer.getMenuBottomImagePane());
            mainEditStage.setShownControlContainerPane(null);
        }
    }

    private void updateCommunicationManager(CommunicationType communicationType)
    {
        if(communicationType == null)
        {
            return;
        }

        //Just to be sure, on setup complete i will set the next data updater since it does not set
        //the first time from the listener created in the setup
        switch(mainEditStage.getCommunicationStage().getCommunicationType())
        {
            case SIEMENS_S7:
                nextControlDataUpdater = siemensPLCDataUpdater;
                break;
            case MODBUS_TCP:
                nextControlDataUpdater = modbusTCPDataUpdater;
                break;
        }
    }

    private void changeSelectedDataUpdater()
    {
        //If i have to change data updater, i need to do it only when the selected data updater has finished (aka is ready)
        if(nextControlDataUpdater == null || (selectedControlDataUpdater != null && !selectedControlDataUpdater.isReady()))
        {
            return;
        }

        if(selectedControlDataUpdater != null)
        {
            controlWrapperSet.forEach(selectedControlDataUpdater::unbindControlWrapper);
        }

        selectedControlDataUpdater = nextControlDataUpdater;
        nextControlDataUpdater = null;

        if(selectedControlDataUpdater != null)
        {
            controlWrapperSet.forEach(selectedControlDataUpdater::bindControlWrapper);
        }
    }

    private void updateSelectedDataUpdater()
    {
        if(selectedControlDataUpdater == null || !selectedControlDataUpdater.isReady())
        {
            //Keep the cooldown refreshed so when the updater is ready, will wait the full time
            nextDataUpdateCooldown.createStamp();
            return;
        }

        //If the data updater is ready it means the plc thread has finished, so i update the read data
        if(parseUpdatedData)
        {
            selectedControlDataUpdater.parseReadData();
            parseUpdatedData = false;
        }
        //And then wait some time for the next update
        if(nextDataUpdateCooldown.passed())
        {
            selectedControlDataUpdater.update();
            parseUpdatedData = true;
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
                "ControlWrapperAmount: " + controlWrapperSet.size() +
                "ActiveControlDataUpdater: " + this.getSelectedControlDataUpdaterName();
    }

    private String getSelectedControlDataUpdaterName()
    {
        return selectedControlDataUpdater == null
               ? "none"
               : selectedControlDataUpdater.getClass().getSimpleName();
    }
}
