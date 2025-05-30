//author: Roman Epishkin
import algos.findCyclesForDirected
import algos.findCyclesForUndirected
import model.DirectedGraph
import model.UndirectedGraph
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FindCyclesTest {
    @Test
    fun `test findCyclesForDirected with no cycles`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addEdge(1, 2, 0)
                addEdge(2, 3, 0)
            }

        val cycles = findCyclesForDirected(graph, 1)
        assertTrue(cycles.isEmpty())
    }

    @Test
    fun `test findCyclesForDirected with single cycle`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addEdge(1, 2, 0)
                addEdge(2, 3, 0)
                addEdge(3, 1, 0)
            }

        val cycles = findCyclesForDirected(graph, 1)
        assertEquals(1, cycles.size)
        assertTrue(cycles.any { it == listOf(1L, 2L, 3L) })
    }

    @Test
    fun `test findCyclesForDirected with multiple cycles`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addVertex(4)
                addEdge(1, 2, 0)
                addEdge(2, 3, 0)
                addEdge(3, 1, 0)
                addEdge(3, 4, 0)
                addEdge(4, 2, 0)
            }

        val cycles = findCyclesForDirected(graph, 1)
        assertEquals(2, cycles.size)
        assertTrue(cycles.any { it == listOf(1L, 2L, 3L) })
        assertTrue(cycles.any { it == listOf(2L, 3L, 4L) })
    }

    @Test
    fun `test findCyclesForDirected with self-loop`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addEdge(1, 1, 0)
            }

        val cycles = findCyclesForDirected(graph, 1)
        assertEquals(1, cycles.size)
        assertEquals(listOf(1L), cycles[0])
    }

    @Test
    fun `test findCyclesForDirected with disconnected components`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addVertex(4)
                addEdge(1, 2, 0)
                addEdge(2, 1, 0)
                addEdge(3, 4, 0)
                addEdge(4, 3, 0)
            }

        val cycles = findCyclesForDirected(graph, 1)
        assertEquals(1, cycles.size)
        assertTrue(cycles.any { it == listOf(1L, 2L) })
    }

    @Test
    fun `test findCyclesForUndirected with no cycles`() {
        val graph =
            UndirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addEdge(1, 2, 0)
                addEdge(2, 3, 0)
            }

        val cycles = findCyclesForUndirected(graph, 1)
        assertTrue(cycles.isEmpty())
    }

    @Test
    fun `test findCyclesForUndirected with single cycle`() {
        val graph =
            UndirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addEdge(1, 2, 0)
                addEdge(2, 3, 0)
                addEdge(3, 1, 0)
            }

        val cycles = findCyclesForUndirected(graph, 1)
        assertEquals(1, cycles.size)
        assertTrue(cycles.any { it.sorted() == listOf(1L, 2L, 3L).sorted() })
    }

    @Test
    fun `test findCyclesForUndirected with multiple cycles`() {
        val graph =
            UndirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addVertex(4)
                addEdge(1, 2, 0)
                addEdge(2, 3, 0)
                addEdge(3, 1, 0)
                addEdge(3, 4, 0)
                addEdge(4, 2, 0)
            }

        val cycles = findCyclesForUndirected(graph, 1)
        assertEquals(2, cycles.size)
        assertTrue(cycles.any { it.sorted() == listOf(1L, 2L, 3L).sorted() })
        assertTrue(cycles.any { it.sorted() == listOf(2L, 3L, 4L).sorted() })
    }

    @Test
    fun `test findCyclesForUndirected with tree structure`() {
        val graph =
            UndirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addVertex(4)
                addEdge(1, 2, 0)
                addEdge(1, 3, 0)
                addEdge(2, 4, 0)
            }

        val cycles = findCyclesForUndirected(graph, 1)
        assertTrue(cycles.isEmpty())
    }

    @Test
    fun `test findCyclesForUndirected with start vertex not in graph`() {
        val graph =
            UndirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addEdge(1, 2, 0)
            }

        val cycles = findCyclesForUndirected(graph, 3)
        assertTrue(cycles.isEmpty())
    }
}
