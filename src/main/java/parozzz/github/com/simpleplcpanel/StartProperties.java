package parozzz.github.com.simpleplcpanel;

import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

public final class StartProperties
{
    public enum PropertyEnum
    {
        SHOW_EMBEDDED_KEYBOARD("ShowEmbeddedKeyboard", false),
        MODBUS_DEBUG("ModbusDebug", false);

        private final String key;
        private final Object defaultValue;
        PropertyEnum(String key, Object defaultValue)
        {
            this.key = key;
            this.defaultValue = defaultValue;
        }
    }

    private final Properties properties;
    public StartProperties()
    {
        properties = new Properties();
    }

    public void setup()
    {
        var file = new File(System.getProperty("user.dir"), "start.properties");
        if(!file.exists())
        {
            try
            {
                file.createNewFile();

                Stream.of(PropertyEnum.values()).forEach(propertyEnum -> properties.put(propertyEnum.key, propertyEnum.defaultValue.toString()));

                var fileOutputStream = new FileOutputStream(file);
                properties.store(fileOutputStream, "");
                fileOutputStream.close();

            } catch (IOException exception)
            {
                MainLogger.getInstance().error("Error while creating properties file", exception, this);
            }
        }
        else
        {
            try(var fileInputStream = new FileInputStream(file))
            {
                properties.load(fileInputStream);
            } catch (IOException exception)
            {
                MainLogger.getInstance().error("Error while loading properties file", exception, this);
            }


            boolean anyAdded = false;
            for(var property : PropertyEnum.values())
            {
                if(!properties.containsKey(property.key))
                {
                    properties.setProperty(property.key, property.defaultValue.toString());
                    anyAdded = true;
                }
            }

            if(anyAdded)
            {
                try(var fileOutputStream = new FileOutputStream(file))
                {
                    properties.store(fileOutputStream, null);
                } catch (IOException exception)
                {
                    MainLogger.getInstance().error("Error while saving file for missing properties", exception, this);
                }
            }
        }
    }

    public void applyGeneralProperties()
    {
        if (this.getBoolean(StartProperties.PropertyEnum.SHOW_EMBEDDED_KEYBOARD))
        {
            //It needs to be done here before launching JavaFX applet otherwise it won't work
            System.setProperty("com.sun.javafx.touch", "true");
            System.setProperty("com.sun.javafx.isEmbedded", "true");
            System.setProperty("com.sun.javafx.virtualKeyboard", "javafx");
        }

        if (this.getBoolean(StartProperties.PropertyEnum.MODBUS_DEBUG))
        {
            System.setProperty("net.wimpi.modbus.debug", "true");
        }
    }

    public boolean getBoolean(PropertyEnum propertyEnum)
    {
        var stringProperty = properties.getProperty(propertyEnum.key);
        return Boolean.parseBoolean(Objects.requireNonNullElse(stringProperty, propertyEnum.defaultValue.toString()));
    }
}
