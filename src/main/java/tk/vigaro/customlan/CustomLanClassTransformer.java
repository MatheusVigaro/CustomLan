package tk.vigaro.customlan;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

public class CustomLanClassTransformer implements IClassTransformer {

    private static final String[] classes = {"net.minecraft.server.integrated.IntegratedServer", "net.minecraft.client.gui.GuiIngameMenu"};

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        boolean isObf = !name.equals(transformedName);
        int index = Arrays.asList(classes).indexOf(transformedName);
        return index != -1 ? transform(index, basicClass, isObf) : basicClass;
    }

    private byte[] transform(int index, byte[] basicClass, boolean isObf) {
        System.out.println("Transforming: " + classes[index]);
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            switch (index) {
                case 0:
                    transformIntegratedServer(classNode, isObf);
                    break;
                case 1:
                    transformGuiIngameMenu(classNode, isObf);
                    break;
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return basicClass;
    }

    private static void transformIntegratedServer(ClassNode integratedServerClass, boolean isObf) {
        final String SHARE_TO_LAN = isObf ? "a" : "shareToLAN";
        final String SHARE_TO_LAN_DESCRIPTOR = isObf ? "(Laib;Z)Ljava/lang/String;" : "(Lnet/minecraft/world/GameType;Z)Ljava/lang/String;";

        final String HTTP_UTIL = isObf ? "oh" : "net/minecraft/util/HttpUtil";
        final String GET_SUITABLE_LAN_PORT = isObf ? "a" : "getSuitableLanPort";

        for (MethodNode methodNode : integratedServerClass.methods) {
            if (methodNode.name.equals(SHARE_TO_LAN) && methodNode.desc.equals(SHARE_TO_LAN_DESCRIPTOR)) {
                MethodInsnNode getLanPort = null;
                for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                    if (instruction.getOpcode() == Opcodes.INVOKESTATIC) {
                        if (((MethodInsnNode) instruction).owner.equals(HTTP_UTIL) && ((MethodInsnNode) instruction).name.equals(GET_SUITABLE_LAN_PORT)) {
                            getLanPort = (MethodInsnNode) instruction;
                            break;
                        }
                    }
                }
                if (getLanPort != null) {
                    methodNode.instructions.set(getLanPort, new MethodInsnNode(Opcodes.INVOKESTATIC, "tk/vigaro/customlan/CustomLanModContainer", "getLanPort", "()I", false));
                } else {
                    System.out.println("ERROR, ERROR, ERROR!!!!!!!!");
                }
            }

        }

    }

    private void transformGuiIngameMenu(ClassNode guiIngameMenuClass, boolean isObf) {
        final String ACTION_PERFORMED = isObf ? "a" : "actionPerformed";
        final String ACTION_PERFORMED_DESCRIPTOR = isObf ? "(Lbdr;)V" : "(Lnet/minecraft/client/gui/GuiButton;)V";

        final String GUI_SHARE_TO_LAN = isObf ? "bfu" : "net/minecraft/client/gui/GuiShareToLan";
        final String GUI_SHARE_TO_LAN_DESCRIPTOR = isObf ? "(Lbtf;)V" : "(Lnet/minecraft/client/gui/GuiScreen;)V";

        int i = 0;

        for (MethodNode methodNode : guiIngameMenuClass.methods) {
            if (methodNode.name.equals(ACTION_PERFORMED) && methodNode.desc.equals(ACTION_PERFORMED_DESCRIPTOR)) {
                TypeInsnNode newShareToLan = null;
                MethodInsnNode invokeShareToLan = null;
                for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                    if (instruction.getOpcode() == Opcodes.NEW) {
                        if (((TypeInsnNode) instruction).desc.equals(GUI_SHARE_TO_LAN) && ((TypeInsnNode) instruction).getNext().getOpcode() == Opcodes.DUP) {
                            newShareToLan = (TypeInsnNode) instruction;
                            i++;
                        }
                    } else if (instruction.getOpcode() == Opcodes.INVOKESPECIAL) {
                        if (((MethodInsnNode) instruction).owner.equals(GUI_SHARE_TO_LAN) && ((MethodInsnNode) instruction).desc.equals(GUI_SHARE_TO_LAN_DESCRIPTOR)) {
                            invokeShareToLan = (MethodInsnNode) instruction;
                            i++;
                        }
                    }
                    if (i == 2) break;
                }

                if (newShareToLan != null && invokeShareToLan != null) {
                    methodNode.instructions.set(newShareToLan, new TypeInsnNode(Opcodes.NEW, "tk/vigaro/customlan/GuiCustomShareToLan"));
                    methodNode.instructions.set(invokeShareToLan, new MethodInsnNode(Opcodes.INVOKESPECIAL, "tk/vigaro/customlan/GuiCustomShareToLan", "<init>", GUI_SHARE_TO_LAN_DESCRIPTOR, false));
                } else {
                    System.out.println("ERROR, ERROR, ERROR!!!!!!!!");
                }

            }
        }
    }
}
