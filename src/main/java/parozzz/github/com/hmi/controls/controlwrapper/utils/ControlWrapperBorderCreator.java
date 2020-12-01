package parozzz.github.com.hmi.controls.controlwrapper.utils;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;

import java.util.ArrayList;
import java.util.List;

public final class ControlWrapperBorderCreator
{
    public static void applyDashedBorder(ControlWrapper<?> controlWrapper)
    {
        var borderStrokeStyle = new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.SQUARE,
                10, 0, List.of(10d, 10d));

        var widths = new BorderWidths(3);
        var border = new Border(new BorderStroke(Color.GRAY, borderStrokeStyle, CornerRadii.EMPTY, widths));
        controlWrapper.getContainerPane().setBorder(border);
    }

    public static void applySelectedBorder(ControlWrapper<?> controlWrapper)
    {
        var containerPane = controlWrapper.getContainerPane();
        //This took a WHOLE (Nope 2) day to find a compromise. Fuck me and my arse.
        var rectWidth = containerPane.getWidth();
        var rectHeight = containerPane.getHeight();

        var dashMultiplier = Math.min(rectWidth, rectHeight);

        var dashWidth = 3d;
        var dashLength = 4.0d + (dashMultiplier / 8d);

        //Adjust the width and the height based on the dash values
        rectWidth += dashWidth * 2;
        rectHeight += dashWidth * 2;

        var offset = dashLength / 2.0d;

        var dashList = new ArrayList<Double>();
        for(int x = 0; x < 2; x++)
        {
            dashList.add(dashLength);

            var dashEmptyDistance = rectHeight / 2.0 - (dashWidth / 2d + dashLength);
            dashList.add(Math.max(0, dashEmptyDistance));
        }

        for(int x = 0; x < 2; x++)
        {
            dashList.add(dashLength);

            var dashEmptyDistance = rectWidth / 2.0 - (dashWidth / 2d + dashLength);
            dashList.add(Math.max(0, dashEmptyDistance));
        }

        var borderStrokeStyle = new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER,
                StrokeLineCap.SQUARE, 10, offset, dashList);

        var color = controlWrapper.isMainSelection() ? Color.DARKORANGE : Color.BLACK;

        var stroke = new BorderStroke(color, borderStrokeStyle, CornerRadii.EMPTY, new BorderWidths(dashWidth));
        var border = new Border(stroke);
        containerPane.setBorder(border);
    }

    private ControlWrapperBorderCreator() {}
}
