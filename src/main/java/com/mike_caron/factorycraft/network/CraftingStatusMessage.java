package com.mike_caron.factorycraft.network;

import com.mike_caron.factorycraft.api.IPlayerCrafting;
import com.mike_caron.factorycraft.api.capabilities.CapabilityPlayerCrafting;
import com.mike_caron.factorycraft.util.Tuple2;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class CraftingStatusMessage
    implements IMessage
{
    String[] items;
    int[] counts;

    public CraftingStatusMessage()
    {

    }

    public CraftingStatusMessage(List<Tuple2<Item, Integer>> items)
    {
        this.items = new String[items.size()];
        this.counts = new int[items.size()];

        for(int i = 0; i < items.size(); i++)
        {
            this.items[i] = ItemUtils.getTagFromItem(items.get(i).first);
            this.counts[i] = items.get(i).second;
        }
    }

    @Override
    public void fromBytes(ByteBuf byteBuf)
    {
        int numItems = byteBuf.readInt();
        items = new String[numItems];
        counts = new int[numItems];

        for(int i = 0; i < numItems; i++)
        {
            items[i] = ByteBufUtils.readUTF8String(byteBuf);
            counts[i] = byteBuf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf byteBuf)
    {
        byteBuf.writeInt(items.length);
        for(int i = 0; i < items.length; i++)
        {
            ByteBufUtils.writeUTF8String(byteBuf, items[i]);
            byteBuf.writeInt(counts[i]);
        }
    }

    public static class Handler
        implements IMessageHandler<CraftingStatusMessage, IMessage>
    {
        @Override
        public IMessage onMessage(CraftingStatusMessage msg, MessageContext ctx)
        {
            IPlayerCrafting crafting = Minecraft.getMinecraft().player.getCapability(CapabilityPlayerCrafting.PLAYER_CRAFTING, null);

            if(crafting != null)
            {
                crafting.handleQueueMessage(msg.items, msg.counts);
            }

            return null;
        }
    }
}
