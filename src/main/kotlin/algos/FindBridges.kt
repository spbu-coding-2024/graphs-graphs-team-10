package algos

import model.Edge
import model.Graph
import model.UndirectedGraph

fun findBridges(graph: Graph): List<Edge> {
    if (graph !is UndirectedGraph) return emptyList() 

    val discoveryTimes = mutableMapOf<Long, Int>()
    val lowValues = mutableMapOf<Long, Int>()
    val visited = mutableSetOf<Long>()
    var time = 0
    val bridges = mutableListOf<Edge>()

    fun dfs(current: Long, parent: Long) {
        visited.add(current)
        discoveryTimes[current] = time
        lowValues[current] = time
        time++

        val neighbors = graph.edges
            .filter { it.vertices.first == current || it.vertices.second == current }
            .map { if (it.vertices.first == current) it.vertices.second else it.vertices.first }

        neighbors.forEach { neighbor ->
            when {
                neighbor == parent -> return@forEach
                neighbor !in visited -> {
                    dfs(neighbor, current)
                    lowValues[current] = minOf(lowValues[current]!!, lowValues[neighbor]!!)
                    if (lowValues[neighbor]!! > discoveryTimes[current]!!) {
                        graph.findEdge(current, neighbor)?.let { bridges.add(it) }
                    }
                }
                else -> {
                    lowValues[current] = minOf(lowValues[current]!!, discoveryTimes[neighbor]!!)
                }
            }
        }
    }

    graph.vertices.forEach { vertex ->
        if (vertex !in visited) {
            dfs(vertex, -1)
        }
    }

    return bridges
}