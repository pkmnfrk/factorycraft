package util;

import com.mike_caron.factorycraft.util.Graph;
import com.mike_caron.factorycraft.util.INBTSerializer;
import net.minecraft.nbt.NBTTagCompound;
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
        Graph<Integer, NBTTagCompound> graph = getGraph();

        Assertions.assertTrue(graph.getEdges(1).isEmpty());
    }

    @Test
    void canAddNode()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addNode(1);

        Assertions.assertEquals(1, graph.nodeCount());
    }

    @Test
    void canAddMultipleNodes()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addNode(1);
        graph.addNode(2);

        Assertions.assertEquals(2, graph.nodeCount());
    }

    @Test
    void canAddSameNodeMultipleTimes()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addNode(1);
        graph.addNode(1);

        Assertions.assertEquals(1, graph.nodeCount());
    }

    @Test
    void canRemoveNode()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addNode(1);
        graph.removeNode(1);

        Assertions.assertEquals(0, graph.nodeCount());
    }

    @Test
    void removingNodeRemovesEdges()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addEdge(1, 2);
        graph.removeNode(1);

        Assertions.assertFalse(graph.getEdges(2).contains(1));
    }

    @Test
    void canAddEdge()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addEdge(1, 2);

        Assertions.assertTrue(graph.getEdges(1).contains(2));
    }

    @Test
    void addEdgeAddsReciprocal()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addEdge(1, 2);

        Assertions.assertTrue(graph.getEdges(2).contains(1));
    }

    @Test
    void canRemoveEdge()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addEdge(1, 2);

        graph.removeEdge(1, 2);

        Assertions.assertFalse(graph.getEdges(1).contains(2));
    }

    @Test
    void removeEdgeRemovesReciprocal()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addEdge(1, 2);

        graph.removeEdge(1, 2);

        Assertions.assertFalse(graph.getEdges(2).contains(1));
    }

    @Test
    void getDiscreteGraphsHandlesNoNodes()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

        List<Set<Integer>> graphs = graph.getDiscreteGraphs();

        Assertions.assertEquals(0, graphs.size());
    }

    @Test
    void getDiscreteGraphsHandlesSingleGraph()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

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
        Graph<Integer, NBTTagCompound> graph = getGraph();

        graph.addEdge(1, 2);

        List<Set<Integer>> graphs = graph.getDiscreteGraphs();

        Assertions.assertEquals(2, graph.nodeCount());
    }


    @Test
    void getDiscreteGraphsHandlesMultipleGraphs()
    {
        Graph<Integer, NBTTagCompound> graph = getGraph();

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

    private static Graph<Integer, NBTTagCompound> getGraph()
    {
        return new Graph<>(serializer);
    }

    static class IntegerSerializer
        implements INBTSerializer<Integer, NBTTagCompound>
    {
        @Override
        public NBTTagCompound serializeNBT(Integer obj)
        {
            NBTTagCompound ret = new NBTTagCompound();

            ret.setInteger("i", obj);

            return ret;
        }

        @Override
        public Integer deserializeNBT(NBTTagCompound nbt)
        {
            return nbt.getInteger("i");
        }
    }
}
