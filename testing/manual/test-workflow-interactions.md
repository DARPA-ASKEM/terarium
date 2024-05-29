## `[Workflow Interactions (add, remove, connect, disconnect, zoom, pan, drag and drop)]`
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test 
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, or open, project named `QA [Your Name] [YYMMDD]`

### 2. Test Scenarios

`[Task 1: Adding an operator to the workflow via the "+ Add a component" menu']`
1. Create a new workflow - open it.
2. Click the "+ Add a component" primary button in the top right corner
3. Select Work with mode > Stratify model
4. __Expected Result__: The Stratify model operator node should appear on the workflow canvas

`[Task 2: Adding a resource to the workflow via drag and drop from resource panel']`
1. Add a model to the project with Explorer.
2. Open the workflow created in Task 1.
3. In the Resources panel - left side of the screen - click "Model (1)" to expand it so that the model you added from Explorer is now visible
3. Click and drag that model and drag it out onto the workflow canvas.
4. __Expected Result__: The model resource node should appear on the canvas in the mouse position when the mouse button is release during click and drag.

`[Task 3: Duplicate and removing an operator from the workflow']`
1. Click the kebab menu in the top right of the model operator node, select "Duplicate" from the menu
2. Click the kebab menu in the top right of the newly added duplicate model operator node, select "Remove" from the menu
3. __Expected Result__: The model operator node should have been duplicated, then deleted from the canvas.

`[Task 4: Opening an operator drill down in a new window']`
1. Click the kebab menu in the top right of the model operator node, select "Open in new window" from the menu
2. __Expected Result__: The drill down of the model operator should open in a new window.

`[Task 5: Connecting an operator nodes to another operator node']`
1. Click the output port of the model operator node
2. Drag the pipe line to the input port of the Stratify model operator node, click mouse to release
3. __Expected Result__: The pipe should connect the output of the model operator node to the input port of the Stratify model operator node with the ends of the connection pipe attached to the ports. The input label on the Stratify node should be the same as the output port label of the mode, and an Edit button should appear in the Stratify model node.

`[Task 6: Disconnecting an operator node']`
1. Hover over the Stratify Model input port label, 
2. Click the Unlink button that appears on hover.
3. __Expected Result__: The pipe connecting the model and stratify model operator nodes should be deleted. The input label on Stratify Model should change back to "Model or Model Configuration" hint text and the edit button should disappear.

`[Task 7: Re-positioning a disconnected node on the canvas']`
1. Hover the mouse anywhere over an operator node. A hover state should be indicated by the node's header changing to a darker shade of green and the node's shadow darkening.
2. Click anywhere on the operator node, drag in somewhere else on the canvas and release the mouse button.
3. __Expected Result__: The node's hover state should trigger on mouse over and the node should be repositioned on the canvas in the position of the mouse when the mouse button is released.

`[Task 8: Re-positioning a connected node on the canvas']`
1. Repeat the steps in Task 5 to connect two nodes.
2. Repeate the steps in Task 7 to re-position one of the nodes.
3. __Expected Result__: The node should reposition as in Task 7, and the pipe connecting the nodes should flex contort to accomodate the re-position of the node.

`[Task 9: Canvas zoom in / out and pan']`
1. With the mouse positioned over the canvas, scroll the mouse wheel up and down to zoom the canvas in and out.
2. With the mouse positioned over the canvas, click and drag the mouse around.
3. __Expected Result__: The canvas view should zoom in/out on mouse wheel scroll and pan on click and drag.

### 3. End test
1. logout of the application 
