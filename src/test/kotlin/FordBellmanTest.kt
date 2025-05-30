//author: Roman Epishkin
import model.DirectedGraph
import model.fordBellman
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FordBellmanTest {
    @Test
    fun `test shortest path in simple graph`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addEdge(1, 2, 1)
                addEdge(2, 3, 2)
                addEdge(1, 3, 4)
            }

        val result = fordBellman(graph, 1, 3)
        assertEquals(listOf(1L, 2L, 3L), result)
    }

    @Test
    fun `test no path exists`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addEdge(1, 2, 1)
                // No edge from 2 to 3
            }

        val result = fordBellman(graph, 1, 3)
        assertNull(result)
    }

    @Test
    fun `test start equals end`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addEdge(1, 2, 1)
            }

        val result = fordBellman(graph, 1, 1)
        assertEquals(listOf(1L), result)
    }

    @Test
    fun `test negative weights without negative cycle`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addEdge(1, 2, 2)
                addEdge(2, 3, -1)
                addEdge(1, 3, 4)
            }

        val result = fordBellman(graph, 1, 3)
        assertEquals(listOf(1L, 2L, 3L), result)
    }

    @Test
    fun `test graph with negative cycle`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addEdge(1, 2, 1)
                addEdge(2, 3, -3)
                addEdge(3, 1, 1)
            }

        val result = fordBellman(graph, 1, 3)
        assertNull(result)
    }

    @Test
    fun `test start or end vertex not in graph`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addEdge(1, 2, 1)
            }

        assertNull(fordBellman(graph, 1, 3))
        assertNull(fordBellman(graph, 3, 1))
    }

    @Test
    fun `test multiple paths with different weights`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addVertex(4)
                addEdge(1, 2, 1)
                addEdge(2, 3, 1)
                addEdge(3, 4, 1)
                addEdge(1, 4, 5)
            }

        val result = fordBellman(graph, 1, 4)
        assertEquals(listOf(1L, 2L, 3L, 4L), result)
    }

    @Test
    fun `test disconnected graph`() {
        val graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addVertex(4)
                addEdge(1, 2, 1)
                addEdge(3, 4, 1)
            }

        assertNull(fordBellman(graph, 1, 4))
    }
}
