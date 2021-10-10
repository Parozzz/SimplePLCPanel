package parozzz.github.com.simpleplcpanel.hmi.util.multipleobjects;

class MutableXY
{
    private double x;
    private double y;

    public MutableXY()
    {
        this(0,0);
    }
    public MutableXY(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public void setXY(double x, double y)
    {
        this.setX(x);
        this.setY(y);
    }
}
