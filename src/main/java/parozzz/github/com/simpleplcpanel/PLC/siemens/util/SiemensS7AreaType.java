package parozzz.github.com.simpleplcpanel.PLC.siemens.util;

public enum SiemensS7AreaType
{
    /*
        S7AreaCT = 0x1C; //Not available for s7 1200 / 1500
        S7AreaTM = 0x1D;    //Not available for s7 1200 / 1500
     */
    INPUT(0x81, "I"),
    OUTPUT(0x82, "Q"),
    MERKER(0x83, "M"),
    DB(0x84, "DB");
    
    public static SiemensS7AreaType getFromID(int id)
    {
        switch(id)
        {
            case 0x81:
                return INPUT;
            case 0x82:
                return OUTPUT;
            case 0x83:
                return MERKER;
            case 0x84:
                return DB;
            default:
                return null;
        }
    }
    
    private final int id;
    private final String acronym;
    SiemensS7AreaType(int id, String acronym)
    {
        this.id = id;
        this.acronym = acronym;
    }

    public String getAcronym()
    {
        return acronym;
    }

    public byte getId()
    {
        return (byte) id;
    }
}
