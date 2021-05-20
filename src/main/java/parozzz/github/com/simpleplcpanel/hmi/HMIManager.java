package parozzz.github.com.simpleplcpanel.hmi;

import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;

import java.io.IOException;

public final class HMIManager extends FXController
{
    private final TagsManager tagsManager;
    private final CommunicationDataHolder communicationDataHolder;
    private final MainEditStage mainEditStage;

    public HMIManager(CommunicationDataHolder communicationDataHolder, Runnable saveDataRunnable) throws IOException
    {
        super.addFXChild(this.communicationDataHolder = communicationDataHolder)
                .addFXChild(this.tagsManager = new TagsManager(communicationDataHolder))
                .addFXChild(this.mainEditStage = new MainEditStage(tagsManager, communicationDataHolder, saveDataRunnable));
    }

    public CommunicationDataHolder getCommunicationDataHolder()
    {
        return communicationDataHolder;
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    public TagsManager getTagStage()
    {
        return tagsManager;
    }

    public void showStage()
    {
        mainEditStage.showStage();
    }

    @Override
    public JSONDataMap serialize()
    {
        return super.serialize();
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        super.deserialize(jsonDataMap);
    }
}
