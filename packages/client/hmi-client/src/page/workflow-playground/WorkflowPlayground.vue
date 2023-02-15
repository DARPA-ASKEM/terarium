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
            <div class="node"></div>
        </div>
    </div>
</template>

<style scoped>
.container {
    display: grid;
    height: 100%;
    width: 100%;
    grid-template-columns: repeat(auto-fill, 50px);
    background-color: var(--surface-ground);
}

.column {
    display: flex;
    background-color: var(--surface-secondary);
}

.node {
    background-color: var(--surface-section);
    border: 1px solid black;
    flex: 1;
}
</style>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import Modal from '@/components/Modal.vue';
import InputText from 'primevue/inputtext';

interface Node {
    name: string,
    inputs?: Node[],
    outputs?: Node[]
}
const nodes = ref<Node[]>([]);
const modalVisible = ref(false);
const newNodeName = ref<string>('');
const input = ref<HTMLInputElement | null>(null);

async function createNode() {
    modalVisible.value = true;
    await nextTick();
    // @ts-ignore
    input.value?.$el.focus();
}

function insertNode() {
    const newNode = {
        name: newNodeName.value
    } as Node;
    nodes.value.push(newNode);
    modalVisible.value = false;
}
</script>