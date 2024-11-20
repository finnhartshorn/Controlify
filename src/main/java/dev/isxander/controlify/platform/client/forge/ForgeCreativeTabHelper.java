//? if forge {
/*package dev.isxander.controlify.platform.client.forge;

import dev.isxander.controlify.platform.client.CreativeTabHelper;
import dev.isxander.controlify.platform.forge.mixins.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.List;

public class ForgeCreativeTabHelper implements CreativeTabHelper {
    private final CreativeModeInventoryScreen screen;

    public ForgeCreativeTabHelper(CreativeModeInventoryScreen screen) {
        this.screen = screen;
    }

    @Override
    public void setCurrentPage(int page) {
        screen.setCurrentPage(getPages().get(page));
    }

    @Override
    public int getCurrentPage() {
        return getPages().indexOf(screen.getCurrentPage());
    }

    @Override
    public int getPageCount() {
        return getPages().size();
    }

    @Override
    public List<CreativeModeTab> getTabsForPage(int page) {
        return screen.getCurrentPage().getVisibleTabs();
    }

    @Override
    public CreativeModeTab getSelectedTab() {
        return CreativeModeInventoryScreenAccessor.getSelectedTab();
    }

    @Override
    public void setSelectedTab(CreativeModeTab tab) {
        ((CreativeModeInventoryScreenAccessor) screen).invokeSelectTab(tab);
    }

    private List<CreativeTabsScreenPage> getPages() {
        return  ObfuscationReflectionHelper.getPrivateValue(CreativeModeInventoryScreen.class, screen, "pages");
    }
}
*///?}
