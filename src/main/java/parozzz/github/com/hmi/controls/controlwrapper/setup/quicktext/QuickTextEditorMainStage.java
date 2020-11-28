package parozzz.github.com.hmi.controls.controlwrapper.setup.quicktext;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.page.HMIStage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuickTextEditorMainStage extends HMIStage<VBox>
{
    @FXML
    private VBox firstRowVBox;
    @FXML
    private VBox secondRowVBox;

    private final List<QuickTextEditorStatePane> statePaneList;

    public QuickTextEditorMainStage() throws IOException
    {
        super("QuickTextEditor", "setup/quicktext/quickTextEditorMainPane.fxml", VBox.class);

        this.statePaneList = new ArrayList<>();
    }

    @Override
    public void setup()
    {
        super.setup();
    }

    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        firstRowVBox.getChildren().clear();
        secondRowVBox.getChildren().clear();

        if(controlWrapper == null)
        {
            return;
        }

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
    }

}
