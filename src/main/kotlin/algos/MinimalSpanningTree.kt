package algos

import model.Edge
import model.Graph
import model.UndirectedGraph

fun findMinimalSpanningTree(graph: Graph): List<Edge> {
    if (graph !is UndirectedGraph || graph.isEmpty()) return emptyList()

    val sortedEdges = graph.edges.sortedBy { it.weight }
    val mstEdges = mutableListOf<Edge>()
    val parent = mutableMapOf<Long, Long>()
    val rank = mutableMapOf<Long, Int>()

    graph.vertices.forEach { vertex ->
        parent[vertex] = vertex
        rank[vertex] = 0
    }

    fun find(vertex: Long): Long {
        if (parent[vertex] != vertex) {
            parent[vertex] = find(parent[vertex]!!)
        }
        return parent[vertex]!!
    }

    fun union(u: Long, v: Long) {
        val rootU = find(u)
        val rootV = find(v)

        when {
            rootU == rootV -> return
            rank[rootU]!! > rank[rootV]!! -> parent[rootV] = rootU
            rank[rootV]!! > rank[rootU]!! -> parent[rootU] = rootV
            else -> {
                parent[rootV] = rootU
                rank[rootU] = rank[rootU]!! + 1
            }
        }
    }

    for (edge in sortedEdges) {
        val (u, v) = edge.vertices
        if (find(u) != find(v)) {
            mstEdges.add(edge)
            union(u, v)

            if (mstEdges.size == graph.vertices.size - 1) break
        }
    }

    return mstEdges
}