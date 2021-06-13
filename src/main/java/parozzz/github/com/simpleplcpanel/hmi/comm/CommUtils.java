package parozzz.github.com.simpleplcpanel.hmi.comm;

import javafx.scene.control.TextField;
import parozzz.github.com.simpleplcpanel.util.Util;

public final class CommUtils
{
    public final static String DEFAULT_IP1_STRING = "192";
    public final static String DEFAULT_IP2_STRING = "168";
    public final static String DEFAULT_IP3_STRING = "0";
    public final static String DEFAULT_IP4_STRING = "5";

    public final static String DEFAULT_SIEMENS_RACK_STRING = "0";
    public final static String DEFAULT_SIEMENS_SLOT_STRING = "0";

    public final static String DEFAULT_MODBUSTCP_PORT_STRING = "502";

    public static String validateAndCreateIpAddress(TextField t1,
            TextField t2, TextField t3, TextField t4)
    {
        var ip1 = Util.parseInt(t1.getText(), -1);
        var ip2 = Util.parseInt(t2.getText(), -1);
        var ip3 = Util.parseInt(t3.getText(), -1);
        var ip4 = Util.parseInt(t4.getText(), -1);
        if (ip1 < 0 || ip2 < 0 || ip3 < 0 || ip4 < 0)
        {
            return null;
        }

        return String.format("%d.%d.%d.%d", ip1, ip2, ip3, ip4);
    }

    private CommUtils() {}
}
