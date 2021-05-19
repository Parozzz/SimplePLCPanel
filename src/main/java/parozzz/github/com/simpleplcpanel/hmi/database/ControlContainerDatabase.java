package parozzz.github.com.simpleplcpanel.hmi.database;

import net.wimpi.modbus.Modbus;
import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.tcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.database.dataupdater.ControlDataUpdater;
import parozzz.github.com.simpleplcpanel.hmi.database.dataupdater.ModbusTCPDataUpdater;
import parozzz.github.com.simpleplcpanel.hmi.database.dataupdater.SiemensPLCDataUpdater;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.Cooldown;

import java.io.IOException;
import java.util.*;

public final class ControlContainerDatabase extends FXController implements Iterable<ControlContainerPane>, Loggable
{
    private final MainEditStage mainEditStage;
    private final CommunicationDataHolder communicationDataHolder;

    private final Map<CommunicationType<?>, ControlDataUpdater<?>> controlDataUpdaterMap;
    private ControlDataUpdater<?> selectedControlDataUpdater;
    private CommunicationType<?> nextControlDataCommunicationType;

    private final Cooldown nextDataUpdateCooldown;
    private boolean parseUpdatedData;

    private final Map<String, ControlContainerPane> controlsPageMap;
    private final Set<ControlWrapper<?>> controlWrapperSet;
    private Set<ControlWrapper<?>> immutableControlWrapperSet;

    public ControlContainerDatabase(MainEditStage mainEditStage, TagStage tagStage, CommunicationDataHolder communicationDataHolder)
    {
        this.mainEditStage = mainEditStage;
        this.communicationDataHolder = communicationDataHolder;

        this.controlDataUpdaterMap = new HashMap<>();
        controlDataUpdaterMap.put(
                CommunicationType.SIEMENS_S7,
                SiemensPLCDataUpdater.createInstance(tagStage, this, communicationDataHolder)
        );
        controlDataUpdaterMap.put(
                CommunicationType.MODBUS_TCP,
                ModbusTCPDataUpdater.createInstance(tagStage, this, communicationDataHolder)
        );

        this.nextDataUpdateCooldown = new Cooldown(200);

        this.controlsPageMap = new HashMap<>();
        this.controlWrapperSet = new HashSet<>();
        this.immutableControlWrapperSet = Collections.unmodifiableSet(controlWrapperSet);
    }

    @Override
    public void setup()
    {
        super.setup();

        communicationDataHolder.getCommunicationStage().addCommunicationTypeListener(communicationType ->
                this.nextControlDataCommunicationType = communicationType
        );
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

        this.nextControlDataCommunicationType = communicationDataHolder.getCommunicationStage().getCommunicationType();
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
        if (doNameExists(name))
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
/*
                        if (selectedControlDataUpdater != null)
                        {
                            selectedControlDataUpdater.bindControlWrapper(controlWrapper);
                        }*/
                    },
                    controlWrapper ->
                    {
                        controlWrapperSet.remove(controlWrapper);
                        immutableControlWrapperSet = Collections.unmodifiableSet(controlWrapperSet);
/*
                        if (selectedControlDataUpdater != null)
                        {
                            selectedControlDataUpdater.unbindControlWrapper(controlWrapper);
                        }*/
                    });
            controlContainerPanelMainPage.setup();
            if (setDefault)
            {
                controlContainerPanelMainPage.setDefault();
            }

            this.addPage(controlContainerPanelMainPage);
            return controlContainerPanelMainPage;
        } catch (IOException exception)
        {
            MainLogger.getInstance().warning("Something went wrong while create a new Page", exception, this);
            return null;
        }
    }

    public void addPage(ControlContainerPane controlContainerPane)
    {
        if (!controlContainerPane.isSetupDone())
        {
            MainLogger.getInstance().warning("Cannot add a non initialized ControlContainerPane", this);
            return;
        }

        if (controlsPageMap.containsKey(controlContainerPane.getName()))
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
        if (controlsPageMap.remove(controlContainer.getName(), controlContainer))
        {
            mainEditStage.getPageScrollingPane().removeImagePane(controlContainer.getMenuBottomImagePane());
            mainEditStage.setShownControlContainerPane(null);
        }
    }

    private void changeSelectedDataUpdater()
    {
        if (nextControlDataCommunicationType == null)
        {
            return;
        }

        //If i have to change data updater, i need to do it only when the selected data updater has finished (aka is ready)
        if (selectedControlDataUpdater != null && !selectedControlDataUpdater.isReady())
        {
            return;
        }
/*
        if (selectedControlDataUpdater != null)
        {
            controlWrapperSet.forEach(selectedControlDataUpdater::unbindControlWrapper);
        }
*/
        selectedControlDataUpdater = controlDataUpdaterMap.get(nextControlDataCommunicationType);
        nextControlDataCommunicationType = null;
/*
        if (selectedControlDataUpdater != null)
        {
            controlWrapperSet.forEach(selectedControlDataUpdater::bindControlWrapper);
        }*/
    }

    private void updateSelectedDataUpdater()
    {
        if (selectedControlDataUpdater == null)
        {
            return;
        }

        if (!selectedControlDataUpdater.isReady())
        {
            //Keep the cooldown refreshed so when the updater is ready, will wait the full time
            nextDataUpdateCooldown.createStamp();
            return;
        }

        //If the data updater is ready it means the plc thread has finished, so i update the read data
        if (parseUpdatedData)
        {
            selectedControlDataUpdater.parseReadData();
            parseUpdatedData = false;
        }
        //And then wait some time for the next update
        if (nextDataUpdateCooldown.passed())
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
        if (pagesJSONArray == null)
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
                    if (pageName != null)
                    {
                        var controlWrapperPage = this.create(pageName, false);
                        if (controlWrapperPage != null)
                        {
                            controlWrapperPage.deserialize(pageJSONDataMap);
                            //No setupComplete here, is called automagically inside the FXController
                        } else
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
