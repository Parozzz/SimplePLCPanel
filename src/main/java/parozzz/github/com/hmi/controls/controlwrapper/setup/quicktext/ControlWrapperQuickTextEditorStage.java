package parozzz.github.com.hmi.controls.controlwrapper.setup.quicktext;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.hmi.controls.controlwrapper.attributes.ControlWrapperGenericAttributeUpdateConsumer;
import parozzz.github.com.hmi.page.HMIStage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ControlWrapperQuickTextEditorStage extends HMIStage<VBox> implements ControlWrapperSpecific
{
    @FXML
    private VBox firstRowVBox;
    @FXML
    private VBox secondRowVBox;

    private final List<QuickTextEditorStatePane> statePaneList;

    private final ChangeListener<Boolean> controlWrapperValidListener;
    private final ControlWrapperGenericAttributeUpdateConsumer attributeUpdatedConsumer;
    private ControlWrapper<?> selectedControlWrapper;

    public ControlWrapperQuickTextEditorStage() throws IOException
    {
        super("setup/quicktext/quickTextEditorMainPane.fxml", VBox.class);

        this.statePaneList = new ArrayList<>();
        this.controlWrapperValidListener = (observableValue, oldValue, newValue) ->
        {
            if(!newValue)
            {
                this.setSelectedControlWrapper(null);
            }
        };

        this.attributeUpdatedConsumer = updateData ->
                statePaneList.forEach(QuickTextEditorStatePane::refreshValues);
    }

    @Override
    public void setup()
    {
        super.setup();

        this.getStageSetter()
                .setAlwaysOnTop(true)
                .setOnWindowCloseRequest(windowEvent -> this.setSelectedControlWrapper(null));
    }

    @Override
    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        firstRowVBox.getChildren().clear();
        secondRowVBox.getChildren().clear();
        statePaneList.clear();

        if(selectedControlWrapper != null)
        {
            selectedControlWrapper.validProperty().removeListener(controlWrapperValidListener);
            selectedControlWrapper.getAttributeUpdater().removeGenericUpdateConsumer(attributeUpdatedConsumer);
        }

        this.selectedControlWrapper = controlWrapper;
        if(controlWrapper == null)
        {
            this.getStageSetter().close();
            return;
        }

        controlWrapper.validProperty().addListener(controlWrapperValidListener);
        controlWrapper.getAttributeUpdater().addGenericUpdateConsumer(attributeUpdatedConsumer);
        controlWrapper.getStateMap().forEach(wrapperState ->
        {
            try
            {
                var statePane = new QuickTextEditorStatePane(wrapperState);
                statePane.setup();
                statePaneList.add(statePane);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        });

        for(int x = 0; x < statePaneList.size(); x++)
        {
            var statePane = statePaneList.get(x);
            (x % 2 == 0 ? firstRowVBox : secondRowVBox).getChildren().add(statePane.getParent());
        }

        super.showStage();
    }

    @Override
    public ControlWrapper<?> getSelectedControlWrapper()
    {
        return selectedControlWrapper;
    }
}
