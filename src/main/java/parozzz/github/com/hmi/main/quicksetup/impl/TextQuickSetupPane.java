package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public final class TextQuickSetupPane extends FXObject implements QuickSetupPane
{
    @FXML
    private TextArea textTextArea;

    private final VBox vBox;

    public TextQuickSetupPane() throws IOException
    {
        super("TextQuickPropertiesPane");

        this.vBox = (VBox) FXUtil.loadFXML("quickproperties/textQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setMargin(vBox, new Insets(2, 0, 0, 0));
    }

    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public void onNewControlWrapper(ControlWrapper<?> controlWrapper)
    {
        vBox.setVisible(controlWrapper.getAttributeManager().hasType(AttributeType.TEXT));
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(AttributeType.TEXT)
                .direct(textTextArea.textProperty(), TextAttribute.TEXT);
    }

    @Override
    public void clear()
    {
        QuickSetupPane.super.clear();

        textTextArea.setText("");
    }
}
