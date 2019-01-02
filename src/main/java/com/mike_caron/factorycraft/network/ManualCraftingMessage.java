package com.mike_caron.factorycraft.network;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IPlayerCrafting;
import com.mike_caron.factorycraft.api.capabilities.CapabilityPlayerCrafting;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ManualCraftingMessage
    implements IMessage
{
    private Item item;
    private int amount;

    public ManualCraftingMessage()
    {

    }

    public ManualCraftingMessage(Item item, int amount)
    {
        this.item = item;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf)
    {
        this.item = ItemUtils.getItemFromTag(ByteBufUtils.readUTF8String(byteBuf));
        this.amount = byteBuf.readInt();
    }

    @Override
    public void toBytes(ByteBuf byteBuf)
    {
        ByteBufUtils.writeUTF8String(byteBuf, ItemUtils.getTagFromItem(item));
        byteBuf.writeInt(amount);
    }

    public static class Handler
        implements IMessageHandler<ManualCraftingMessage, IMessage>
    {
        @Override
        public IMessage onMessage(ManualCraftingMessage msg, MessageContext ctx)
        {
            IPlayerCrafting crafting = ctx.getServerHandler().player.getCapability(CapabilityPlayerCrafting.PLAYER_CRAFTING, null);

            if(crafting == null)
            {
                FactoryCraft.logger.error("Player {}'s IPlayerCrafting was null", ctx.getServerHandler().player.getName());
                return null;
            }

            crafting.enqueueCrafting(msg.item, msg.amount);

            return null;
        }
    }
}
