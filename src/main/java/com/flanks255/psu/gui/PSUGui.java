package com.flanks255.psu.gui;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.network.SlotClickMessage;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class PSUGui extends ContainerScreen<PSUContainer> {
    public PSUGui (PSUContainer container, PlayerInventory inventory, ITextComponent name) {
        super(container, inventory, name);
        imageWidth = 176;
        imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();

        Button.IPressable slotClick = button -> {
            PocketStorage.NETWORK.sendToServer(new SlotClickMessage(((GUISlot)button).slot + scroll, Screen.hasShiftDown(), Screen.hasControlDown(), false));
            menu.networkSlotClick(((GUISlot)button).slot+scroll, Screen.hasShiftDown(), Screen.hasControlDown(), false);
        };

        addButton(new GUISlot(leftPos + 8, topPos + 19, 34,36,0 , slotClick));
        addButton(new GUISlot(leftPos + 8 + 36, topPos + 19, 34,36,1 , slotClick));
        addButton(new GUISlot(leftPos + 8 + 72, topPos + 19, 34,36,2 , slotClick));
        addButton(new GUISlot(leftPos + 8 + 108, topPos + 19, 34,36,3 , slotClick));
        addButton(new GUISlot(leftPos + 8, topPos + 19 + 38, 34,36,4 , slotClick));
        addButton(new GUISlot(leftPos + 8 + 36, topPos + 19 + 38, 34,36,5 , slotClick));
        addButton(new GUISlot(leftPos + 8 + 72, topPos + 19 + 38, 34,36,6 , slotClick));
        addButton(new GUISlot(leftPos + 8 + 108, topPos + 19 + 38, 34,36,7 , slotClick));
        addButton(new ScrollButton(leftPos+ 152,topPos + 18, 16,37, true, (A) ->  scroll = scroll <= 0?0:scroll - 4 ));
        addButton(new ScrollButton(leftPos + 152,topPos + 55, 16,37, false, (A) ->  scroll = scroll >= menu.handler.getSlots()-8?menu.handler.getSlots()-8:scroll + 4 ));
    }

    private final ResourceLocation GUI = new ResourceLocation(PocketStorage.MODID, "textures/gui/psugui.png");
    private int scroll = 0;

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (pDelta < 0)
            scroll = MathHelper.clamp(scroll + 4, 0, menu.handler.getSlots() -8);
        if (pDelta > 0)
            scroll = MathHelper.clamp(scroll - 4, 0, menu.handler.getSlots() -8);
        return false;
    }

    @Override
    protected void renderBg(@Nonnull MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        this.getMinecraft().textureManager.bind(GUI);
        blit(matrixStack, leftPos, topPos, 0,0, 176,180, 176 ,180);
    }

    @Override
    protected void renderLabels(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, this.title.getString(), 7,6,0x404040);
    }

    @Override
    public void render(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        for (IGuiEventListener listener : children) {
            if (listener instanceof GUISlot)
                ((IRenderable) listener).render(stack, mouseX, mouseY, partialTicks);
        }
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(@Nonnull MatrixStack stack, int x, int y) {
        for (IGuiEventListener listener : children) {
            if (listener instanceof GUISlot)
                ((GUISlot) listener).renderToolTip(stack, x, y);
        }
        super.renderTooltip(stack, x, y);
    }

    class ScrollButton extends Button {
        public ScrollButton (int x, int y, int width, int height, boolean upIn, Button.IPressable pressable) {
            super(x,y,width,height,new StringTextComponent(""),pressable);
            up = upIn;
        }
        private final boolean up;
        private final ResourceLocation TEX = new ResourceLocation(PocketStorage.MODID, "textures/gui/buttons.png");


        @Override
        public void renderButton(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
            getMinecraft().getTextureManager().bind(TEX);
            if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height)
                blit(stack, x, y, 16, up?0:37, 16, 37, 32, 74);
            else
                blit(stack, x, y, 0, up?0:37, 16, 37, 32, 74);
        }
    }


    class GUISlot extends Button {
        public GUISlot(int x, int y, int width, int height,int slotIn, Button.IPressable pressable) {
            super(x,y,width,height,new StringTextComponent(""), pressable);
            this.slot = slotIn;
        }
        public final int slot;

        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (this.active && this.visible) {
                if (pButton == 0 && this.clicked(pMouseX,pMouseY)) {
                    //this.playDownSound(Minecraft.getInstance().getSoundHandler());
                    this.onClick(pMouseX, pMouseY);
                    return true;
                } else if (pButton == 1 && this.clicked(pMouseX,pMouseY)) {
                    PocketStorage.NETWORK.sendToServer(new SlotClickMessage(slot + scroll, Screen.hasShiftDown(), Screen.hasControlDown(), true));
                    menu.networkSlotClick(slot+scroll, Screen.hasShiftDown(), Screen.hasControlDown(), true);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void renderToolTip(@Nonnull MatrixStack mStack, int mx, int my) {
            if (mx >= x && mx < x + width && my >= y && my < y + height && menu != null && menu.handler != null) {
                ItemStack stack = menu.handler.getStackInSlot(slot + scroll);
                if(!stack.isEmpty()) {
                    net.minecraftforge.fml.client.gui.GuiUtils.preItemToolTip(stack);
                    List<ITextComponent> tooltip = getTooltipFromItem(stack);
                    tooltip.add(new TranslationTextComponent("pocketstorage.util.count").withStyle(TextFormatting.WHITE).append(String.valueOf(stack.getCount())));
                    //renderTooltip with list
                    renderComponentTooltip(mStack, tooltip, mx, my);
                    net.minecraftforge.fml.client.gui.GuiUtils.postItemToolTip();
                }
            }
        }

        public String formatAmount(int input) {
            if (input >= 1000 && input < 1000000)
                return String.format("%.2fK", input / 1000.0f);
            else if (input >= 1000000 && input < 1000000000)
                return String.format("%.2fM", input / 1000000.0f);
            else if (input >= 1000000000)
                return String.format("%.2fG", input / 1000000000.0f);
            else
                return String.valueOf(input);
        }

        @Override
        public void renderButton(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
            mStack.pushPose();
            FontRenderer fontRenderer = Minecraft.getInstance().font;

            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if (hovered) {
                fill(mStack, x, y - 1, x + width, y + height, -2130706433);
            }
            mStack.pushPose();
            mStack.translate(x + 0.5, y + 0.5, 0);
            mStack.scale(0.5f, 0.5f, 0.5f);
            fontRenderer.draw(mStack,"#" + (slot + scroll), 0, 0, 0x454545);
            mStack.popPose();
            if (menu.handler != null) {
                ItemStack tmp = menu.handler.getStackInSlot(slot + scroll);
                if (tmp != null) {
                    itemRenderer.blitOffset = 100F;
                    RenderSystem.enableDepthTest();
                    RenderHelper.turnBackOn();
                    itemRenderer.renderAndDecorateItem(tmp, x + 9, y + 4);
                    if (tmp.getCount() > 0) {
                        String count = Integer.toString(tmp.getCount());
                        int stringWidth = fontRenderer.width(count);

                        fontRenderer.draw(mStack, formatAmount(tmp.getCount()), x + 1 + (width / 2.0f) - (stringWidth / 2.0f), y + 22, 0x000000);
                    } else
                        fontRenderer.draw(mStack, new TranslationTextComponent("pocketstorage.util.empty"), x + 1 + (width / 2.0f) - (fontRenderer.width(new TranslationTextComponent("pocketstorage.util.empty")) / 2.0f), y + 20, 0x000000);
                    itemRenderer.blitOffset = 0F;
                    RenderHelper.turnOff();
                }
            }
            mStack.popPose();
        }
    }
}
