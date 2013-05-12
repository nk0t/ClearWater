package nk0t.mods.clearwater;

import java.util.*;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import cpw.mods.fml.relauncher.*;

public class EntityRendererTransformer implements IClassTransformer, Opcodes
{
    public static final String TargetClassName = "net.minecraft.client.renderer.EntityRenderer";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if (!transformedName.equals(TargetClassName) || !FMLRelauncher.side().equals("CLIENT"))
        {
            return bytes;
        }

        return hookEntityRenderer(bytes);
    }

    private byte[] hookEntityRenderer(byte[] bytes)
    {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        // setupFog を取得してくる
        String setupFogMethodName = "setupFog";
        MethodNode setupFogMethodNode = null;
        for (MethodNode mNode : classNode.methods)
        {
            if (setupFogMethodName.equals(mNode.name))
            {
                setupFogMethodNode = mNode;
                break;
            }
        }

        if (setupFogMethodNode != null)
        {
            ArrayList<MethodInsnNode> glFogfList = new ArrayList<MethodInsnNode>();

            // setupFogの処理を見ていき、 glFogf(GL11.GL_FOG_DENSITY, XX) を取得してくる
            for (AbstractInsnNode node : setupFogMethodNode.instructions.toArray())
            {
                if (node instanceof MethodInsnNode)
                {
                    if (methodInsnEqual((MethodInsnNode)node, "org/lwjgl/opengl/GL11", "glFogf"))
                    {
                        AbstractInsnNode par1 = node.getPrevious().getPrevious();
                        if (par1 instanceof IntInsnNode)
                        {
                            if (((IntInsnNode)par1).operand == GL11.GL_FOG_DENSITY)
                            {
                                glFogfList.add((MethodInsnNode)node);
                            }
                        }
                    }
                }
            }

            // コードを挿入していく
            for (MethodInsnNode node : glFogfList)
            {
                setupFogMethodNode.instructions.insert(node, getInsertionCode());
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        bytes = writer.toByteArray();

        return bytes;
    }

    public InsnList getInsertionCode()
    {
        // GL11.glFogf(GL11.GL_FOG_DENSITY, 0F);

        InsnList insnList = new InsnList();
        insnList.add(new IntInsnNode(Opcodes.SIPUSH, GL11.GL_FOG_DENSITY));
        insnList.add(new LdcInsnNode(0.01F));
        insnList.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/GL11", "glFogf", "(IF)V"));
        return insnList;
    }

    public boolean methodInsnEqual(MethodInsnNode methodInsnNode, String ownerClassName, String methodName)
    {
        return (methodInsnNode.owner.equals(ownerClassName) && methodInsnNode.name.equals(methodName));
    }
}
