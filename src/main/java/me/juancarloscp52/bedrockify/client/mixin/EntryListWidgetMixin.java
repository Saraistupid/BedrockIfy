package me.juancarloscp52.bedrockify.client.mixin;

import me.juancarloscp52.bedrockify.Bedrockify;
import me.juancarloscp52.bedrockify.client.gui.BedrockifyRotatingCubeMapRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin {
    @Shadow protected int left;
    @Shadow @Final protected MinecraftClient client;
    @Shadow protected int bottom;
    @Shadow protected int top;
    @Shadow protected abstract void renderBackground(MatrixStack matrices);

    @Redirect(method = "render", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V"))
    private void renderPanorama(EntryListWidget entryListWidget,MatrixStack matrices){
        if(!Bedrockify.getInstance().settings.isCubemapBackgroundEnabled() || ((TranslatableText)this.client.currentScreen.getTitle()).getKey().equals("modmenu.title") || ((TranslatableText)this.client.currentScreen.getTitle()).getKey().equals("gui.socialInteractions.title")){
            this.renderBackground(matrices);
            return;
        }
        if (!(this.client.currentScreen instanceof PackScreen)) {
            BedrockifyRotatingCubeMapRenderer.getInstance().render();
            DrawableHelper.fill(matrices, 0, this.top, client.getWindow().getScaledWidth(), this.bottom, (60 << 24));
        }
    }

    // Prevent the screen background from drawing
    @Redirect(method = "render", at=@At(value = "INVOKE",target = "Lnet/minecraft/client/render/Tessellator;draw()V", ordinal = 0))
    private void doNotDrawBackground(Tessellator tessellator){
        if(!Bedrockify.getInstance().settings.isCubemapBackgroundEnabled() || ((TranslatableText)this.client.currentScreen.getTitle()).getKey().equals("modmenu.title")){
            tessellator.draw();
            return;
        }
        tessellator.getBuffer().end();
        tessellator.getBuffer().popData();
        tessellator.getBuffer().clear();
    }

    //Prevent top and bottom bars from drawing (Only on pack Screens)
    @Redirect(method = "render", at=@At(value = "INVOKE",target = "Lnet/minecraft/client/render/Tessellator;draw()V", ordinal = 1))
    private void doNotDrawBars(Tessellator tessellator){
        if(this.client.currentScreen instanceof PackScreen && Bedrockify.getInstance().settings.isCubemapBackgroundEnabled()){
            tessellator.getBuffer().end();
            tessellator.getBuffer().popData();
            tessellator.getBuffer().clear();
            return;
        }
        tessellator.draw();
    }

    //Prevent top and bottom shadows from drawing (Only on pack Screens)
    @Redirect(method = "render", at=@At(value = "INVOKE",target = "Lnet/minecraft/client/render/Tessellator;draw()V", ordinal = 2))
    private void doNotDrawBarsShadows(Tessellator tessellator){
        if(this.client.currentScreen instanceof PackScreen && Bedrockify.getInstance().settings.isCubemapBackgroundEnabled()){
            tessellator.getBuffer().end();
            tessellator.getBuffer().popData();
            tessellator.getBuffer().clear();
            return;
        }
        tessellator.draw();
    }
}
