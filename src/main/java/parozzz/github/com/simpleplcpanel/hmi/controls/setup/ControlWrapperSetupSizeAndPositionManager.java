package parozzz.github.com.simpleplcpanel.hmi.controls.setup;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.textfields.IntegerTextField;

public final class ControlWrapperSetupSizeAndPositionManager extends FXObject
{
    private final static String ATTRIBUTE_LISTENER_KEY = "ControlWrapperSetupSizeAndPositionManager_AttributeKey";

    private final IntegerTextField xTextField;
    private final IntegerTextField yTextField;
    private final IntegerTextField widthTextField;
    private final IntegerTextField heightTextField;
    private final CheckBox adaptSizeCheckBox;

    private final ControlWrapperSetupStage setupStage;

    private final ChangeListener<? super Number> layoutXListener;
    private final ChangeListener<? super Number> layoutYListener;
    private ControlWrapper<?> selectedControlWrapper;

    public ControlWrapperSetupSizeAndPositionManager(ControlWrapperSetupStage setupStage)
    {
        this.setupStage = setupStage;

        this.xTextField = new IntegerTextField(
                FXUtil.findNestedChild(setupStage, "xTextField", TextField.class)).newValueConsumer(this::setLayoutX);

        this.yTextField = new IntegerTextField(
                FXUtil.findNestedChild(setupStage, "yTextField", TextField.class)).newValueConsumer(this::setLayoutY);

        this.widthTextField = new IntegerTextField(
                FXUtil.findNestedChild(setupStage, "widthTextField", TextField.class)).newValueConsumer(this::setWidth);

        this.heightTextField = new IntegerTextField(
                FXUtil.findNestedChild(setupStage, "heightTextField", TextField.class)).newValueConsumer(this::setHeight);

        this.adaptSizeCheckBox = FXUtil.findNestedChild(setupStage, "adaptSizeCheckBox", CheckBox.class);

        layoutXListener = (observable, oldValue, newValue) -> this.xTextField.setValue(newValue.intValue());
        layoutYListener = (observable, oldValue, newValue) -> this.yTextField.setValue(newValue.intValue());
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        xTextField.init();
        yTextField.init();
        widthTextField.init();
        heightTextField.init();

        adaptSizeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
        {//Disable the text fields if the adaptSize option is enabled.
            widthTextField.getTextField().setDisable(newValue);
            heightTextField.getTextField().setDisable(newValue);
        });

        adaptSizeCheckBox.setOnAction(event ->
        {
            if (selectedControlWrapper != null)
            {
                AttributeFetcher.fetchRequired(selectedControlWrapper, AttributeType.SIZE)
                        .setValue(SizeAttribute.ADAPT, adaptSizeCheckBox.isSelected());
                this.updateValuesFromAttribute(); //This is in case my change was not effective, it will reload them!
            }
        });
    }

    public void setControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if (selectedControlWrapper != null)
        {
            selectedControlWrapper.layoutXProperty().removeListener(layoutXListener);
            selectedControlWrapper.layoutYProperty().removeListener(layoutYListener);

            var sizeAttribute = AttributeFetcher.fetchRequired(selectedControlWrapper, AttributeType.SIZE);
            sizeAttribute.removeAttributeUpdaterRunnable(ATTRIBUTE_LISTENER_KEY);

            selectedControlWrapper = null;
        }

        if (controlWrapper != null)
        {
            this.selectedControlWrapper = controlWrapper;

            selectedControlWrapper.layoutXProperty().addListener(layoutXListener);
            selectedControlWrapper.layoutYProperty().addListener(layoutYListener);

            AttributeFetcher.fetchRequired(selectedControlWrapper, AttributeType.SIZE)
                    .addAttributeUpdaterRunnable(ATTRIBUTE_LISTENER_KEY, this::updateValuesFromAttribute);

            this.updateLayoutValuesFromControlWrapper();
            this.updateValuesFromAttribute();
        }
    }

    private void setLayoutX(int x)
    {
        if (selectedControlWrapper != null)
        {
            //When a ControlWrapper is on their SetupPage SHOULD be the only one selected,
            //but nothing bad should happen if there is a bug in the system, since it moved them together
            var xDiff = x - selectedControlWrapper.getLayoutX();
            selectedControlWrapper.getDragAndResizeObject().move(xDiff, 0, false);
            //This is in case my change was not effective, it will reload them!
            this.updateLayoutValuesFromControlWrapper();
        }
    }

    private void setLayoutY(int y)
    {
        if (selectedControlWrapper != null)
        {
            //When a ControlWrapper is on their SetupPage SHOULD be the only one selected,
            //but nothing bad should happen if there is a bug in the system, since it moved them together
            var yDiff = y - selectedControlWrapper.getLayoutY();
            selectedControlWrapper.getDragAndResizeObject().move(0, yDiff, false);
            //This is in case my change was not effective, it will reload them!
            this.updateLayoutValuesFromControlWrapper();
        }
    }

    private void setWidth(int width)
    {
        if (selectedControlWrapper != null)
        {
            selectedControlWrapper.getDragAndResizeObject().resize(width, 0, false);
            this.updateValuesFromAttribute();//This is in case my change was not effective, it will reload them!
        }
    }

    private void setHeight(int height)
    {
        if (selectedControlWrapper != null)
        {
            selectedControlWrapper.getDragAndResizeObject().resize(0, height, false);
            this.updateValuesFromAttribute();//This is in case my change was not effective, it will reload them!
        }
    }

    private void updateLayoutValuesFromControlWrapper()
    {
        if (selectedControlWrapper != null)
        {
            xTextField.setValue((int) selectedControlWrapper.getLayoutX());
            yTextField.setValue((int) selectedControlWrapper.getLayoutY());
        }
    }

    private void updateValuesFromAttribute()
    {
        if (selectedControlWrapper != null)
        {
            var sizeAttribute = AttributeFetcher.fetchRequired(selectedControlWrapper, AttributeType.SIZE);
            widthTextField.setValue(sizeAttribute.getValue(SizeAttribute.WIDTH));
            heightTextField.setValue(sizeAttribute.getValue(SizeAttribute.HEIGHT));
            adaptSizeCheckBox.setSelected(sizeAttribute.getValue(SizeAttribute.ADAPT));
        }
    }
}
