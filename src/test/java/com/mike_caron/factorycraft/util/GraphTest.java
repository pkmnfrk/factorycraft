package com.mike_caron.factorycraft.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class GraphTest
{
    private static final IntegerSerializer serializer = new IntegerSerializer();

    @Test
    void edgesForNewNodeEmpty()
    {
        Graph<Integer, Integer> graph = getGraph();

        Assertions.assertTrue(graph.getEdges(1).isEmpty());
    }

    @Test
    void canAddNode()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);

        Assertions.assertEquals(1, graph.nodeCount());
    }

    @Test
    void canAddMultipleNodes()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);

        Assertions.assertEquals(2, graph.nodeCount());
    }

    @Test
    void canAddSameNodeMultipleTimes()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(1, 0);

        Assertions.assertEquals(1, graph.nodeCount());
    }

    @Test
    void canRemoveNode()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.removeNode(1);

        Assertions.assertEquals(0, graph.nodeCount());
    }

    @Test
    void removingNodeRemovesEdges()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);

        graph.addEdge(1, 2);
        graph.removeNode(1);

        Assertions.assertFalse(graph.getEdges(2).contains(1));
    }

    @Test
    void needToAddNodeBeforeAddingEdge()
    {
        Graph<Integer, Integer> graph = getGraph();

        Assertions.assertThrows(IllegalStateException.class, () -> graph.addEdge(1, 2));
    }

    @Test
    void needToAddOtherNodeBeforeAddingEdge()
    {
        Graph<Integer, Integer> graph = getGraph();

        Assertions.assertThrows(IllegalStateException.class, () -> {
            graph.addNode(1, 0);
            graph.addEdge(1, 2);
        });
    }

    @Test
    void canAddEdge()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);

        graph.addEdge(1, 2);

        Assertions.assertTrue(graph.getEdges(1).contains(2));
    }

    @Test
    void addEdgeAddsReciprocal()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);

        graph.addEdge(1, 2);

        Assertions.assertTrue(graph.getEdges(2).contains(1));
    }

    @Test
    void canRemoveEdge()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);

        graph.addEdge(1, 2);

        graph.removeEdge(1, 2);

        Assertions.assertFalse(graph.getEdges(1).contains(2));
    }

    @Test
    void removeEdgeRemovesReciprocal()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);

        graph.addEdge(1, 2);

        graph.removeEdge(1, 2);

        Assertions.assertFalse(graph.getEdges(2).contains(1));
    }

    @Test
    void getDiscreteGraphsHandlesNoNodes()
    {
        Graph<Integer, Integer> graph = getGraph();

        List<Set<Integer>> graphs = graph.getDiscreteGraphs();

        Assertions.assertEquals(0, graphs.size());
    }

    @Test
    void getDiscreteGraphsHandlesSingleGraph()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);

        graph.addEdge(1, 2);

        List<Set<Integer>> graphs = graph.getDiscreteGraphs();

        Assertions.assertEquals(1, graphs.size());
        Assertions.assertEquals(2, graphs.get(0).size());
        Assertions.assertTrue(graphs.get(0).contains(1));
        Assertions.assertTrue(graphs.get(0).contains(2));
    }

    @Test
    void getDiscreteGraphsDoesntBreakData()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);

        graph.addEdge(1, 2);

        graph.getDiscreteGraphs();

        Assertions.assertEquals(2, graph.nodeCount());
    }


    @Test
    void getDiscreteGraphsHandlesMultipleGraphs()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 0);
        graph.addNode(2, 0);
        graph.addNode(3, 0);
        graph.addNode(4, 0);

        graph.addEdge(1, 2);
        graph.addEdge(3, 4);

        List<Set<Integer>> graphs = graph.getDiscreteGraphs();

        Assertions.assertEquals(2, graphs.size());
        Assertions.assertEquals(2, graphs.get(0).size());
        Assertions.assertEquals(2, graphs.get(1).size());
        Assertions.assertTrue(graphs.get(0).contains(1));
        Assertions.assertTrue(graphs.get(0).contains(2));
        Assertions.assertTrue(graphs.get(1).contains(3));
        Assertions.assertTrue(graphs.get(1).contains(4));
        Assertions.assertFalse(graphs.get(1).contains(1));
        Assertions.assertFalse(graphs.get(1).contains(2));
        Assertions.assertFalse(graphs.get(0).contains(3));
        Assertions.assertFalse(graphs.get(0).contains(4));
    }

    @Test
    void graphSerializesAndDeserializesCorrectly()
    {
        Graph<Integer, Integer> graph = getGraph();

        graph.addNode(1, 3);
        graph.addNode(2, 4);
        graph.addEdge(1,2);

        NBTTagCompound data = graph.serializeNBT();

        graph = getGraph();

        graph.deserializeNBT(data);

        Assertions.assertEquals(2, graph.nodeCount());
        Assertions.assertEquals(3, (int)graph.getValue(1));
        Assertions.assertEquals(4, (int)graph.getValue(2));
        Assertions.assertTrue(graph.getEdges(1).contains(2));
        Assertions.assertTrue(graph.getEdges(2).contains(1));
    }

    @Test
    void serializerGetsTheSameTagItReturnedList()
    {
        INBTSerializer<Integer> ser = new INBTSerializer<Integer>()
        {
            @Override
            public NBTBase serializeNBT(Integer obj)
            {
                NBTTagList ret = new NBTTagList();

                ret.appendTag(new NBTTagInt(obj));

                return ret;
            }

            @Override
            public Integer deserializeNBT(NBTBase nbt)
            {
                Assertions.assertTrue(nbt instanceof NBTTagList);

                return ((NBTTagList)nbt).getIntAt(0);
            }
        };

        Graph<Integer, Integer> graph = new Graph<>(ser, ser);
    }

    @Test
    void serializerGetsTheSameTagItReturnedCompound()
    {
        INBTSerializer<Integer> ser = new INBTSerializer<Integer>()
        {
            @Override
            public NBTBase serializeNBT(Integer obj)
            {
                NBTTagCompound ret = new NBTTagCompound();

                ret.setInteger("q", obj);

                return ret;
            }

            @Override
            public Integer deserializeNBT(NBTBase nbt)
            {
                Assertions.assertTrue(nbt instanceof NBTTagCompound);

                return ((NBTTagCompound)nbt).getInteger("q");
            }
        };

        Graph<Integer, Integer> graph = new Graph<>(ser, ser);
    }

    @Test
    void serializerGetsTheSameTagItReturnedRaw()
    {
        INBTSerializer<Integer> ser = new INBTSerializer<Integer>()
        {
            @Override
            public NBTBase serializeNBT(Integer obj)
            {
                return new NBTTagInt(obj);
            }

            @Override
            public Integer deserializeNBT(NBTBase nbt)
            {
                Assertions.assertTrue(nbt instanceof NBTTagInt);

                return ((NBTTagInt)nbt).getInt();
            }
        };

        Graph<Integer, Integer> graph = new Graph<>(ser, ser);
    }

    private static Graph<Integer, Integer> getGraph()
    {
        return new Graph<>(serializer, serializer);
    }

    static class IntegerSerializer
        implements INBTSerializer<Integer>
    {
        @Override
        public NBTBase serializeNBT(Integer obj)
        {
            NBTTagCompound ret = new NBTTagCompound();

            ret.setInteger("i", obj);

            return ret;
        }

        @Override
        public Integer deserializeNBT(NBTBase nbt)
        {
            return ((NBTTagCompound)nbt).getInteger("i");
        }
    }
}
