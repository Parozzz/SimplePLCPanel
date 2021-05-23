package parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers;

import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;

public final class CommunicationTypeCellFactoryHandler extends CellFactoryHandler<CommunicationType<?>>
{
    public CommunicationTypeCellFactoryHandler(CommunicationDataHolder communicationDataHolder)
    {
        super(communicationDataHolder);
    }

    @Override
    protected void registerTag(CommunicationTag tag)
    {

    }

    @Override
    protected void unregisterTag(CommunicationTag tag)
    {

    }

    @Override
    protected void setGraphic()
    {

    }
    /*
    private final ChoiceBox<CommunicationType<?>> choiceBox;

    public CommunicationTypeCellFactoryHandler(CommunicationDataHolder communicationDataHolder)
    {
        super(communicationDataHolder);

        this.choiceBox = new ChoiceBox<>();
    }

    @Override
    public void init()
    {
        super.init();

        choiceBox.setMinSize(0, 0);
        choiceBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        choiceBox.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        choiceBox.setBorder(null);
        choiceBox.setConverter(new StringConverter<>()
        {
            @Override
            public String toString(CommunicationType<?> object)
            {
                return object == null ? "" : Util.capitalizeWithUnderscore(object.getName());
            }

            @Override
            public CommunicationType<?> fromString(String string)
            {
                return null;
            }
        });
        choiceBox.getItems().addAll(CommunicationType.values());
    }

    @Override
    protected void registerTag(CommunicationTag tag)
    {
        //This need BEFORE BINDING BIDIRECTIONAL. If not set, it will change the value when bind.
        choiceBox.setValue(tag.getCommunicationType());

        tag.communicationTypeProperty().bindBidirectional(choiceBox.valueProperty());
    }

    @Override
    protected void unregisterTag(CommunicationTag tag)
    {
        tag.communicationTypeProperty().unbindBidirectional(choiceBox.valueProperty());

        //This need AFTER BINDING BIDIRECTIONAL. If not set, it will change the value inside the binding.
        choiceBox.setValue(null);
    }

    @Override
    protected void setGraphic()
    {
        cell.setGraphic(choiceBox);
    }*/
}
