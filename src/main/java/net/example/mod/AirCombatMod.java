package net.example.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.example.mod.logic.AirCombatController;
import net.example.mod.renderer.AirCombatRenderer;

public class AirCombatMod implements ClientModInitializer {
    private final AirCombatController controller = new AirCombatController();

    @Override
    public void onInitializeClient() {
        // Логика обновлений (Тики)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            controller.onTick();
        });

        // Визуализация (Новый рендеринг 26.1.2)
        WorldRenderEvents.LAST.register(context -> {
            // Отрисовываем ESP линии вокруг текущей захваченной цели
            if (controller.getTarget() != null) {
                AirCombatRenderer.renderESP(context, controller.getTarget());
            }
        });
    }
}
