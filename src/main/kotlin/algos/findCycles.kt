package algos

import model.DirectedGraph
import model.UndirectedGraph

fun findCyclesForDirected(graph: DirectedGraph, startVertex: Long) : List<List<Long>>{
    val adjList = getAdjacencyList(graph)
    val visited = mutableSetOf<Long>()
    val recursionStack = mutableSetOf<Long>()
    val path = mutableListOf<Long>()
    val cycles = mutableListOf<List<Long>>()

    fun dfs(current : Long){
        when{
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

fun findCyclesForUndirected(graph: UndirectedGraph, startVertex: Long): List<List<Long>> {
    val adjList = getAdjacencyList(graph)
    val cycles = mutableListOf<List<Long>>()
    val path = mutableListOf<Long>()

    fun dfs(current: Long, parent: Long) {
        if (path.contains(current)) {
            val cycleStart = path.indexOf(current)
            val cycle = path.subList(cycleStart, path.size)
            if (cycle.contains(startVertex) && cycle.size >= 3) {
                cycles.add(cycle.toList() + current)
            }
            return
        }

        path.add(current)

        adjList[current]?.forEach { neighbor ->
            if (neighbor != parent) {
                dfs(neighbor, current)
            }
        }

        path.removeAt(path.size - 1)
    }

    if (graph.vertices.any { it == startVertex }) {
        dfs(startVertex, -1)
    }

    return cycles.distinctBy { cycle ->
        val min = cycle.minOrNull() ?: 0L
        val index = cycle.indexOf(min)
        cycle.drop(index) + cycle.take(index)
    }
}