package dev.isxander.controlify.mixins.core;

import com.mojang.blaze3d.platform.GLX;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.LongSupplier;

@Mixin(value = GLX.class, remap = false)
public class GLXMixin {
    @Inject(method = "_initGlfw", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwInit()Z"))
    private static void addInitHints(CallbackInfoReturnable<LongSupplier> cir) {
        // stops GLFW adding hats to button list (backward compat)
        GLFW.glfwInitHint(GLFW.GLFW_JOYSTICK_HAT_BUTTONS, GLFW.GLFW_FALSE);
    }
}
