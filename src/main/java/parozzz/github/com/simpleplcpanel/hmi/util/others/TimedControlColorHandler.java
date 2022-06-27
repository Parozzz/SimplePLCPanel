package parozzz.github.com.simpleplcpanel.hmi.util.others;

import javafx.scene.control.Control;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

public final class TimedControlColorHandler
{
    private final Control control;
    private final Background initialBackground;
    private final Border initialBorder;
    public TimedControlColorHandler(Control control)
    {
        this.control = control;
        this.initialBackground = control.getBackground();
        this.initialBorder = control.getBorder();
    }

    public void setBackground(Background background, int resetTime)
    {
        control.setBackground(background);
        FXUtil.runLater(resetTime, () -> control.setBackground(initialBackground));
    }

    public void setBorder(Border border, int resetTime)
    {
        control.setBorder(border);
        FXUtil.runLater(resetTime, () -> control.setBorder(initialBorder));
    }

    public void setBorderAndBackground(Background background, Border border, int resetTime)
    {
        control.setBackground(background);
        control.setBorder(border);
        FXUtil.runLater(resetTime, () ->
        {
            control.setBackground(initialBackground);
            control.setBorder(initialBorder);
        });
    }
}
