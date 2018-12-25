package com.mike_caron.factorycraft.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.*;

public class Graph<V, N extends NBTBase>
    implements INBTSerializable<NBTTagCompound>
{
    private final HashMap<V, Set<V>> nodes = new HashMap<>();
    private final INBTSerializer<V, N> serializer;

    public Graph(@Nonnull INBTSerializer<V, N> serializer)
    {
        this.serializer = serializer;
    }

    public void addEdge(V key, V other)
    {
        if(!nodes.containsKey(key))
        {
            nodes.put(key, new HashSet<>());
        }

        if(!nodes.containsKey(other))
        {
            nodes.put(other, new HashSet<>());
        }

        nodes.get(key).add(other);
        nodes.get(other).add(key);

    }

    public void removeEdge(V key, V other)
    {
        if(nodes.containsKey(key))
        {
            nodes.get(key).remove(other);
        }

        if(nodes.containsKey(other))
        {
            nodes.get(other).remove(key);
        }
    }

    public void addNode(V key)
    {
        if (!nodes.containsKey(key))
        {
            nodes.put(key, new HashSet<>());
        }
    }

    public void removeNode(V key)
    {
        if(nodes.containsKey(key))
        {
            for (V other : nodes.get(key))
            {
                nodes.get(other).remove(key);
            }
            nodes.remove(key);
        }
    }

    public int nodeCount()
    {
        return nodes.size();
    }

    public Set<V> getEdges(V key)
    {
        if(nodes.containsKey(key))
            return nodes.get(key);

        return Collections.emptySet();
    }

    public List<Set<V>> getDiscreteGraphs()
    {
        List<Set<V>> ret = new ArrayList<>();
        if(nodes.isEmpty())
            return ret;

        Set<V> allNodes = new HashSet<>(nodes.keySet());

        while(!allNodes.isEmpty())
        {

            Set<V> currentNodes = new HashSet<>();
            Stack<V> toVisit = new Stack<>();

            V first = allNodes.stream().findAny().orElse(null);

            toVisit.push(first);

            while(!toVisit.empty())
            {
                V node = toVisit.pop();

                allNodes.remove(node);
                currentNodes.add(node);

                for(V edge : nodes.get(node))
                {
                    if(!currentNodes.contains(edge))
                    {
                        toVisit.push(edge);
                    }
                }
            }

            ret.add(currentNodes);

        }

        return ret;

    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound ret = new NBTTagCompound();

        NBTTagList nodes = new NBTTagList();

        for(Map.Entry<V, Set<V>> node : this.nodes.entrySet())
        {
            NBTTagCompound nodeObj = new NBTTagCompound();

            nodeObj.setTag("key", serializer.serializeNBT(node.getKey()));

            NBTTagList edges = new NBTTagList();

            for(V e : node.getValue())
            {
                edges.appendTag(serializer.serializeNBT(e));
            }

            nodeObj.setTag("edges", edges);

            nodes.appendTag(nodeObj);
        }

        ret.setTag("nodes", nodes);

        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbtTagCompound)
    {
        this.nodes.clear();
        NBTTagList nodes = nbtTagCompound.getTagList("nodes", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < nodes.tagCount(); i++)
        {
            NBTTagCompound nodeObj = nodes.getCompoundTagAt(i);

            V key = serializer.deserializeNBT((N)nodeObj.getTag("key"));

            Set<V> edges = new HashSet<>();
            NBTTagList edgeList = nodeObj.getTagList("edges", Constants.NBT.TAG_COMPOUND);

            for(int j = 0; j < edgeList.tagCount(); j++)
            {
                V edge = serializer.deserializeNBT((N)edgeList.get(j));

                edges.add(edge);
            }

            this.nodes.put(key, edges);

        }
    }
}
