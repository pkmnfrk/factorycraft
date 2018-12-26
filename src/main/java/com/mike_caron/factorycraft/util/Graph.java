package com.mike_caron.factorycraft.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiPredicate;

public class Graph<K, V>
    implements INBTSerializable<NBTTagCompound>
{
    private final HashMap<K, Set<K>> nodes = new HashMap<>();
    private final HashMap<K, V> values = new HashMap<>();
    private final INBTSerializer<K> keySerializer;
    private final INBTSerializer<V> valueSerializer;

    public Graph(@Nonnull INBTSerializer<K> keySerializer, @Nonnull INBTSerializer<V> valueSerializer)
    {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    public void addEdge(@Nonnull K key, @Nonnull K other)
            throws IllegalStateException
    {
        if(!nodes.containsKey(key))
        {
            throw new IllegalStateException("First node is not part of the graph");
        }

        if(!nodes.containsKey(other))
        {
            throw new IllegalStateException("Second node is not part of the graph");
        }

        nodes.get(key).add(other);
        nodes.get(other).add(key);

    }

    public void removeEdge(@Nonnull K key, @Nonnull K other)
            throws IllegalStateException
    {
        if (!nodes.containsKey(key))
        {
            throw new IllegalStateException("First node is not part of the graph");
        }

        if (!nodes.containsKey(other))
        {
            throw new IllegalStateException("Second node is not part of the graph");
        }

        nodes.get(key).remove(other);
        nodes.get(other).remove(key);
    }

    public void addNode(@Nonnull K key, @Nonnull V value)
    {
        if (!nodes.containsKey(key))
        {
            nodes.put(key, new HashSet<>());
        }
        values.put(key, value);
    }

    public void removeNode(@Nonnull K key)
    {
        if(nodes.containsKey(key))
        {
            for (K other : nodes.get(key))
            {
                nodes.get(other).remove(key);
            }
            nodes.remove(key);
            values.remove(key);
        }
    }

    public int nodeCount()
    {
        return nodes.size();
    }

    @Nonnull
    public Set<K> getEdges(@Nonnull K key)
    {
        if(nodes.containsKey(key))
            return nodes.get(key);

        return Collections.emptySet();
    }

    @Nonnull
    public List<Set<K>> getDiscreteGraphs()
    {
        List<Set<K>> ret = new ArrayList<>();
        if(nodes.isEmpty())
            return ret;

        Set<K> allNodes = new HashSet<>(nodes.keySet());

        while(!allNodes.isEmpty())
        {

            Set<K> currentNodes = new HashSet<>();
            Stack<K> toVisit = new Stack<>();

            K first = allNodes.stream().findAny().orElse(null);

            toVisit.push(first);

            while(!toVisit.empty())
            {
                K node = toVisit.pop();

                allNodes.remove(node);
                currentNodes.add(node);

                for(K edge : nodes.get(node))
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

    @Nonnull
    public V getValue(@Nonnull K key)
    {
        if(!nodes.containsKey(key))
        {
            throw new IllegalStateException("Node is not part of the graph");
        }

        return values.get(key);
    }

    @Nonnull
    public Set<K> nodesMatching(@Nonnull BiPredicate<K, V> predicate)
    {
        Set<K> ret = new HashSet<>();

        for(Map.Entry<K,V> entry : values.entrySet())
        {
            if(predicate.test(entry.getKey(), entry.getValue()))
            {
                ret.add(entry.getKey());
            }
        }

        return ret;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound ret = new NBTTagCompound();

        NBTTagList nodes = new NBTTagList();

        for(Map.Entry<K, Set<K>> node : this.nodes.entrySet())
        {
            NBTTagCompound nodeObj = new NBTTagCompound();

            nodeObj.setTag("key", keySerializer.serializeNBT(node.getKey()));
            nodeObj.setTag("value", valueSerializer.serializeNBT(values.get(node.getKey())));

            NBTTagList edges = new NBTTagList();

            for(K e : node.getValue())
            {
                edges.appendTag(keySerializer.serializeNBT(e));
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
        this.values.clear();

        NBTTagList nodes = nbtTagCompound.getTagList("nodes", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < nodes.tagCount(); i++)
        {
            NBTTagCompound nodeObj = nodes.getCompoundTagAt(i);

            K key = keySerializer.deserializeNBT(nodeObj.getTag("key"));
            V value = valueSerializer.deserializeNBT(nodeObj.getTag("value"));

            this.values.put(key, value);

            Set<K> edges = new HashSet<>();
            NBTTagList edgeList = (NBTTagList)nodeObj.getTag("edges");

            for(int j = 0; j < edgeList.tagCount(); j++)
            {
                K edge = keySerializer.deserializeNBT(edgeList.get(j));

                edges.add(edge);
            }

            this.nodes.put(key, edges);

        }
    }
}
