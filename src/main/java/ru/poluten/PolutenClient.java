package ru.poluten;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.power.component.builtin.ResourceComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid="poluten",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class PolutenClient {
    private static final ResourceLocation FORM=ResourceLocation.parse("poluten:form");
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("poluten:textures/overlay/vignette.png");
    @SubscribeEvent public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.parse("poluten:shadow_vignette"),(graphics,delta)->{
            var player=Minecraft.getInstance().player;
            if(player==null||!player.isAlive()||PowerHelper.get(player).getComponent(FORM,ResourceComponent.class).map(ResourceComponent::getValue).orElse(0)!=1)return;
            boolean red=player.getFoodData().getFoodLevel()<=10;
            graphics.flush();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.setColor(red?1:0,red?.015f:0,red?.005f:0,1);
            int w=graphics.guiWidth(),h=graphics.guiHeight();
            graphics.blit(TEXTURE,0,0,-90,0,0,w,h,w,h);
            graphics.flush();
            graphics.setColor(1,1,1,1);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        });
    }
}
