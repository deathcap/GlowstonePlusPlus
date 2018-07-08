package net.glowstone.util.linkstone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.aki.linkstone.runtime.LinkstoneRuntimeData;
import me.aki.linkstone.runtime.boxing.BoxPatchVisitor;
import me.aki.linkstone.runtime.direct.DirectFieldAccessReplaceVisitor;
import me.aki.linkstone.runtime.inithook.ClassInitInvokeVisitor;
import me.aki.linkstone.runtime.reflectionredirect.FieldRedirectUtil;
import me.aki.linkstone.runtime.reflectionreplace.ReflectFieldAccessReplaceVisitor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class LinkstonePluginLoader extends JavaPluginLoader {
    /**
     * Bukkit will invoke this constructor via reflection.
     * Its signature should therefore not be changed!
     *
     * @param instance the server instance
     */
    public LinkstonePluginLoader(Server instance) {
        super(instance);
    }

    @Override
    protected PluginClassLoader newPluginLoader(JavaPluginLoader loader, ClassLoader parent,
            PluginDescriptionFile description, File dataFolder, File file) throws Exception {

        return new PluginClassLoader(loader, parent, description, dataFolder, file) {
            @Override
            protected byte[] transformBytecode(byte[] bytecode) {
                ClassWriter cw = new ClassWriter(0);

                ClassVisitor cv = cw;
                cv = new DirectFieldAccessReplaceVisitor(LinkstoneRuntimeData.getFields(), cv);
                if (!FieldRedirectUtil.isSupported()) {
                    cv = new ReflectFieldAccessReplaceVisitor(cv);
                }
                cv = new ClassInitInvokeVisitor(cv);
                cv = new BoxPatchVisitor(LinkstoneRuntimeData.getBoxes(), cv);

                new ClassReader(bytecode).accept(cv, 0);
                return cw.toByteArray();
            }
        };
    }
}
