# graph-scaffolder
graph-scaffolder provides SVG/DOM scaffolding:
- SVG/DOM hierarchies + data-binding via D3
- Basic boiler-plate functions such as pan/zoom, highlighting
- Hooks for building custom events

Note: graph-scaffolder is _not_ a layout engine. It can leverage a layout function, provided that the algorithm produces output in the format below.


## Data format
The graph data is specified in the format of `{nodes, edges}` and allows for nesting, nested nodes are expected to be positioend relative to their parent node.  For example:

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
Simple renderer with Typescript

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

Then to render the graph

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

const graphData = layout_algorithm(rawData);
await renderer.setData(graphData);
await renderer.render();
```


### Renderers
graph-scaffolder comes with 2 pre-canned renderers. 
- BasicRenderer: this erases and rebuilds the graph everytime the graph topology changes.
- DeltaRenderer: this renderer tries to detect new, modified, and removed graph elevents when data changes - this allows for different rendering semantics.  


### Run examples
```
yarn install
npm run develop
```
