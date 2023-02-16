<template>
    <Teleport to="body">
        <Modal v-if="modalVisible" @modal-mask-clicked="modalVisible = false">
            <template #default>
                <InputText ref="input" id="new-node-name" type="text" v-model="newNodeName"
                    @keyup.enter="insertNode()" />
            </template>
        </Modal>
    </Teleport>
    <div class="container" @click="createNode()">
        <div class="column" v-for="node in nodes">
            <div class="junction">
                <div :style="calcJunctionSize(node)"></div>
            </div>
            <div class="inputs">
                <div class="wire" :style="`height: ${wireSize}px`" v-for="out in node.inputs">
                </div>
            </div>
            <div class="node" :id="node.id">
                <div class="node-in" @click.stop="(event) => nodeSelected(event)"></div>
                <div>{{ node.name }}</div>
                <div class="node-out" @click.stop="(event) => nodeSelected(event)"></div>
            </div>
            <div class="outputs">
                <div class="wire" :style="`height: ${wireSize}px`" v-for="out in node.outputs">
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
    grid-auto-columns: auto;
    background-color: var(--surface-ground);
}

.column {
    display: flex;
    background-color: var(--surface-secondary);
    grid-row-start: 1;
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
    align-items: center;
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

.wire {
    /* height: 1px; */
    width: 100%;
    background-color: var(--gray-500);
}

.junction {
    display: flex;
    align-items: center;
    flex: 0;
}

.junction div {
    background-color: var(--gray-900);
}
</style>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import Modal from '@/components/Modal.vue';
import InputText from 'primevue/inputtext';

interface Node {
    id: string,
    name: string,
    inputs: Node[],
    outputs: Node[]
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
const wireSize = 2;

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
        outputs: []
    };
    nodes.value.push(newNode);
    modalVisible.value = false;
    newNodeName.value = '';
}

function invalidateConnection(reason?: string) {
    console.warn(`Invalid connection - ${reason}`);
    newConnection.value = {};
}

function createConnection() {
    if (newConnection.value.out && newConnection.value.in) {
        newConnection.value.in?.inputs.push(newConnection.value.out);
        newConnection.value.out?.outputs.push(newConnection.value.in);
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
                        createConnection();
                    }
                }
                break;
            default: break;
        }
        isSelectingConnection.value = false;
    }
}

function calcJunctionSize(node: Node) {
    const size = `${Math.max(node.inputs.length, node.outputs.length) * wireSize}px`;
    const style = { width: size, height: size };
    return style;
}
</script>