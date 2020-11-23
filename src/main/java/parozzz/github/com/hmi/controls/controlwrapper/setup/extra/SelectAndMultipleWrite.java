package parozzz.github.com.hmi.controls.controlwrapper.setup.extra;

import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.util.Util;
import parozzz.github.com.util.functionalinterface.primitives.BooleanConsumer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class SelectAndMultipleWrite extends FXObject
{
    private final ControlWrapperSetupStage setupPage;

    private final ToggleButton selectMultipleToggleButton;
    private final ImageView selectMultipleImageView;

    private final Button writeToAllStateButton;
    private final ImageView writeToAllStateImageView;

    private final Set<PropertyData<?>> selectedPropertySet;

    public SelectAndMultipleWrite(ControlWrapperSetupStage setupPage,
                                  ToggleButton selectMultipleToggleButton, ImageView selectMultipleImageView,
                                  Button writeToAllStateButton, ImageView writeToAllStateImageView)
    {
        super("SelectAndMultipleWrite");

        this.setupPage = setupPage;

        this.selectMultipleToggleButton = selectMultipleToggleButton;
        this.selectMultipleImageView = selectMultipleImageView;
        this.writeToAllStateButton = writeToAllStateButton;
        this.writeToAllStateImageView = writeToAllStateImageView;

        this.selectedPropertySet = new HashSet<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        selectMultipleImageView.setImage(new Image(Util.getResource("images/select_multiple_icon.png").toExternalForm()));
        writeToAllStateImageView.setImage(new Image(Util.getResource("images/write_to_all_icon.png").toExternalForm()));

        selectMultipleToggleButton.setBackground(Background.EMPTY);
        selectMultipleToggleButton.setTooltip(new Tooltip("Select multiple fields"));
        selectMultipleToggleButton.selectedProperty().addListener((observableValue, oldValue, newValue) ->
        {
            Background background = Background.EMPTY;
            if (newValue)
            {
                background = new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY));
            }
            selectMultipleToggleButton.setBackground(background);
        });

        writeToAllStateButton.setBackground(Background.EMPTY);
        writeToAllStateButton.setTooltip(new Tooltip("Copy the selected fields to all states"));
        writeToAllStateButton.setOnAction(actionEvent ->
        {
            var selectedControlWrapper = setupPage.getSelectedControlWrapper();
            if (selectedControlWrapper != null)
            {
                selectedPropertySet.forEach(propertyData -> propertyData.writeToAllStates(selectedControlWrapper));
            }
        });
    }

    public void setSelectingMultiples(boolean selected)
    {
        selectMultipleToggleButton.setSelected(selected);
    }

    public boolean isSelectingMultiples()
    {
        return selectMultipleToggleButton.isSelected();
    }

    public void onSelectingMultiplesChangeListener(BooleanConsumer consumer)
    {
        selectMultipleToggleButton.selectedProperty()
                .addListener((observableValue, oldValue, newValue) -> consumer.accept(newValue));
    }

    public <A extends Attribute> void addSelected(Property<?> property, Class<A> attributeClass,
            SetupPaneAttributeChangerList<A> attributeChangerList)
    {
        selectedPropertySet.add(new PropertyData<>(property, attributeClass, attributeChangerList));
    }

    public void removeSelected(Property<?> property)
    {
        selectedPropertySet.removeIf(propertyData -> propertyData.property == property);
    }

    public void forEachSelected(Consumer<Property<?>> consumer)
    {
        selectedPropertySet.forEach(propertyData -> consumer.accept(propertyData.property));
    }

    public void clear()
    {
        selectMultipleToggleButton.setSelected(false);
        selectedPropertySet.clear();
    }

    private final static class PropertyData<A extends Attribute>
    {
        private final Property<?> property;
        private final Class<A> attributeClass;
        private final SetupPaneAttributeChangerList<A> attributeChangerList;

        public PropertyData(Property<?> property, Class<A> attributeClass,
                SetupPaneAttributeChangerList<A> attributeChangerList)
        {
            this.property = property;
            this.attributeClass = attributeClass;
            this.attributeChangerList = attributeChangerList;
        }

        public void writeToAllStates(ControlWrapper<?> controlWrapper)
        {
            ControlWrapperSetupUtil.writeSingleAttributeChangerToAllStates(controlWrapper, attributeClass, attributeChangerList, property);
        }
    }
}
