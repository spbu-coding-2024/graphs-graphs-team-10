import algos.findMinimalSpanningTree
import model.Edge
import model.UndirectedGraph
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MSTTest {

    private fun toTriples(edges: List<Edge>): Set<Triple<Long, Long, Long>> {
        return edges.map { e ->
            val (u, v) = e.vertices
            Triple(u, v, e.weight)
        }.toSet()
    }

    @Test
    fun `empty graph returns empty list`() {
        val graph = UndirectedGraph()
        val mst = findMinimalSpanningTree(graph)
        assertEquals(emptyList(), mst)
    }

    @Test
    fun `single edge is MST`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 5)
        }
        val mst = findMinimalSpanningTree(graph)
        val expected = setOf(Triple(1L, 2L, 5L))
        assertEquals(expected, toTriples(mst))
    }

    @Test
    fun `triangle picks two smallest edges`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(2L, 3L, 2)
            addEdge(1L, 3L, 10)
        }
        val mst = findMinimalSpanningTree(graph)
        val expected = setOf(
            Triple(1L, 2L, 1L),
            Triple(2L, 3L, 2L)
        )
        assertEquals(expected, toTriples(mst))
    }

    @Test
    fun `disconnected graph returns forest`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(3L, 4L, 1)
        }
        val mst = findMinimalSpanningTree(graph)
        val expected = setOf(
            Triple(1L, 2L, 1L),
            Triple(3L, 4L, 1L)
        )
        assertEquals(expected, toTriples(mst))
    }

    @Test
    fun `large chain graph`() {
        val graph = UndirectedGraph()
        val expected = mutableSetOf<Triple<Long, Long, Long>>()
        val n = 100
        for (i in 0..<n) {
            graph.addEdge(i.toLong(), (i + 1).toLong(), 1)
            expected.add(Triple(i.toLong(), (i + 1).toLong(), 1L))
        }

        val mst = findMinimalSpanningTree(graph)

        assertEquals(expected, toTriples(mst))
        assertEquals(graph.vertices.size - 1, mst.size)
    }
}