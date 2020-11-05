package parozzz.github.com.logger;

import parozzz.github.com.util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MainLogger extends Thread
{
    private enum Month
    {
        JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
    }


    private final static MainLogger INSTANCE = new MainLogger();

    public static MainLogger getInstance()
    {
        return INSTANCE;
    }

    private final DateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

    private final File mainFolder = new File(System.getProperty("user.dir"), "logs");
    private final Logger logger = Logger.getAnonymousLogger();

    private final Queue<LogData> logDataQueue;
    private final Queue<String> messageQueue;

    private boolean stop;
    private boolean doNotLog;

    private MainLogger()
    {
        this.setName("LoggerThread");

        logDataQueue = new ConcurrentLinkedQueue<>();
        messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void setStop()
    {
        stop = true;
    }

    public synchronized Queue<String> getMessageQueue()
    {
        return messageQueue;
    }

    public void info(String message, Object involvedObject)
    {
        var finalMessage = this.parseMessage(message, involvedObject, true);

        doNotLog = true;
        logger.log(Level.INFO, finalMessage);
        doNotLog = false;

        this.appendLogData(Level.INFO, finalMessage, null);
    }

    public void warning(String message, Object involvedObject)
    {
        this.warning(message, null, involvedObject);
    }

    public void warning(String message, Exception exception, Object involvedObject)
    {
        this.log(Level.WARNING, message, exception, involvedObject);
    }

    public void error(String message, Exception exception, Object involvedObject)
    {
        this.log(Level.SEVERE, message, exception, involvedObject);
    }

    public void log(Level level, String message, Exception exception, Object involvedObject)
    {
        var finalMessage = this.parseMessage(message, involvedObject, false);

        doNotLog = true;
        logger.log(level, finalMessage, exception);
        doNotLog = false;

        this.appendLogData(level, finalMessage, exception);
    }

    public void appendLogData(Level level, String message, Exception exception)
    {
        var logData = new LogData(exception, level, message, System.currentTimeMillis());

        var stringDate = dateFormat.format(new Date(logData.timestamp));
        if(message == null)
        {
            message = "Generic error";
        }
        messageQueue.offer(message + '\n' + stringDate);

        logDataQueue.add(logData);
    }

    private String parseMessage(String message, Object involvedObject, boolean appendNewLine)
    {
        String finalMessage = "";
        if (involvedObject != null)
        {
            finalMessage = "InvolvedClass: " + involvedObject.getClass().getSimpleName() + '\n';
            if (involvedObject instanceof Loggable)
            {
                finalMessage += "Log: " + ((Loggable) involvedObject).log() + '\n';
            }
        }

        if (message != null && !message.isEmpty())
        {
            finalMessage += "Message: " + message;
        }

        return finalMessage + (appendNewLine ? '\n' : "");
    }

    @Override
    public void run()
    {
        System.setOut(new PrintStream(System.out)
        {
            @Override
            public void print(boolean b)
            {
                super.print(b);
                log(String.valueOf(b));
            }

            public void print(char c)
            {
                super.print(c);
                log(String.valueOf(c));
            }

            public void print(int i)
            {
                super.print(i);
                log(String.valueOf(i));
            }

            public void print(long l)
            {
                super.print(l);
                log(String.valueOf(l));
            }

            public void print(float f)
            {
                super.print(f);
                log(String.valueOf(f));
            }

            public void print(double d)
            {
                super.print(d);
                log(String.valueOf(d));
            }

            public void print(char[] s)
            {
                super.print(s);
                log(String.valueOf(s));
            }

            public void print(String s)
            {
                super.print(s);
                log(String.valueOf(s));
            }

            public void print(Object obj)
            {
                super.print(obj);
                log(Objects.toString(obj));
            }

            private void log(String string)
            {
                if (!doNotLog)
                {
                    logDataQueue.add(new LogData(null, null, string, System.currentTimeMillis()));
                }
            }
        });

        while (true)
        {
            if(stop)
            {
                return;
            }

            try
            {
                if (logDataQueue.isEmpty())
                {
                    try
                    {
                        Thread.sleep(500); //Why waste so much time doing useless stuff?
                    } catch (InterruptedException interruptedException)
                    {
                        interruptedException.printStackTrace();
                    }

                    Thread.onSpinWait();
                    continue;
                }

                if (!mainFolder.exists())
                {
                    mainFolder.mkdirs();
                }

                File file = null;
                FileWriter fileWriter = null;

                var logData = logDataQueue.poll();
                while (logData != null)
                {
                    var date = new Date(logData.timestamp);

                    var localFile = this.getFile(date);
                    if (file == null || !file.equals(localFile))
                    {
                        if(fileWriter != null)
                        {
                            fileWriter.flush();
                            fileWriter.close();
                        }

                        fileWriter = new FileWriter(localFile, true);
                        file = localFile;
                    }

                    fileWriter.append("[").append(dateFormat.format(date)).append("]");

                    if (logData.level != null)
                    {
                        if (logData.message != null)
                        {
                            fileWriter.append('\n'); //Append a new line only if they are both present
                        }

                        fileWriter.append("Level: ").append(logData.level.getName()).append('\n');
                    }

                    if (logData.message != null)
                    {
                        fileWriter.append(logData.message).append('\n');
                    }

                    if (logData.exception != null)
                    {
                        for (var stackTraceElement : logData.exception.getStackTrace())
                        {
                            fileWriter.append(stackTraceElement.toString()).append('\n');
                        }

                        fileWriter.append('\n');
                    }

                    logData = logDataQueue.poll();
                }

                if(fileWriter != null)
                {
                    fileWriter.flush();
                    fileWriter.close();
                }

                /*
                var logData = logDataQueue.poll();
                if (logData == null)
                {
                    return;
                }

                var date = new Date(logData.timestamp);

                var fileWriter = new FileWriter(this.getFile(date), true);
                fileWriter.append("[").append(dateFormat.format(date)).append("]");

                if (logData.level != null && logData.message != null)
                { //Append a new line only if they are both present
                    fileWriter.append('\n');
                }

                if (logData.level != null)
                {
                    fileWriter.append("Level: ").append(logData.level.getName())
                            .append('\n');
                }

                if (logData.message != null)
                {
                    fileWriter.append(logData.message)
                            .append('\n');
                }

                if (logData.exception != null)
                {
                    for (var stackTraceElement : logData.exception.getStackTrace())
                    {
                        fileWriter.append(stackTraceElement.toString()).append('\n');
                    }

                    fileWriter.append('\n');
                }

                fileWriter.flush();
                fileWriter.close();*/
            } catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    private File getFile(Date date) throws IOException
    {
        var calendar = Calendar.getInstance();
        calendar.setTime(date);

        var month = Month.values()[calendar.get(Calendar.MONTH)];
        var day = calendar.get(Calendar.DAY_OF_MONTH);
        var hour = calendar.get(Calendar.HOUR_OF_DAY);

        var monthFolder = new File(mainFolder, Util.capitalize(month.name()));
        if (!monthFolder.exists())
        {
            monthFolder.mkdirs();
        }

        var dayFolder = new File(monthFolder, "" + day);
        if (!dayFolder.exists())
        {
            dayFolder.mkdirs();
        }

        var hourFile = new File(dayFolder, hour + ".log");
        if (!hourFile.exists())
        {
            hourFile.createNewFile();
        }

        return hourFile;
    }

    private static class LogData
    {
        private final Exception exception;
        private final Level level;
        private final String message;
        private final long timestamp;

        private LogData(Exception exception, Level level, String message, long timestamp)
        {
            this.exception = exception;
            this.level = level;
            this.message = message;
            this.timestamp = timestamp;
        }

    }
}
