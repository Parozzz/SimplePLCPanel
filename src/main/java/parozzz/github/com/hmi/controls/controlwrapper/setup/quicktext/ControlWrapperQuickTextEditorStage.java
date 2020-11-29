package parozzz.github.com.hmi.controls.controlwrapper.setup.quicktext;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.hmi.page.HMIStage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ControlWrapperQuickTextEditorStage extends HMIStage<VBox> implements ControlWrapperSpecific
{
    @FXML
    private VBox firstRowVBox;
    @FXML
    private VBox secondRowVBox;

    private final List<QuickTextEditorStatePane> statePaneList;

    private final ChangeListener<Boolean> controlWrapperValidListener;
    private final Consumer<Object> attributesUpdatedConsumer;
    private ControlWrapper<?> selectedControlWrapper;
    private boolean ignoreAttributeUpdate;

    public ControlWrapperQuickTextEditorStage() throws IOException
    {
        super("QuickTextEditor", "setup/quicktext/quickTextEditorMainPane.fxml", VBox.class);

        this.statePaneList = new ArrayList<>();
        controlWrapperValidListener = (observableValue, oldValue, newValue) ->
        {
            if(!newValue)
            {
                this.setSelectedControlWrapper(null);
            }
        };
        attributesUpdatedConsumer = involvedObject ->
        {
          if(involvedObject != this)
          {
              ignoreAttributeUpdate = true;
              statePaneList.forEach(QuickTextEditorStatePane::refreshValues);
              ignoreAttributeUpdate = false;
          }
        };
    }

    @Override
    public void setup()
    {
        super.setup();

        this.getStageSetter().setOnWindowCloseRequest(windowEvent -> this.setSelectedControlWrapper(null));
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
            selectedControlWrapper.removeAttributesUpdatedConsumer(attributesUpdatedConsumer);
        }

        this.selectedControlWrapper = controlWrapper;
        if(controlWrapper == null)
        {
            this.getStageSetter().close();
            return;
        }

        controlWrapper.validProperty().addListener(controlWrapperValidListener);
        controlWrapper.addAttributesUpdatedConsumer(attributesUpdatedConsumer);
        controlWrapper.getStateMap().forEach(wrapperState ->
        {
            try
            {
                var statePane = new QuickTextEditorStatePane(this, wrapperState);
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

    void applyAttributesToSelectedControlWrapper()
    {
        if(selectedControlWrapper != null && !ignoreAttributeUpdate)
        {
            selectedControlWrapper.applyAttributes(this);
        }
    }
}