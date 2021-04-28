package parozzz.github.com.simpleplcpanel.PLC.siemens.data.structure;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class SiemensS7TimerData
        extends SiemensS7ReadableData<SiemensS7TimerData.Timer>
{
    public SiemensS7TimerData()
    {
        super("TIMER", 16);
    }

    @Override
    public Timer readBuffer(byte[] buffer, int offset)
    {
        var timer = new Timer();
        timer.millisTimeSet = SiemensS7Util.getDWordAt(buffer, offset + 4);
        timer.millisTimePassed = SiemensS7Util.getDWordAt(buffer, offset + 8);

        timer.enabled = SiemensS7Util.getBitAt(buffer, offset + 12, 1);
        timer.timeReached = SiemensS7Util.getBitAt(buffer, offset + 12, 2);

        return timer;
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Timer;
    }

    @Override
    public String getAcronym()
    {
        return "T";
    }

    public static class Timer
    {
        private long millisTimeSet;
        private long millisTimePassed;
        private boolean enabled;
        private boolean timeReached;

        private Timer()
        {

        }

        public String toString()
        {
            var formattedTimeSet = new SimpleDateFormat("mm:ss:SSS").format(new Date(millisTimeSet));
            var formattedTimePassed = new SimpleDateFormat("mm:ss:SSS").format(new Date(millisTimePassed));

            return "Timer => " +
                    "TimeSet: " + formattedTimeSet + " min:sec:ms, " +
                    "TimePassed: " + formattedTimePassed + " min:sec:ms," +
                    "Enabled: " + enabled + ", " +
                    "TimeReached: " + timeReached;
        }
    }
}
