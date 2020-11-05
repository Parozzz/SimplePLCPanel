package parozzz.github.com.hmi.controls.controlwrapper.setup;

import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import parozzz.github.com.logger.MainLogger;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class SetupInjector
{
    private final Class<?> managerClass;
    private final Object managerInstance;
    private final Map<String, Object> objectMap;

    public <T> SetupInjector(Class<T> managerClass, T managerInstance)
    {
        this.managerClass = managerClass;
        this.managerInstance = managerInstance;

        objectMap = new HashMap<>();
    }

    public SetupInjector addAll(Parent parent)
    {
        parent.getChildrenUnmodifiable().forEach(node ->
        {
            var id = node.getId();
            if (id == null || id.isEmpty())
            {
                return;
            }

            objectMap.put(id, node);
        });

        return this;
    }

    public SetupInjector addAllInternalMenuButton(String id)
    {
        return this.addAll(this.get(id, MenuButton.class));
    }

    public SetupInjector addAll(MenuButton menuButton)
    {
        menuButton.getItems().forEach(menuItem ->
        {
            var id = menuItem.getId();
            if (id == null || id.isEmpty())
            {
                return;
            }

            objectMap.put(id, menuItem);
        });

        return this;
    }

    public <T> T get(String id, Class<T> objectClass)
    {
        var object = objectMap.get(id);
        if (!objectClass.isInstance(object))
        {
            throw new NullPointerException("Invalid object for id " + id + " and class " + objectClass.getName());
        }

        return objectClass.cast(object);
    }

    public void inject()
    {
        try
        {
            var fields = managerClass.getDeclaredFields();
            for (var field : fields)
            {
                if (Modifier.isFinal(field.getModifiers()))
                {
                    continue;
                }

                var managerObject = objectMap.get(field.getName());
                if (managerObject == null)
                {
                    continue;
                }

                field.trySetAccessible();
                if (field.getType().isInstance(managerObject))
                {
                    field.set(managerInstance, managerObject);
                }
            }
        } catch (Exception exception)
        {
            MainLogger.getInstance().error("", exception, this);
        }
    }
}
