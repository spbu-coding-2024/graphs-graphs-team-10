//author: Roman Epishkin
package algos

import model.DirectedGraph
import model.UndirectedGraph

fun findCyclesForDirected(
    graph: DirectedGraph,
    startVertex: Long,
): List<List<Long>>  {
    val adjList = getAdjacencyList(graph)
    val visited = mutableSetOf<Long>()
    val recursionStack = mutableSetOf<Long>()
    val path = mutableListOf<Long>()
    val cycles = mutableListOf<List<Long>>()

    fun dfs(current: Long)  {
        when {
            recursionStack.contains(current) -> {
                val cycleStartIndex = path.indexOf(current)
                if (cycleStartIndex != -1) {
                    val cycle = path.subList(cycleStartIndex, path.size)
                    cycles.add(cycle.toList())
                }
                return
            }
            visited.contains(current) -> return
            else -> {
                visited.add(current)
                recursionStack.add(current)
                path.add(current)

                adjList[current]?.forEach { neighbor -> dfs(neighbor) }

                path.removeAt(path.size - 1)
                recursionStack.remove(current)
            }
        }
    }

    dfs(startVertex)

    return cycles.distinctBy { cycle ->
        val min = cycle.minOrNull() ?: 0L
        val index = cycle.indexOf(min)
        cycle.drop(index) + cycle.take(index)
    }
}

fun findCyclesForUndirected(
    graph: UndirectedGraph,
    startVertex: Long,
): List<List<Long>> {

    val adjList = getAdjacencyList(graph)
    val visited = mutableSetOf<Long>()
    val cycles = mutableListOf<List<Long>>()
    val parent = mutableMapOf<Long, Long>()

    fun dfs(current: Long) {
        visited.add(current)

        adjList[current]?.forEach { neighbor ->
            when {
                parent[current] == neighbor -> return@forEach

                visited.contains(neighbor) -> {
                    val cycle = mutableListOf<Long>()
                    var node = current
                    while (node != neighbor && node != -1L) {
                        cycle.add(node)
                        node = parent[node] ?: -1L
                    }
                    if (node != -1L) {
                        cycle.add(neighbor)
                        cycle.reverse()
                        if (cycle.size >= 3) {
                            val minIndex = cycle.indexOf(cycle.minOrNull())
                            val normalizedCycle = cycle.drop(minIndex) + cycle.take(minIndex)
                            cycles.add(normalizedCycle)
                        }
                    }
                }

                else -> {
                    parent[neighbor] = current
                    dfs(neighbor)
                }
            }
        }
    }

    if (graph.vertices.contains(startVertex)) {
        parent[startVertex] = -1L
        dfs(startVertex)
    }

    return cycles.distinctBy { it.joinToString(",") }

}
