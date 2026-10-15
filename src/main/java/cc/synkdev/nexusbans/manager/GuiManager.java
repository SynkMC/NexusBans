package cc.synkdev.nexusbans.manager;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.object.PluginGui;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class GuiManager {
    private GuiManager() {
        /* This utility class should not be instantiated */
    }

    public static void reloadGuis(NexusBans core) {
        try (ScanResult result = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("cc.synkdev")
                .addClassLoader(PluginGui.class.getClassLoader()).scan()) {
            List<Class<?>> list = result.getSubclasses(PluginGui.class.getName()).loadClasses();

            for (Class<?> clazz : list) {
                PluginGui type = (PluginGui) clazz.getDeclaredConstructor().newInstance();
                type.init(core);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
