package parozzz.github.com.simpleplcpanel.PLC.siemens.util;

public enum SiemensS7Status
{
    /*
        public static final int S7CpuStatusUnknown = 0x00;
    public static final int S7CpuStatusRun     = 0x08;
    public static final int S7CpuStatusStop    = 0x04;
     */
    RUN,
    STOP,
    UNKNOWN;
    
    public static SiemensS7Status getFromID(int id)
    {
        switch(id)
        {
            case 0x08:
                return RUN;
            case 0x03: //This depends on the PLC model. Older might have 3 newer 4.
            case 0x04:
                return STOP;
            case 0x00:
            default:
                return UNKNOWN;
        }
    }
}
