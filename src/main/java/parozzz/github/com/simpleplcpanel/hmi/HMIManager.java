package parozzz.github.com.simpleplcpanel.hmi;

import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;

import java.io.IOException;

public final class HMIManager extends FXController
{
    private final TagStage tagStage;
    private final CommunicationDataHolder communicationDataHolder;
    private final MainEditStage mainEditStage;

    public HMIManager(CommunicationDataHolder communicationDataHolder, Runnable saveDataRunnable) throws IOException
    {
        super.addFXChild(this.communicationDataHolder = communicationDataHolder)
                .addFXChild(this.tagStage = new TagStage(communicationDataHolder))
                .addFXChild(this.mainEditStage = new MainEditStage(tagStage, communicationDataHolder, saveDataRunnable));
    }

    public CommunicationDataHolder getCommunicationDataHolder()
    {
        return communicationDataHolder;
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    public TagStage getTagStage()
    {
        return tagStage;
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
