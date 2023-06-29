package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.utils;

import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;

public final class ControlWrapperPositionHandler extends FXObject
{
    private final ControlWrapper<?> controlWrapper;

    public ControlWrapperPositionHandler(ControlWrapper<?> controlWrapper)
    {
        this.controlWrapper = controlWrapper;
    }

    @Override
    public void onSetup()
    {
        super.onSetup();
    }

    public void alignLeft()
    {
        var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
        var mainSelectionControlWrapper = multipleSelectionManager.getMainSelection();
        if (multipleSelectionManager.size() < 2 || mainSelectionControlWrapper == null)
        {
            return;
        }

        //This is ALWAYS done based on the main selection
        var layoutX = multipleSelectionManager.getMainSelection().getLayoutX();
        multipleSelectionManager.forEachIgnoring(mainSelectionControlWrapper, selectedControlWrapper ->
                selectedControlWrapper.getContainerPane().setLayoutX(layoutX)
        );
    }

    public void alignRight()
    {
        var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
        var mainSelectionControlWrapper = multipleSelectionManager.getMainSelection();
        if (multipleSelectionManager.size() < 2 || mainSelectionControlWrapper == null)
        {
            return;
        }

        var rightX = mainSelectionControlWrapper.getLayoutX() + mainSelectionControlWrapper.getContainerPane().getWidth();
        multipleSelectionManager.forEachIgnoring(mainSelectionControlWrapper, selectedControlWrapper ->
        {
            var layoutX = rightX - selectedControlWrapper.getContainerPane().getWidth();
            selectedControlWrapper.getContainerPane().setLayoutX(layoutX);
        });
    }

    public void alignTop()
    {
        var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
        var mainSelectionControlWrapper = multipleSelectionManager.getMainSelection();
        if (multipleSelectionManager.size() < 2 || mainSelectionControlWrapper == null)
        {
            return;
        }

        //This is ALWAYS done based on the main selection
        var layoutY = multipleSelectionManager.getMainSelection().getLayoutY();
        multipleSelectionManager.forEachIgnoring(mainSelectionControlWrapper, selectedControlWrapper ->
                selectedControlWrapper.getContainerPane().setLayoutY(layoutY)
        );
    }

    public void alignBottom()
    {
        var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
        var mainSelectionControlWrapper = multipleSelectionManager.getMainSelection();
        if (multipleSelectionManager.size() < 2 || mainSelectionControlWrapper == null)
        {
            return;
        }

        var bottomY = mainSelectionControlWrapper.getLayoutY() + mainSelectionControlWrapper.getContainerPane().getHeight();
        multipleSelectionManager.forEachIgnoring(mainSelectionControlWrapper, selectedControlWrapper ->
        {
            var layoutY = bottomY - selectedControlWrapper.getContainerPane().getHeight();
            selectedControlWrapper.getContainerPane().setLayoutY(layoutY);
        });
    }

    public void alignVerticalCenter()
    {
        var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
        var mainSelectionControlWrapper = multipleSelectionManager.getMainSelection();
        if (multipleSelectionManager.size() < 2 || mainSelectionControlWrapper == null)
        {
            return;
        }

        var containerPane = mainSelectionControlWrapper.getContainerPane();
        var centerX = containerPane.getLayoutX() + (containerPane.getWidth() / 2);
        multipleSelectionManager.forEachIgnoring(mainSelectionControlWrapper, selectedControlWrapper ->
        {
            var centeredLayoutX = centerX - (selectedControlWrapper.getContainerPane().getWidth() / 2);
            selectedControlWrapper.getContainerPane().setLayoutX(centeredLayoutX);
        });
    }

    public void alignHorizontalCenter()
    {
        var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
        var mainSelectionControlWrapper = multipleSelectionManager.getMainSelection();
        if (multipleSelectionManager.size() < 2 || mainSelectionControlWrapper == null)
        {
            return;
        }

        var containerPane = mainSelectionControlWrapper.getContainerPane();
        var centerY = containerPane.getLayoutY() + (containerPane.getHeight() / 2);
        multipleSelectionManager.forEachIgnoring(mainSelectionControlWrapper, selectedControlWrapper ->
        {
            var centeredLayoutY = centerY - (selectedControlWrapper.getContainerPane().getHeight() / 2);
            selectedControlWrapper.getContainerPane().setLayoutY(centeredLayoutY);
        });
    }

    public void centerX(boolean executeOnAllSelections)
    {
        var containerStackPane = controlWrapper.getContainerPane();

        var newLayoutX = (int) Math.floor(
                (controlWrapper.getControlMainPage().getMainAnchorPane().getWidth() / 2) - (containerStackPane.getWidth() / 2)
        );
        containerStackPane.setLayoutX(newLayoutX);

        if (executeOnAllSelections)
        {
            var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
            multipleSelectionManager.forEachIgnoring(controlWrapper, selectedControlWrapper ->
                    selectedControlWrapper.getPositionHandler().centerX(false)
            );
        }
    }

    public void centerY(boolean executeOnAllSelections)
    {
        var containerStackPane = controlWrapper.getContainerPane();
        var newLayoutY = (int) Math.floor(
                (controlWrapper.getControlMainPage().getMainAnchorPane().getHeight() / 2) - (containerStackPane.getHeight() / 2)
        );
        containerStackPane.setLayoutY(newLayoutY);

        if (executeOnAllSelections)
        {
            var multipleSelectionManager = controlWrapper.getControlMainPage().getSelectionManager();
            multipleSelectionManager.forEachIgnoring(controlWrapper, selectedControlWrapper ->
                    selectedControlWrapper.getPositionHandler().centerY(false)
            );
        }
    }
}
