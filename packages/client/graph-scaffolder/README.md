# graph-scaffolder
Given a layout/adapter function and data, graph-scaffolder creates the basic SVG structures and interaction hooks for interactive management of graph data.

graph-scaffolder is _not_ a layout engine, rather, it abstracts away boilder-plate code like zooming, panning, and provides a unified interface.


## Data format
The graph data is specified in the format of `{nodes, edges}` and allows for nesting, for example:
```
{
	nodes: [
		{
		}
  ],
  edges: [
    id: 'e1',
    source: 'node1',
    target: 'node2'
  ]
}
```



### Running examples
```
npm run develop
```
