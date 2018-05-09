package com.mattsmeets.macrokey.gui;

import com.mattsmeets.macrokey.event.MacroEvent;
import com.mattsmeets.macrokey.gui.fragment.MacroListFragment;
import com.mattsmeets.macrokey.model.LayerInterface;
import com.mattsmeets.macrokey.model.MacroInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mattsmeets.macrokey.MacroKey.instance;

public class GuiMacroManagement extends GuiScreen {
    private MacroListFragment keyBindingList;

    private final GuiScreen parentScreen;
    private final GameSettings settings;

    private GuiButton layerEditor;
    private GuiButton layerSwitcher;

    public MacroInterface macroModify;

    private final String screenTitle = I18n.format("gui.keybindings.screenTitle");

    private GuiButton buttonDone;
    private GuiButton buttonAdd;

    private int currentSelectedLayer;
    private List<LayerInterface> layers;

    private boolean updateList = false;

    public GuiMacroManagement(GuiScreen screen, GameSettings settings) {
        this.parentScreen = screen;
        this.settings = settings;
        this.currentSelectedLayer = -1;

        try {
            this.layers = instance.bindingsRepository.findAllLayers(true)
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if(this.updateList){
            this.updateScreen();
        }

        this.keyBindingList.drawScreen(mouseX, mouseY, partialTicks);

        buttonDone.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, 0.0f);
        buttonAdd.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, 0.0f);

        layerEditor.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, 0.0f);

        layerSwitcher.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, 0.0f);

        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 8, 16777215);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(this.parentScreen);
                break;
            case 1:
                this.mc.displayGuiScreen(new GuiModifyMacro(this));
                break;
            case 3:
                if (currentSelectedLayer < this.layers.size() - 1) {
                    currentSelectedLayer++;
                } else {
                    currentSelectedLayer = -1;
                }

                this.updateList = true;
                break;
        }

        /*
        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiManageLayers(this, mc.gameSettings));
        }*/
    }


    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        this.buttonList.add(buttonDone = new GuiButton(0, this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(buttonAdd = new GuiButton(1, this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("gui.keybindings.addkeybinding", new Object[0])));

        this.buttonList.add(layerEditor = new GuiButton(2, this.width / 2 - 155 + 160, 40, 150, 20, "Layer Editor"));
        this.buttonList.add(layerSwitcher = new GuiButton(3, this.width / 2 - 155, 40, 150, 20, "Switch Layer"));

        this.updateList = true;
    }


    @Override
    public void updateScreen() {
        super.updateScreen();

        if (!this.updateList) {
            return;
        }

        LayerInterface currentLayer = currentSelectedLayer == -1 ? null : this.layers.get(currentSelectedLayer);

        try {
            this.keyBindingList = new MacroListFragment(this, currentLayer);
            this.layerSwitcher.displayString = I18n.format("text.layer.display",
                    currentLayer == null ? I18n.format("text.layer.master") : currentLayer.getDisplayName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.updateList = false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.macroModify == null) {
            super.keyTyped(typedChar, keyCode);

            return;
        }

        if (keyCode == 1) {
            this.macroModify.setKeyCode(0);
        } else if (keyCode != 0) {
            this.macroModify.setKeyCode(keyCode);
        } else if (typedChar > 0) {
            this.macroModify.setKeyCode(typedChar + 256);
        }

        MinecraftForge.EVENT_BUS.post(new MacroEvent.MacroChangedEvent(this.macroModify));

        this.macroModify = null;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.macroModify != null) {
            this.macroModify = null;
        } else if (mouseButton != 0 || !this.keyBindingList.mouseClicked(mouseX, mouseY, mouseButton)) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (buttonDone.mousePressed(mc, mouseX, mouseY)) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }


    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        this.keyBindingList.handleMouseInput();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
