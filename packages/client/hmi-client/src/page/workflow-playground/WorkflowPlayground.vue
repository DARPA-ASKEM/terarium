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
            <div class="connection"></div>
            <div class="inputs"></div>
            <div class="node" :id="node.id">
                <div class="node-in" @click.stop="(event) => createConnection(event)"></div>
                <div>{{ node.name }}</div>
                <div class="node-out" @click.stop="(event) => createConnection(event)"></div>
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

.connection,
.inputs,
.node {
    flex: 1;
    height: 100px;
    min-width: 50px;
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
</style>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import Modal from '@/components/Modal.vue';
import InputText from 'primevue/inputtext';

interface Node {
    id: string,
    name: string,
    inputs?: Node[],
    outputs?: Node[]
}
interface Connection {
    in?: string,
    out?: string
}
const nodes = ref<Node[]>([]);
const modalVisible = ref(false);
const newNodeName = ref<string>('');
const input = ref<HTMLInputElement | null>(null);
const isSelectingConnection = ref(false);
const newConnection = ref<Connection>({});

async function createNode() {
    modalVisible.value = true;
    await nextTick();
    // @ts-ignore
    input.value?.$el.focus();
}

function insertNode() {
    const newNode: Node = {
        id: nodes.value.length.toString(),
        name: newNodeName.value
    };
    nodes.value.push(newNode);
    modalVisible.value = false;
    newNodeName.value = '';
}

function invalidateConnection() {
    console.warn("Invalid connection");
    newConnection.value = {};
}

function createConnection(event) {
    const clickedElement: HTMLElement = event.target;
    if (isSelectingConnection.value === false) {
        isSelectingConnection.value = true;
        switch (clickedElement.className) {
            case 'node-out':
                newConnection.value.out = clickedElement.parentElement?.id;
                break;
            case 'node-in':
                newConnection.value.in = clickedElement.parentElement?.id;
                break;
            default: break;
        }
    } else {
        switch (clickedElement.className) {
            case 'node-out':
                if (newConnection.value.out) {
                    invalidateConnection();
                } else {
                    const nodeId = clickedElement.parentElement?.id;
                    if (newConnection.value.in === nodeId) {
                        invalidateConnection();
                    } else {
                        newConnection.value.out = nodeId;
                    }
                }
                break;
            case 'node-in':
                if (newConnection.value.in) {
                    invalidateConnection();
                } else {
                    const nodeId = clickedElement.parentElement?.id;
                    if (newConnection.value.out === nodeId) {
                        invalidateConnection();
                    } else {
                        newConnection.value.in = nodeId;
                    }
                }
                break;
            default: break;
        }
        isSelectingConnection.value = false;
    }
}
</script>