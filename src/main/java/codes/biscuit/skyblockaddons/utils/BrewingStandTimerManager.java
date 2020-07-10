package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class BrewingStandTimerManager {

    private static Map<BlockPos, Float> brewingStands = new HashMap<>();
    private static BlockPos lastBrewingStand;
    private static final Color GREEN = new Color(0, 195, 0);
    private static final Color RED = new Color(255, 0, 0);
    private static int[] colorFade = new int[401];
    @Setter static GuiChest lastOpenChest;

    static {
        for(float i = 20; i >= 0; i -= 0.05F) {
            colorFade[(int)(i*20)] = getColorSlow(i).getRGB();
        }
    }

    public static void onRightClickBlock(BlockPos pos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
        if (block.equals(Blocks.brewing_stand)) {
            lastBrewingStand = pos;
        }
    }

    public static void onClose() {
        IInventory inv = lastOpenChest.lowerChestInventory;
        if(EnumUtils.InventoryType.getCurrentInventoryType(inv.getName()) == EnumUtils.InventoryType.BREWING_STAND) {
            ItemStack pane = inv.getStackInSlot(20);
            if(pane.getMetadata() == 1 || pane.getMetadata() == 4) {
                float timeRemaining = Float.parseFloat(EnumChatFormatting.getTextWithoutFormattingCodes(pane.getDisplayName()).replace("s", ""));
                if(lastBrewingStand != null) {
                    brewingStands.put(lastBrewingStand, timeRemaining);
                }
            } else if(pane.getMetadata() == 3) {
                brewingStands.remove(lastBrewingStand);
            }
        }
    }

    public static void onRender(float partialTicks) {
        brewingStands.forEach((pos, time) -> {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            String str = new DecimalFormat("#0.0's'").format(time);

            if(SkyblockAddons.getInstance().getConfigValues().isDisabled(Feature.COLOR_BY_TIME_REMAINING)) {
                ChromaManager.renderingText(Feature.SHOW_TIME_REMAINING_ABOVE_BREWING_STANDS);
            }
            SkyblockAddons.getInstance().getUtils().renderNameTag(str, x + 0.5F, y + 1.0F, z + 0.5F, getColor(time), partialTicks);
            ChromaManager.doneRenderingText();
        });
    }

    public static void reset() {
        brewingStands.clear();
    }

    public static void onTick() {
        brewingStands.forEach((pos, time) -> {
            time -= 0.05F;
            if (time > 0) {
                brewingStands.put(pos, time);
            } else{
                brewingStands.put(pos, 0.0F);
            }
        });
    }

    public static Color getColorSlow(float time) {
        if(time == 0) return GREEN;
        float p = 1 - time / 20;
        float[] hsb1 = Color.RGBtoHSB(RED.getRed(), RED.getGreen(), RED.getBlue(), null);
        float[] hsb2 = Color.RGBtoHSB(GREEN.getRed(), GREEN.getGreen(), GREEN.getBlue(), null);
        float[] hsb3 = new float[3];

        hsb3[0] = (hsb2[0] - hsb1[0]) * p + hsb1[0];
        hsb3[1] = (hsb2[1] - hsb1[1]) * p + hsb1[1];
        hsb3[2] = (hsb2[2] - hsb1[2]) * p + hsb1[2];

        return new Color(Color.HSBtoRGB(hsb3[0], hsb3[1], hsb3[2]));
    }

    public static Color getColor(float time) {
        if(SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.COLOR_BY_TIME_REMAINING)) {
            return new Color(colorFade[(int) (time * 20)]);
        } else {
            return SkyblockAddons.getInstance().getConfigValues().getColor(Feature.SHOW_TIME_REMAINING_ABOVE_BREWING_STANDS);
        }
    }
}
