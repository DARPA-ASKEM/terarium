# graph-scaffolder
graph-scaffolder provides SVG/DOM scaffolding, basic boiler-plate functions, and utilities for working with compound-graphs in SVG.



Note: graph-scaffolder is _not_ a layout engine.


## Data format
The graph data is specified in the format of `{nodes, edges}` and allows for nesting, for example:

```
const graphData = {
	nodes: [
		{
      id: 'node1',
      label: 'node1 label'
      x: 100,
      y: 100,
      width: 100,
      height: 100,
      data: { value: 123 },
      nodes: []
		},
    {
      id: 'node2',
      label: 'node2 label',
      x: 400,
      y: 400,
      width: 100,
      height: 100
      data: { value: 234 },
      nodes: [
        {
          id: 'nested node1',
          label: 'nested'
          x: 10,
          y: 10,
          width: 20,
          height: 20,
          data: { value: 999 },
          nodes: []
        }
      ]
    }
  ],
  edges: [
    {
      id: '1',
      source: 'node1',
      target: 'node2',
      points: [ { x: 200, y: 150 }, { x: 400, y: 450 }],
      data: { foo: 'bar' }
    }
  ]
}
```

### Usage
For example, with Typescript


```
interface NodeData {
  value: number;
}

interface EdgeData {
	foo: string;
}

const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

class SampleRenderer extends BasicRenderer<NodeData, EdgeData> {
  renderNodes(selection: D3SelectionINode<NodeData>) {
	  selection 
			.append('rect')
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#eee')
			.style('stroke', '#888');
  }

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000');
  }
}
```

Then

```
// Initialization
const div = document.createElement('div');
div.style.height = '800px';
div.style.width = '800px';
document.body.append(div);

const renderer = new SampleRenderer({
	el: div
});

renderer.on('node-click', () => {
	console.log('node click');
});

await renderer.setData(graphData);
await renderer.render();
```



### Running examples
```
yarn install
npm run develop
```
