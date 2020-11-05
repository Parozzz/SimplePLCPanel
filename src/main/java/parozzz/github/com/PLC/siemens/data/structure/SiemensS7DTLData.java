package parozzz.github.com.PLC.siemens.data.structure;

import parozzz.github.com.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

import java.util.Calendar;

public final class SiemensS7DTLData
        extends SiemensS7Data<SiemensS7DTLData.DTL>
{
    
    public SiemensS7DTLData()
    {
        super("DTL", 12);
    }
    
    @Override
    public void writeBuffer(byte[] buffer, int offset, DTL value)
    {
        SiemensS7Util.setWordAt(buffer, offset, value.year);
        buffer[offset + 2] = (byte) (value.month + 1);
        buffer[offset + 3] = value.day;
        buffer[offset + 4] = value.weekday;
        buffer[offset + 5] = value.hour;
        buffer[offset + 6] = value.minute;
        buffer[offset + 7] = value.second;
        SiemensS7Util.setDWordAt(buffer, offset + 8, value.nanosecond);
    }

    @Override
    public String getAcronym()
    {
        return "DTL";
    }

    @Override
    public DTL readBuffer(byte[] buffer, int offset)
    {
        var dtl = new DTL();
        dtl.year = SiemensS7Util.getWordAt(buffer, offset);
        dtl.month = (byte) ( buffer[offset + 2] - 1);
        dtl.day = buffer[offset + 3];
        dtl.weekday = buffer[offset + 4];
        dtl.hour = buffer[offset + 5];
        dtl.minute = buffer[offset + 6];
        dtl.second = buffer[offset + 7];
        dtl.nanosecond = SiemensS7Util.getDWordAt(buffer, offset + 8);
        
        return dtl;
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof DTL;
    }

    public static class DTL
    {
        private int year;
        private byte month;
        private byte day;
        private byte weekday;
        private byte hour;
        private byte minute;
        private byte second;
        private long nanosecond = 0;
        
        public static DTL ofCurrentCalendar()
        {
            return new DTL(Calendar.getInstance());
        }
        
        public DTL()
        {
        
        }
        
        public DTL(Calendar calendar)
        {
            year = calendar.get(Calendar.YEAR);
            month = (byte) calendar.get(Calendar.MONTH);
            day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
            weekday = (byte) calendar.get(Calendar.DAY_OF_WEEK);
            hour = (byte) calendar.get(Calendar.HOUR);
            minute = (byte) calendar.get(Calendar.MINUTE);
            second = (byte) calendar.get(Calendar.SECOND);
        }
        
        public String toString()
        {
            return "DTL => " +
                    "Year: " + year + ", " +
                    "Month: " + month + ", " +
                    "Day: " + day + ", " +
                    "WeekDay: " + weekday + ", " +
                    "Hour: " + hour + ", " +
                    "Minute: " + minute + ", " +
                    "Second: " + second + ", " +
                    "NanoSecond: " + nanosecond;
        }
    }
}
