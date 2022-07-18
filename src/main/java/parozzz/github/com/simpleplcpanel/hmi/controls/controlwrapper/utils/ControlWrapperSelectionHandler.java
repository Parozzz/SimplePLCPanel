package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.util.BooleanChangeType;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.util.stream.Stream;

public final class ControlWrapperSelectionHandler extends FXObject
{
    private final ControlWrapper<?> controlWrapper;

    private EventHandler<MouseEvent> mousePressedEventHandler;
    private EventHandler<MouseEvent> mouseReleasedEventHandler;
    private boolean hasEventFilters = false;

    private final BooleanProperty selectedProperty;
    private final BooleanProperty mainSelectionProperty;

    private boolean mousePressedValid = false;

    public ControlWrapperSelectionHandler(ControlWrapper<?> controlWrapper)
    {
        this.controlWrapper = controlWrapper;

        this.selectedProperty = new SimpleBooleanProperty();
        this.mainSelectionProperty = new SimpleBooleanProperty();
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        var containerStackPane = controlWrapper.getContainerPane();

        containerStackPane.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler = mouseEvent ->
        {
            var contextMenuController = controlWrapper.getContextMenuController();
            var selectionManager = controlWrapper.getControlMainPage().getSelectionManager();

            mousePressedValid = false;
            if(mouseEvent.getButton() == MouseButton.PRIMARY)
            {
                //If the context menu is been shown, i close it and ignore this click. I feel like gives a better UX.
                if(contextMenuController.isShowing())
                {
                    contextMenuController.hide();
                    return;
                }

                if (!mouseEvent.isControlDown() && selectionManager.isEmpty())
                {
                    //In this system, if i have control down and there is no selected
                    //the first will be added and deleted right away. So this is required.
                    selectionManager.set(controlWrapper);
                }
            }
            else
            {
                //This is here so, when there is nothing selected, and you request a context menu the clicked item is selected
                //and the menu is opened.
                if(selectionManager.isEmpty())
                {
                    selectionManager.set(controlWrapper);
                }
            }

            mousePressedValid = true;
        });

        containerStackPane.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler = mouseEvent ->
        {
            if (mouseEvent.getButton() != MouseButton.PRIMARY
                    || !mousePressedValid
                    || controlWrapper.wasLastPressDrag()
                    || controlWrapper.wasLastPressResize())
            {
                return;
            }

            var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
            if (mouseEvent.isControlDown()) //Multiple selection holding ctrl
            {
                if (selectedProperty.get())
                {
                    multipleSelectionManager.remove(controlWrapper);
                } else
                {
                    multipleSelectionManager.add(controlWrapper);
                }
            } else
            {
                multipleSelectionManager.set(controlWrapper);
            }
        });

        Stream.of(containerStackPane.widthProperty(), containerStackPane.heightProperty()).forEach(property ->
                property.addListener((observableValue, oldValue, newValue) ->
                { //This update the border (for the corner and center pieces) every time is resized.
                    if (selectedProperty.get())
                    {
                        ControlWrapperBorderCreator.applySelectedBorder(controlWrapper);
                    }
                })
        );

        selectedProperty.addListener((observable, oldValue, newValue) ->
        {
            if (!controlWrapper.isReadOnly())
            {
                switch (Util.checkChangeType(newValue, oldValue))
                {
                    case FALLING:
                        ControlWrapperBorderCreator.applyDashedBorder(controlWrapper);
                        break;
                    case RISING:
                        ControlWrapperBorderCreator.applySelectedBorder(controlWrapper);
                        break;
                }
            }
        });

        mainSelectionProperty.addListener((observable, oldValue, newValue) ->
        {
            if (!controlWrapper.isReadOnly() && Util.checkChangeType(newValue, oldValue) == BooleanChangeType.RISING)
            {
                ControlWrapperBorderCreator.applySelectedBorder(controlWrapper);
            }
        });

        hasEventFilters = true;
    }

    public void setSelected(boolean selected)
    {
        this.selectedProperty.setValue(selected);
        if (!selected)
        {
            this.mainSelectionProperty.set(false);
        }
    }

    public boolean isSelected()
    {
        return selectedProperty.get();
    }

    public void setMainSelection(boolean mainSelection)
    {
        this.mainSelectionProperty.setValue(mainSelection);
    }

    public boolean isMainSelection()
    {
        return mainSelectionProperty.get();
    }

    public void addEventFilters()
    {
        if(!hasEventFilters)
        {
            hasEventFilters = true;

            var containerStackPane = controlWrapper.getContainerPane();
            containerStackPane.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
            containerStackPane.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
        }
    }

    public void removeEventFilters()
    {
        if(hasEventFilters)
        {
            hasEventFilters = false;

            var containerStackPane = controlWrapper.getContainerPane();
            containerStackPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
            containerStackPane.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
        }
    }
}
