package parozzz.github.com.hmi;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public final class ChartTest
{
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final LineChart<Number, Number> lineChart;

    private final StackPane mainPane;
    private final AnchorPane rectPane;

    private final List<ChartRect> chartRectList;
    private ChartRect drawingChartRect;

    private Group plotContent;
    private Path verticalLines;
    private Path horizontalLines;
    private Region plotBackground;

    public ChartTest()
    {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();

        this.mainPane = new StackPane(
                rectPane = new AnchorPane(),
                lineChart = new MyLineChart(xAxis, yAxis, this)
        );

        this.chartRectList = new ArrayList<>();
    }

    public void test()
    {
        mainPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        mainPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane.setAlignment(rectPane, Pos.TOP_LEFT);
        rectPane.setPrefSize(500, 500);
        rectPane.setMinSize(300, 300);
        rectPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        rectPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        StackPane.setAlignment(lineChart, Pos.TOP_LEFT);
        lineChart.setPrefSize(500, 500);
        lineChart.setMinSize(300, 300);
        lineChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lineChart.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        //lineChart.setHorizontalGridLinesVisible(false);
        //lineChart.setVerticalGridLinesVisible(false);

        //lineChart.addEventFilter(MouseEvent.MOUSE_EXITED, this::handleInsideMouseOutside);

        var plotContent = lineChart.lookup(".plot-content");
        if(plotContent instanceof Group)
        {
            this.plotContent = (Group) plotContent;
        }

        var verticalLines = lineChart.lookup(".chart-vertical-grid-lines");
        var horizontalLines = lineChart.lookup(".chart-horizontal-grid-lines");
        if(verticalLines instanceof Path && horizontalLines instanceof Path)
        {
            this.verticalLines = (Path) verticalLines;
            this.horizontalLines = (Path) horizontalLines;
        }

        var plotBackground = lineChart.lookup(".chart-plot-background");
        if(plotBackground instanceof Region)
        {
            plotBackground.setCursor(Cursor.CROSSHAIR);

            plotBackground.setOnMouseMoved(mouseEvent ->
            {
                //This is to avoid having to handle too many mouse events around
                if(plotContent.contains(mouseEvent.getX(), mouseEvent.getY()))
                {
                }
            });
            plotBackground.setOnMouseExited(this::handleMouseExit);
            plotBackground.setOnMousePressed(this::handleMousePressed);
            plotBackground.setOnMouseDragged(this::handleMouseDragged);
            plotBackground.setOnMouseReleased(this::handleMouseReleased);

            this.plotBackground = (Region) plotBackground;
            this.plotBackground.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        }

        var series = new XYChart.Series<Number, Number>();
        series.setName("Random Data");
        series.getData().addAll(new XYChart.Data<>(3, 5),
                new XYChart.Data<>(5, 7),
                new XYChart.Data<>(6, 8),
                new XYChart.Data<>(4, 9),
                new XYChart.Data<>(7, 12),
                new XYChart.Data<>(4, 15),
                new XYChart.Data<>(8, 16));
        lineChart.getData().add(series);

        var stage = new Stage();

        var scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.show();

        this.startRectCreationMode();
    }

    private void startRectCreationMode()
    {
        plotContent.setMouseTransparent(true);
        verticalLines.setMouseTransparent(true);
        horizontalLines.setMouseTransparent(true);
    }

    private void recreateRects()
    {
        var children = rectPane.getChildren();
        children.clear();

        chartRectList.forEach(chartRect ->
        {
            chartRect.recreateRect();
            children.add(chartRect.getRectangle());
        });
    }

    private void handleMouseExit(MouseEvent mouseEvent)
    {
        if(drawingChartRect != null)
        {
            rectPane.getChildren().remove(drawingChartRect.getRectangle());
            drawingChartRect = null;
        }
    }

    private void handleMouseReleased(MouseEvent mouseEvent)
    {
        if(drawingChartRect != null)
        {
            drawingChartRect.complete(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            chartRectList.add(drawingChartRect);

            drawingChartRect = null;
        }
    }

    private void handleMouseDragged(MouseEvent mouseEvent)
    {
        if(drawingChartRect != null)
        {
            drawingChartRect.updateDrag(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        }
    }

    private void handleMousePressed(MouseEvent mouseEvent)
    {
        drawingChartRect = new ChartRect(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        rectPane.getChildren().add(0, drawingChartRect.getRectangle());
    }

    private class ChartRect
    {
        private final Point2D firstPoint;
        private Point2D secondPoint;

        private final double firstSceneX;
        private final double firstSceneY;

        private Rectangle rectangle;

        public ChartRect(double sceneX, double sceneY)
        {
            this.firstSceneX = sceneX;
            this.firstSceneY = sceneY;

            var backgroundBounds = plotBackground.localToScene(plotBackground.getBoundsInLocal());
            this.firstPoint = new Point2D(
                    xAxis.getValueForDisplay(sceneX - backgroundBounds.getMinX()).doubleValue(),
                    yAxis.getValueForDisplay(sceneY - backgroundBounds.getMinY()).doubleValue()
            );

            this.rectangle = new Rectangle(sceneX, sceneY, 0, 0);
            this.parseRectangle();
        }

        public void updateDrag(double sceneX, double sceneY)
        {
            var x = Math.min(sceneX, firstSceneX);
            var y = Math.min(sceneY, firstSceneY);

            var width = Math.abs(firstSceneX - sceneX);
            var height = Math.abs(firstSceneY - sceneY);

            rectangle.setX(x);
            rectangle.setY(y);

            rectangle.setWidth(width);
            rectangle.setHeight(height);
        }

        public void complete(double sceneX, double sceneY)
        {
            var backgroundBounds = plotBackground.localToScene(plotBackground.getBoundsInLocal());
            secondPoint = new Point2D(
                    xAxis.getValueForDisplay(sceneX - backgroundBounds.getMinX()).doubleValue(),
                    yAxis.getValueForDisplay(sceneY - backgroundBounds.getMinY()).doubleValue()
            );
        }

        public void recreateRect()
        {
            var backgroundBounds = plotBackground.localToScene(plotBackground.getBoundsInLocal());

            var xOffset = backgroundBounds.getMinX();
            var yOffset = backgroundBounds.getMinY();

            var x1 = xAxis.getDisplayPosition(firstPoint.getX()) + xOffset;
            var y1 = yAxis.getDisplayPosition(firstPoint.getY()) + yOffset;

            var x2 = xAxis.getDisplayPosition(secondPoint.getX()) + xOffset;
            var y2 = yAxis.getDisplayPosition(secondPoint.getY()) + yOffset;

            var sceneX = Math.min(x1, x2);
            var sceneY = Math.min(y1, y2);

            var width = Math.abs(x1 - x2);
            var height = Math.abs(y1 - y2);

            rectangle = new Rectangle(sceneX, sceneY, width, height);
            this.parseRectangle();
        }

        public Rectangle getRectangle()
        {
            return rectangle;
        }

        private void parseRectangle()
        {
            if(rectangle != null)
            {
                rectangle.setStroke(Color.BLACK);
                rectangle.setStrokeWidth(1);
                rectangle.setFill(Color.TRANSPARENT);
            }
        }
    }

    private static class MyLineChart extends LineChart<Number, Number>
    {
        private final ChartTest chartTest;

        public MyLineChart(Axis<Number> numberAxis, Axis<Number> numberAxis2, ChartTest chartTest)
        {
            super(numberAxis, numberAxis2);

            this.chartTest = chartTest;
        }

        @Override
        protected void layoutPlotChildren()
        {
            super.layoutPlotChildren();

            chartTest.recreateRects();
        }
    }
}
