<template>
    <Teleport to="body">
        <Modal v-if="modalVisible" @modal-mask-clicked="modalVisible = false">
            <template #default>
                <InputText ref="input" id="new-node-name" type="text" v-model="newNodeName" @keyup.enter="insertNode()" />
            </template>
        </Modal>
    </Teleport>
    <div class="container" @click="createNode()">
        <div class="column" v-for="node in nodes" :style="node.gridStyle">
            <div class="inputs">
                <div class="edge" :style="inputEdgeStyle(node, input)" v-for="(input, inIndex) in node.inputs">
                </div>
            </div>
            <div class="node" :id="node.id">
                <div class="node-in" @click.stop="(event) => nodeSelected(event)"></div>
                <div>{{ node.name }} {{ node.gridStyle }} {{ node.root?.name }}</div>
                <div class="node-out" @click.stop="(event) => nodeSelected(event)"></div>
            </div>
            <div class="outputs">
                <div class="edge" :style="outputEdgeStyle(node, output, outIndex, node.outputs.length)"
                    v-for="(output, outIndex) in node.outputs">
                </div>
            </div>
        </div>
</div>
</template>

<style scoped>
.container {
    display: grid;
    height: 100%;
    width: 100%;
    /* grid-auto-columns: auto; */
    /* grid-auto-rows: auto; */
    background-color: var(--surface-ground);
    grid-template-rows: repeat(auto-fill, 200px);
}

.column {
    display: flex;
    background-color: var(--surface-secondary);
    /* grid-row: 1; */
    /* grid-column: 1; */
}

.junction,
.inputs,
.node,
.outputs {
    flex: 1;
    height: 100px;
}

.inputs,
.outputs {
    display: flex;
    justify-content: center;
    flex-direction: column;
}

.node {
    background-color: var(--surface-section);
    border: 1px solid black;
    flex: 1;
    display: flex;
}

.node div {
    display: flex;
    justify-content: center;
    align-items: center;
    flex: 1;
    height: 100%;
    width: 100%;
}

.node div:hover {
    background-color: var(--surface-secondary);
}

.node-in,
.node-out {
    min-width: 10px;
}

.edge {
    position: relative;
    width: 100%;
}

/* .junction {
    display: flex;
    align-items: center;
    flex: 0;
}

.junction div {
    background-color: var(--gray-900);
} */
</style>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import Modal from '@/components/Modal.vue';
import InputText from 'primevue/inputtext';

interface Node {
    id: string,
    name: string,
    inputs: Node[],
    outputs: Node[],
    gridStyle: {
        gridRow?: number,
        gridColumn: number,
    },
    root?: Node
}
interface Connection {
    in?: Node,
    out?: Node
}
const nodes = ref<Node[]>([]);
const modalVisible = ref(false);
const newNodeName = ref<string>('');
const input = ref<HTMLInputElement | null>(null);
const isSelectingConnection = ref(false);
const newConnection = ref<Connection>({});

function isRoot(node: Node) {
    return (node.root?.id === node.id);
}

async function createNode() {
    isSelectingConnection.value = false;
    newConnection.value = {};

    modalVisible.value = true;
    await nextTick();
    // @ts-ignore
    input.value?.$el.focus();
}

function insertNode() {
    const newNode: Node = {
        id: nodes.value.length.toString(),
        name: newNodeName.value,
        inputs: [],
        outputs: [],
        gridStyle: {
            // gridRow: 1,
            gridColumn: 1
        }
    };
    nodes.value.push(newNode);
    modalVisible.value = false;
    newNodeName.value = '';
    updateNodeInitialPosition();
}

function invalidateConnection(reason?: string) {
    console.info(`Invalid connection - ${reason}`);
    newConnection.value = {};
}

function iterativelyUpdateNodePositions(node: Node) {
    // console.log(`current node ${node.name}`);
    const rootNodeRow = node.root?.gridStyle.gridRow ?? 1;
    // console.log(node.root);
    const previousNode = (node.inputs.length >= 1) ? node.inputs[0] : null;
    // console.log(`previous node ${previousNode?.name}`);
    previousNode?.outputs.forEach((n, index) => {
        // console.log(`${n.name} gridRow = ${rootNodeRow} + ${index}`);
        n.gridStyle.gridRow = rootNodeRow + index;
    });
    // console.log(`${node.name} gridColumn = ${previousNode?.gridStyle.gridColumn} + 1`);
    node.gridStyle.gridColumn = (previousNode) ? previousNode.gridStyle.gridColumn + 1 : 1;
    const nextNode = (node.outputs.length >= 1) ? node.outputs[0] : null;
    if (nextNode) {
        iterativelyUpdateNodePositions(nextNode);
    }
    // console.log('====');
}

function updateNodeInitialPosition() {
    nodes.value.filter(n =>
        n.inputs.length === 0
    ).forEach((n, index) => {
        n.gridStyle.gridRow = index + 1;
    })
    nodes.value.forEach(n => {
        if (isRoot(n) && n.outputs.length > 0) {
            n.outputs.forEach(n => iterativelyUpdateNodePositions(n));
        }
    });
}

function isCircularConnection() {
    return false;
}

function iterativelyUpdateRoots(root: Node, nodes: Node[]) {
    nodes.forEach(n => {
        n.root = root;
        iterativelyUpdateRoots(root, n.outputs);
    })
}

function createConnection() {
    const leftNode = newConnection.value.out;
    const rightNode = newConnection.value.in;
    if (leftNode && rightNode) {
        rightNode.inputs.push(leftNode);
        leftNode.outputs.push(rightNode);
        if (leftNode.inputs.length === 0) {
            leftNode.root = leftNode;
        }
        if (!(rightNode.root && rightNode.root.id !== rightNode.id)) {
            rightNode.root = leftNode.root;
        }
        iterativelyUpdateRoots(rightNode.root!, rightNode.outputs);
        updateNodeInitialPosition();
    }
    newConnection.value = {};
}

function getNodeById(id: string | undefined): Node | undefined {
    if (!id) {
        return undefined;
    }
    return nodes.value.find(n => n.id === id);
}

function nodeSelected(event) {
    const clickedElement: HTMLElement = event.target;
    // console.log(clickedElement.parentElement?.parentElement?.style);
    if (isSelectingConnection.value === false) {
        isSelectingConnection.value = true;
        switch (clickedElement.className) {
            case 'node-out':
                newConnection.value.out = getNodeById(clickedElement.parentElement?.id);
                break;
            case 'node-in':
                newConnection.value.in = getNodeById(clickedElement.parentElement?.id);
                break;
            default: break;
        }
    } else {
        switch (clickedElement.className) {
            case 'node-out':
                if (newConnection.value.out) {
                    invalidateConnection("Can't connect output to output");
                } else {
                    const clickedNodeId = clickedElement.parentElement?.id;
                    if (newConnection.value.in?.id === clickedNodeId) {
                        invalidateConnection("Can't connect node to itself");
                    } else {
                        const clickedNode = getNodeById(clickedNodeId);
                        if (clickedNode?.outputs.find(n => n.id === newConnection.value.in?.id)) {
                            invalidateConnection("Connection already exists");
                        }
                        newConnection.value.out = getNodeById(clickedNodeId);
                        if (isCircularConnection()) {
                            invalidateConnection("Cannot create circular connection");
                        }
                        createConnection();
                    }
                }
                break;
            case 'node-in':
                if (newConnection.value.in) {
                    invalidateConnection("Can't connect input to input");
                } else {
                    const clickedNodeId = clickedElement.parentElement?.id;
                    if (newConnection.value.out?.id === clickedNodeId) {
                        invalidateConnection("Can't connect node to itself");
                    } else {
                        const clickedNode = getNodeById(clickedNodeId);
                        if (clickedNode?.inputs.find(n => n.id === newConnection.value.out?.id)) {
                            invalidateConnection("Connection already exists");
                        }
                        newConnection.value.in = getNodeById(clickedNodeId);
                        if (isCircularConnection()) {
                            invalidateConnection("Cannot create circular connection");
                        }
                        createConnection();
                    }
                }
                break;
            default: break;
        }
        isSelectingConnection.value = false;
    }
}

function inputEdgeStyle(node: Node, input: Node) {
    const rowOffset = ((node.gridStyle.gridRow ?? 1) - (input.gridStyle.gridRow ?? 0));
    const height = 5;
    // const top = -height + 5 * rowOffset;
    const top = 0;
    const borderLeft = (rowOffset > 0) ? `1px solid var(--gray-500)` : `none`;
    return {
        height: `${height}px`,
        top: `${top}%`,
        "border-top": `1px solid var(--gray-500)`,
        "border-left": `none`
    }
}

function outputEdgeStyle(node: Node, output: Node, index: number, numEdges: number) {
    const height = 5;
    const top = 2.5 * (numEdges - 1) + index * 5;
    return {
        top: `${top}px`,
        height: `${height}px`,
        "border-top": `1px solid var(--gray-500)`
    }
}

// function calcJunctionSize(node: Node) {
//     const size = `${Math.max(node.inputs.length, node.outputs.length) * wireSize}px`;
//     const style = { width: size, height: size };
//     return style;
// }
</script>