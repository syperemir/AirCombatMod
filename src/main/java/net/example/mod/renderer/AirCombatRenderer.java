package net.example.mod.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class AirCombatRenderer {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void renderESP(WorldRenderContext context, Player target) {
        if (target == null || mc.player == null) return;

        // Получаем интерполированную позицию камеры игрока
        Vec3 cameraPos = context.camera().getPosition();

        // Рассчитываем координаты бокса цели относительно камеры
        double renderX = target.getX() - cameraPos.x;
        double renderY = target.getY() - cameraPos.y;
        double renderZ = target.getZ() - cameraPos.z;

        // Создаем границы (AABB) вокруг противника по его размерам
        float width = target.getBbWidth() / 2.0F;
        float height = target.getBbHeight();
        AABB box = new AABB(
                renderX - width, renderY, renderZ - width,
                renderX + width, renderY + height, renderZ + width
        );

        // Настройка состояния OpenGL через новые шейдеры 26.x
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        
        // Отключаем тест глубины, чтобы ESP было видно сквозь блоки (стены)
        RenderSystem.disableDepthTest(); 
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        PoseStack poseStack = context.matrixStack();
        poseStack.pushPose();

        // Получаем матрицу трансформации из PoseStack
        Matrix4f matrix = poseStack.last().pose();
        
        // Инициализируем BufferBuilder для рисования линий в 26.1.2
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // Красный цвет для ESP (RGBA: 255, 0, 0, 255)
        drawBoundingBoxLines(bufferBuilder, matrix, box, 1.0F, 0.0F, 0.0F, 1.0F);

        // Рендерим накопленный буфер
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        poseStack.popPose();
        
        // Возвращаем стандартные настройки рендеринга обратно
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawBoundingBoxLines(BufferBuilder buffer, Matrix4f matrix, AABB box, float r, float g, float b, float a) {
        // Нижнее основание куба
        vertex(buffer, matrix, box.minX, box.minY, box.minZ, r, g, b, a);
        vertex(buffer, matrix, box.maxX, box.minY, box.minZ, r, g, b, a);

        vertex(buffer, matrix, box.maxX, box.minY, box.minZ, r, g, b, a);
        vertex(buffer, matrix, box.maxX, box.minY, box.maxZ, r, g, b, a);

        vertex(buffer, matrix, box.maxX, box.minY, box.maxZ, r, g, b, a);
        vertex(buffer, matrix, box.minX, box.minY, box.maxZ, r, g, b, a);

        vertex(buffer, matrix, box.minX, box.minY, box.maxZ, r, g, b, a);
        vertex(buffer, matrix, box.minX, box.minY, box.minZ, r, g, b, a);

        // Верхнее основание куба
        vertex(buffer, matrix, box.minX, box.maxY, box.minZ, r, g, b, a);
        vertex(buffer, matrix, box.maxX, box.maxY, box.minZ, r, g, b, a);

        vertex(buffer, matrix, box.maxX, box.maxY, box.minZ, r, g, b, a);
        vertex(buffer, matrix, box.maxX, box.maxY, box.maxZ, r, g, b, a);

        vertex(buffer, matrix, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        vertex(buffer, matrix, box.minX, box.maxY, box.maxZ, r, g, b, a);

        vertex(buffer, matrix, box.minX, box.maxY, box.maxZ, r, g, b, a);
        vertex(buffer, matrix, box.minX, box.maxY, box.minZ, r, g, b, a);

        // Вертикальные ребра (боковые линии)
        vertex(buffer, matrix, box.minX, box.minY, box.minZ, r, g, b, a);
        vertex(buffer, matrix, box.minX, box.maxY, box.minZ, r, g, b, a);

        vertex(buffer, matrix, box.maxX, box.minY, box.minZ, r, g, b, a);
        vertex(buffer, matrix, box.maxX, box.maxY, box.minZ, r, g, b, a);

        vertex(buffer, matrix, box.maxX, box.minY, box.maxZ, r, g, b, a);
        vertex(buffer, matrix, box.maxX, box.maxY, box.maxZ, r, g, b, a);

        vertex(buffer, matrix, box.minX, box.minY, box.maxZ, r, g, b, a);
        vertex(buffer, matrix, box.minX, box.maxY, box.maxZ, r, g, b, a);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z, float r, float g, float b, float a) {
        buffer.addVertex(matrix, (float) x, (float) y, (float) z).setColor(r, g, b, a);
    }
}
