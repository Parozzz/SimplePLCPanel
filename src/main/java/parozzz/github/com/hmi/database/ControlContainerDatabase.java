package parozzz.github.com.hmi.database;

import org.json.simple.JSONObject;
import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.hmi.comm.siemens.SiemensPLCThread;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.main.MainEditStage;
import parozzz.github.com.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.util.Cooldown;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ControlContainerDatabase extends FXController
{
    private final static Logger logger = Logger.getLogger(ControlContainerDatabase.class.getSimpleName());

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

    public ControlContainerDatabase(MainEditStage mainEditStage, SiemensPLCThread plcThread,
                                    ModbusTCPThread modbusTCPThread)
    {
        super("ControlPageDatabase");

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

        mainEditStage.getCommunicationStage().addCommunicationTypeListener(communicationType ->
        {
            switch (communicationType)
            {
                case SIEMENS:
                    nextControlDataUpdater = siemensPLCDataUpdater;
                    break;
                case MODBUS_TCP:
                    nextControlDataUpdater = modbusTCPDataUpdater;
                    break;
            }
        });
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

        //Just to be sure, on setup complete i will set the next data updater since it does not set
        //the first time from the listener created in the setup
        switch (mainEditStage.getCommunicationStage().getCommunicationType())
        {
            case SIEMENS:
                nextControlDataUpdater = siemensPLCDataUpdater;
                break;
            case MODBUS_TCP:
                nextControlDataUpdater = modbusTCPDataUpdater;
                break;
        }
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

    public void deletePage(ControlContainerPane controlContainer)
    {
        if (controlsPageMap.remove(controlContainer.getName(), controlContainer))
        {
            mainEditStage.getBottomScrollingPane().removeImagePane(controlContainer.getMenuBottomImagePane());
            mainEditStage.setShownControlContainerPane(null);
        }
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
            var controlMainPage = new ControlContainerPane(this, mainEditStage, name,
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
            controlMainPage.setup();

            if (setDefault)
            {
                controlMainPage.setDefault();
            }

            controlsPageMap.putIfAbsent(name, controlMainPage);
            mainEditStage.getBottomScrollingPane().addImagePane(controlMainPage.getMenuBottomImagePane());

            super.addFXChild(controlMainPage, false);

            return controlMainPage;
        } catch (IOException exception)
        {
            logger.log(Level.WARNING, "Something went wrong while create a new Page", exception);
            return null;
        }
    }

    private void changeSelectedDataUpdater()
    {
        //If i have to change data updater, i need to do it only when the selected data updater has finished (aka is ready)
        if (nextControlDataUpdater == null || (selectedControlDataUpdater != null && !selectedControlDataUpdater.isReady()))
        {
            return;
        }

        if (selectedControlDataUpdater != null)
        {
            controlWrapperSet.forEach(selectedControlDataUpdater::unbindControlWrapper);
        }

        selectedControlDataUpdater = nextControlDataUpdater;
        nextControlDataUpdater = null;

        if (selectedControlDataUpdater != null)
        {
            controlWrapperSet.forEach(selectedControlDataUpdater::bindControlWrapper);
        }
    }

    private void updateSelectedDataUpdater()
    {
        if (selectedControlDataUpdater == null || !selectedControlDataUpdater.isReady())
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
        if (pagesJSONArray != null)
        {
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
                                logger.log(Level.WARNING, "Creating while deserializing a ControlMainPage has returned null.");
                            }
                        }
                    });
        } else
        {
            logger.log(Level.WARNING, "Pages JSONArray has not been found while de-serializing");
        }

    }
}
