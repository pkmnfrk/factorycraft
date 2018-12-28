package com.mike_caron.factorycraft.tileentity;

import com.mike_caron.factorycraft.FactoryCraft;
import com.mike_caron.factorycraft.api.IConveyorBelt;
import com.mike_caron.factorycraft.block.BlockConveyor;
import com.mike_caron.factorycraft.api.capabilities.CapabilityConveyor;
import com.mike_caron.factorycraft.util.Tuple2;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class TileEntityConveyor
    extends TypedTileEntity
    implements IConveyorBelt, ITickable
{
    public final List<Track> tracks = new ArrayList<>();
    public static final float ITEM_RADIUS = 4.5f / 32f;

    public BlockConveyor.EnumTurn cachedTurn;
    public EnumFacing cachedFacing;

    public boolean populated = false;

    private static int lastUpdateTick = 0;

    private int lastUpdate = -1;

    public TileEntityConveyor()
    {
        super();

        for(int i = 0; i < numTracks(); i++)
        {
            tracks.add(new Track(i));
        }
    }

    public TileEntityConveyor(int type)
    {
        super(type);

        for(int i = 0; i < numTracks(); i++)
        {
            tracks.add(new Track(i));
        }
    }

    @Override
    protected void onKnowingType()
    {
        super.onKnowingType();


    }

    public void addItemsToDrop(NonNullList<ItemStack> items)
    {
        for(int i = 0; i < numTracks(); i++)
        {
            items.addAll(tracks.get(0).items.stream().map(it -> it.second).collect(Collectors.toList()));
        }

    }

    @SubscribeEvent
    public static void preUpdate(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            lastUpdateTick += 1;
        }
    }

    @Override
    public float trackLength(int track)
    {
        return tracks.get(track).getLength();
    }

    @Override
    @Nonnull
    public ItemStack extract(int track, float position, boolean simulate)
    {
        return tracks.get(track).extract(position, position, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extract(int track, float minPosition, float maxPosition, boolean simulate)
    {
        return tracks.get(track).extract(minPosition, maxPosition, simulate);
    }

    @Override
    @Nonnull
    public ItemStack insert(int track, float position, @Nonnull ItemStack itemStack, boolean simulate)
    {
        return tracks.get(track).insert(position, itemStack, simulate);
    }

    @Override
    public void update()
    {
        if(world.isRemote)
        {
            for(Track t : tracks)
            {
                t.update();
            }
            return;
        }

        if(lastUpdate == lastUpdateTick)
            return;

        //debugPopluation();

        //so basically, we want to follow the chain of conveyors and update them
        //from the furthest point, back up to us.
        //due to how conveyors can link up, we might find a conveyor that has already
        //been updated, in which case we know we're good and can stop.
        //otherwise, we keep going until we run out of conveyors

        Stack<TileEntityConveyor> entitiesToUpdate = new Stack<>();

        TileEntityConveyor tec = this;

        while(tec != null && tec.needsUpdate() && !entitiesToUpdate.contains(tec))
        {
            entitiesToUpdate.push(tec);
            tec = findNextConveyor(tec.pos);
        }

        while(!entitiesToUpdate.empty())
        {
            tec = entitiesToUpdate.pop();
            tec.updateInternal();
        }

    }

    private boolean needsUpdate()
    {
        return this.lastUpdate != lastUpdateTick;
    }

    private void updateInternal()
    {
        this.lastUpdate = lastUpdateTick;

        updateTrackLengths();

        for(Track t : tracks)
        {
            t.update();
        }
    }

    private TileEntityConveyor findNextConveyor(BlockPos pos)
    {
        EnumFacing facing = getFacing(pos);
        BlockPos nextPos = pos.offset(facing);
        TileEntity te = world.getTileEntity(nextPos);
        if(te instanceof TileEntityConveyor)
            return (TileEntityConveyor)te;

        return null;
    }

    private EnumFacing getFacing(BlockPos pos)
    {
        return world.getBlockState(pos).getValue(BlockConveyor.FACING);
    }

    private EnumFacing getFacing()
    {
        if(cachedFacing == null)
        {
            cachedFacing = getFacing(pos);
        }

        return cachedFacing;
    }

    private BlockConveyor.EnumTurn getTurn(BlockPos pos)
    {
        return world.getBlockState(pos).getValue(BlockConveyor.TURN);
    }

    private BlockConveyor.EnumTurn getTurn()
    {
        if(cachedTurn == null)
        {
            cachedTurn = getTurn(pos);
        }

        return cachedTurn;
    }

    private void debugPopluation()
    {
        if(!populated)
        {
            if (tracks.get(0).getItemCount() == 0)
            {
                int i = 0;
                for (Track t : tracks)
                {
                    for (float f = ITEM_RADIUS; f < t.getLength(); f += 1 / 32f)
                    {
                        t.insert(f, new ItemStack(i == 0 ? Items.COAL : Items.DIAMOND, 1), false);
                    }
                    //t.insert(0.6f, new ItemStack(i == 0 ? Items.COAL : Items.DIAMOND, 1), false);
                    i += 1;
                }
                markDirty();
            }
            populated = true;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagList trackList = compound.getTagList("tracks", Constants.NBT.TAG_COMPOUND);

        for(int i = 0; i < trackList.tagCount(); i++)
        {
            NBTTagCompound trackTag = trackList.getCompoundTagAt(i);
            tracks.get(i).deserializeNBT(trackTag);
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        NBTTagList trackList = new NBTTagList();

        for(int i = 0; i < tracks.size(); i++)
        {
            trackList.appendTag(tracks.get(i).serializeNBT());
        }

        ret.setTag("tracks", trackList);

        return ret;
    }

    public void updateTrackLengths()
    {
        updateTrackLengths(getTurn());

    }

    public void updateTrackLengths(BlockConveyor.EnumTurn turn)
    {
    /*           left  right
          ---- +-----  -----+
    |0^1| 0000 |00000  11111|
    |0^1| >>>> |0/>>>  <<<\1|
    |0^1| 1111 |0^111  000^1|
          ---- |0^1+-  -+0^1|
     */

        if(turn == BlockConveyor.EnumTurn.Straight)
        {
            //phew
            tracks.get(0).setLength(1f);
            tracks.get(1).setLength(1f);
        }
        else if(turn == BlockConveyor.EnumTurn.Left)
        {
            tracks.get(0).setLength(37f/32f);
            tracks.get(1).setLength(13.5f/32f);
        }
        else if(turn == BlockConveyor.EnumTurn.Right)
        {
            tracks.get(1).setLength(37f/32f);
            tracks.get(0).setLength(13.5f/32f);
        }
        else
        {
            throw new Error("Failed");
        }
    }

    @Override
    public int numTracks()
    {
        return 2;
    }

    @Override
    public int totalItems()
    {
        int ret = 0;
        for(int i = 0; i < numTracks(); i++)
        {
            ret += tracks.get(i).getItemCount();
        }
        return ret;
    }

    public ItemPosition itemPositions()
    {
        IBlockState state = world.getBlockState(pos);

        EnumFacing facing = state.getValue(BlockConveyor.FACING);
        BlockConveyor.EnumTurn turn = state.getValue(BlockConveyor.TURN);

        return itemPositions(facing, turn);
    }

    public ItemPosition itemPositions(EnumFacing facing, BlockConveyor.EnumTurn turn)
    {
        return new ItemPosition(facing, turn);
    }

    @Override
    protected void markAndNotify()
    {
        super.markAndNotify();
    }

    public void notifyChange()
    {
        cachedTurn = null;
        cachedFacing = null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityConveyor.CONVEYOR)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityConveyor.CONVEYOR)
            return CapabilityConveyor.CONVEYOR.cast(this);
        return super.getCapability(capability, facing);
    }

    @Override
    public int trackClosestTo(@Nonnull EnumFacing facing)
    {
        EnumFacing myFacing = getFacing();
        if(facing == myFacing || facing == myFacing.getOpposite())
            return 0;

        if(facing == myFacing.rotateY())
            return 0;

        return 1;
    }

    public class ItemPosition
    {
        EnumFacing facing;
        BlockConveyor.EnumTurn turn;
        private final Vec2f CENTER = new Vec2f(0.5f, 0.5f);

        public ItemPosition(EnumFacing facing, BlockConveyor.EnumTurn turn)
        {
            this.facing = facing;
            this.turn = turn;
        }

        public Vec3f getPosition(int track, float position)
        {
            Vec2f northPoint;
            float x, z;
            float angle = 0;

            switch(turn)
            {
                case Straight:

                    x = (0.25f + 0.5f * (1 - track));
                    z = (position / tracks.get(track).maxLength);
                    angle = facing.getOpposite().getHorizontalAngle();
                    northPoint = new Vec2f(x, z);
                    break;
                case Left:
                {
                    final float v = tracks.get(track).maxLength;

                    float radius = track == 0 ? 0.75f : 0.25f;
                    angle = -(float) MathHelper.clampedLerp(-90, 0, position / v);

                    northPoint = new Vec2f(
                        radius * (float) Math.cos(Math.toRadians(- angle)),
                        radius * (float) Math.sin(Math.toRadians(- angle)) + 1
                    );

                    angle += facing.getHorizontalAngle() + 180;
                }
                    break;
                case Right:
                {
                    final float v = tracks.get(track).maxLength;

                    float radius = track == 0 ? 0.25f : 0.75f;
                    angle = (float) MathHelper.clampedLerp(0,  90, position / v);

                    northPoint = new Vec2f(
                        radius * (float) Math.cos(Math.toRadians(angle - 90)),
                        radius * (float) Math.sin(Math.toRadians(angle - 90)) + 1
                    );

                    angle += facing.getHorizontalAngle() + 90;
                }
                    break;
                default:
                    throw new Error("Impossible");
            }

            if(facing.getAxis() == EnumFacing.Axis.Z)
            {
                angle +=  180;
            }

            northPoint = new Vec2f(northPoint.x, northPoint.y);

            if(turn == BlockConveyor.EnumTurn.Right)
            {
                northPoint = new Vec2f(1f - northPoint.x, northPoint.y);
            }

            Vec2f ret = rotate(CENTER, facing.getHorizontalAngle(), northPoint);

            return new Vec3f(ret.x, angle, ret.y);
        }

        public void visitAllPositions(BiConsumer<ItemStack, Vec3f> visitor)
        {
            for(int i = 0; i < tracks.size(); i++)
            {
                for (Tuple2<Float, ItemStack> item : tracks.get(i).getItems())
                {
                    Vec3f p = getPosition(i, item.first);
                    visitor.accept(item.second, p);
                }
            }
        }

        public Vec2f rotate(Vec2f origin, float angle, Vec2f point)
        {
            float x = point.x - origin.x;
            float y = point.y - origin.y;

            angle = (float)Math.toRadians(angle);

            double s = Math.sin(angle);
            double c = Math.cos(angle);

            float xNew = (float)(x * c - y * s);
            float yNew = (float)(x * s + y * c);

            return new Vec2f(xNew + origin.x, yNew + origin.y);
        }
    }

    public class Track
        implements INBTSerializable<NBTTagCompound>
    {
        private final List<Tuple2<Float, ItemStack>> items = new ArrayList<>();
        private float maxLength;
        private final int trackNum;

        public Track(int num)
        {
            this.trackNum = num;
        }

        public void setLength(float length)
        {
            for(int i = 0; i < items.size(); i++)
            {
                Tuple2<Float, ItemStack> item = items.get(i);
                Tuple2<Float, ItemStack> newItem = new Tuple2<>(item.first / maxLength * length, item.second);
                items.set(i, newItem);
            }

            this.maxLength = length;
        }

        public float getLength()
        {
            return this.maxLength;
        }

        public int getItemCount()
        {
            return items.size();
        }

        public List<Tuple2<Float, ItemStack>> getItems()
        {
            return items;
        }

        public ItemStack insert(float position, ItemStack itemStack, boolean simulate)
        {
            if(position < 0f || position > maxLength)
                return itemStack;

            for(Tuple2<Float, ItemStack> item : items)
            {
                if(position + ITEM_RADIUS > item.getFirst() - ITEM_RADIUS && position - ITEM_RADIUS < item.getFirst() + ITEM_RADIUS)
                    return itemStack;

            }

            ItemStack ret = itemStack.copy();
            ret.shrink(1);

            if(!simulate)
            {
                ItemStack newStack = itemStack.copy();
                newStack.setCount(1);

                items.add(new Tuple2<>(position, newStack));
                items.sort((o1, o2) -> Float.compare(o1.first, o2.first));

                if(!world.isRemote)
                    markAndNotify();
            }

            return ret;
        }

        public ItemStack extract(float minPosition, float maxPosition, boolean simulate)
        {
            ItemStack ret = ItemStack.EMPTY;
            Tuple2<Float, ItemStack> ret_item = null;

            for(Tuple2<Float, ItemStack> item : items)
            {
                if(minPosition < item.getFirst() + ITEM_RADIUS && maxPosition > item.getFirst() - ITEM_RADIUS)
                {
                    ret = item.getSecond();
                    ret_item = item;
                    break;
                }
            }

            if(!ret.isEmpty() && !simulate)
            {
                items.remove(ret_item);

                if(!world.isRemote)
                    markAndNotify();
            }

            return ret;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound ret = new NBTTagCompound();

            ret.setFloat("maxLength", maxLength);

            NBTTagList items = new NBTTagList();

            for(Tuple2<Float, ItemStack> item : this.items)
            {
                if(item.second.isEmpty())
                    continue;

                NBTTagCompound itm = new NBTTagCompound();
                itm.setFloat("pos", item.first);
                itm.setTag("item", item.second.serializeNBT());

                items.appendTag(itm);
            }
            ret.setTag("items", items);

            return ret;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound)
        {
            maxLength = compound.getFloat("maxLength");
            NBTTagList itemList = compound.getTagList("items", Constants.NBT.TAG_COMPOUND);

            items.clear();
            for(int i = 0; i < itemList.tagCount(); i++)
            {
                NBTTagCompound item = itemList.getCompoundTagAt(i);

                float pos = item.getFloat("pos");
                ItemStack itemStack = new ItemStack(item.getCompoundTag("item"));

                items.add(new Tuple2<>(pos, itemStack));
            }

            items.sort(Comparator.comparing(a -> a.first));
        }

        public void update()
        {
            //basically, we need to push everyone down. if they hit the end of the conveyor
            //(maxLength), they need to get pushed onto the next conveyor.
            //1. If the next conveyor is nothing, then they stop
            //2. If the next conveyor is aligned, then it's easy
            //3. If the next conveyor is not aligned, then we push them onto the
            //   appropriate spot on the closest track

            float speed = (type + 1) / 32f;

            /*
            if(world.isRemote)
            {
                for(int i = 0; i < items.size(); i++)
                {
                    Tuple2<Float, ItemStack> item = items.get(i);
                    items.set(i, new Tuple2<>(item.first + speed, item.second));
                }

                return;
            }
*/
            TileEntityConveyor nextConveyor = findNextConveyor(pos);

            try
            {


                if (nextConveyor != null)
                {
                    if (isAligned(nextConveyor))
                    {
                        updateAligned(speed, nextConveyor.tracks.get(trackNum));
                    }
                    else
                    {
                        Track nextTrack = getNearestTrack(nextConveyor);
                        if (nextTrack != null)
                        {
                            updateMisaligned(speed, nextTrack, getMyPosition(nextConveyor));
                        }
                        else
                        {
                            updateNone(speed);
                        }
                    }
                }
                else
                {
                    updateNone(speed);
                }
            }
            catch(Exception ex)
            {
                if(!world.isRemote)
                {
                    FactoryCraft.logger.error("Error updating track on server side!", ex);
                }

            }

            if(!world.isRemote)
            {
                for(int i = 0; i < items.size(); i++)
                {
                    if(items.get(i).first < 0)
                    {
                        ItemUtils.dropItem(world, items.get(i).second, pos.getX(), pos.getY(), pos.getZ());
                        items.remove(i);
                        i -= 1;
                    }
                }
            }

        }

        private void updateNone(float speed)
        {
            boolean changed = false;
            float lastPos = maxLength;

            for(int i = items.size() - 1; i >= 0; i--)
            {
                Tuple2<Float, ItemStack> item = items.get(i);

                float newPos = item.first + speed;

                if(newPos + ITEM_RADIUS > lastPos)
                {
                    newPos = lastPos - ITEM_RADIUS;
                }

                lastPos = newPos - ITEM_RADIUS;

                if(newPos != lastPos)
                {
                    items.set(i, new Tuple2<Float, ItemStack>(newPos, item.second));
                    //changed = true;
                }
            }

            if(changed && !world.isRemote)
            {
                markAndNotify();
            }
        }

        private void updateAligned(float speed, Track nextTrack)
        {
            boolean changed = false;
            float lastPos = maxLength + 1f;

            if(nextTrack.items.size() > 0)
                lastPos = maxLength + nextTrack.items.get(0).first - ITEM_RADIUS;

            for(int i = items.size() - 1; i >= 0; i--)
            {
                Tuple2<Float, ItemStack> item = items.get(i);

                float newPos = item.first + speed;

                if(newPos + ITEM_RADIUS > lastPos)
                {
                    newPos = lastPos - ITEM_RADIUS;
                }

                lastPos = newPos - ITEM_RADIUS;

                if(newPos > maxLength)
                {
                    ItemStack res = nextTrack.insert(newPos - maxLength, item.second, false);
                    if(res.isEmpty())
                    {
                        newPos -= maxLength;
                        items.remove(item);
                        i -= 1;
                        changed = true;
                    }
                }
                else if(newPos != lastPos)
                {
                    items.set(i, new Tuple2<>(newPos, item.second));
                    //changed = true;
                }
            }

            if(changed && !world.isRemote)
            {
                markAndNotify();
            }
        }

        private void updateMisaligned(float speed, Track nextTrack, float myPos)
        {
            boolean changed = false;
            float lastPos = maxLength;

            ItemStack nextItem = nextTrack.extract(myPos - ITEM_RADIUS, myPos + ITEM_RADIUS, true);

            if(nextItem.isEmpty())
            {
               lastPos = maxLength + 1f;
            }

            for(int i = items.size() - 1; i >= 0; i--)
            {
                Tuple2<Float, ItemStack> item = items.get(i);

                float newPos = item.first + speed;

                if(newPos + ITEM_RADIUS > lastPos)
                {
                    newPos = lastPos - ITEM_RADIUS;
                }

                lastPos = newPos - ITEM_RADIUS;

                if(newPos > maxLength)
                {
                    ItemStack res = nextTrack.insert(myPos, item.second, false);
                    if(res.isEmpty())
                    {
                        lastPos = maxLength;
                        items.remove(item);
                        i -= 1;
                        changed = true;
                    }
                }
                else if(newPos != lastPos)
                {
                    items.set(i, new Tuple2<>(newPos, item.second));
                    //changed = true;
                }
            }

            if(changed && !world.isRemote)
            {
                markAndNotify();
            }
        }

        private boolean isAligned(TileEntityConveyor other)
        {
            EnumFacing otherFacing = other.getFacing();
            BlockConveyor.EnumTurn otherTurn = other.getTurn();
            EnumFacing myFacing = getFacing();

            if(otherTurn == BlockConveyor.EnumTurn.Right)
            {
                otherFacing = otherFacing.rotateY();
            }
            else if(otherTurn == BlockConveyor.EnumTurn.Left)
            {
                otherFacing = otherFacing.rotateYCCW();
            }

            return otherFacing == myFacing;
        }

        private Track getNearestTrack(TileEntityConveyor other)
        {
            EnumFacing otherFacing = other.getFacing();
            EnumFacing myFacing = getFacing();

            if(otherFacing == myFacing.getOpposite())
                return null;

            return other.tracks.get(other.trackClosestTo(myFacing));
        }

        private float getMyPosition(TileEntityConveyor other) throws Exception
        {
            EnumFacing otherFacing = other.getFacing();
            EnumFacing myFacing = getFacing();

            if(myFacing == otherFacing.rotateYCCW())
                return trackNum * 0.5f + 0.25f;

            if(myFacing == otherFacing.rotateY())
                return 1f - (trackNum * 0.5f + 0.25f);

            throw new Exception("Should be impossible");
        }
    }


}
