# AlgoGraphia

This application allows you to visualize algorithms for graphs
## Functionality
- Load graph from Json, Neo4j, SQLite
- Save graph to Json, Neo4j, SQLite
#### Algorithms (for all graphs):
- Placement (Force Directed Layout)
- Find key vertices
- Shortest path between 2 vertices (Dijkstra)
- Find loops for vertex

#### Only for undirected graphs:
- Find bridges
- Find communities (Louvain)
- Minimal spanning tree (***Kruskal***)

#### Only for directed graphs:
-  Find strongly connected components
-  Shortest path between 2 vertices (Ford-Bellman)
## Quick start


```bash
  git clone https://github.com/spbu-coding-2024/graphs-graphs-team-10.git
  cd graphs-graphs-team-10/
```

Run app
```bash
  ./gradlew run
```
## Contributors

- [@ArsenijRomanov](https://github.com/ArsenijRomanov)
- [@epishkin06](https://github.com/epishkin06)
- [@K1mer0](https://www.github.com/K1mer0)


## License

This project is licensed under the [**MIT License**](LICENSE).