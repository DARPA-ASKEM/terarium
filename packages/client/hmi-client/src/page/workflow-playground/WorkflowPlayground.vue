<template>
    <Teleport to="body">
        <Modal v-if="modalVisible" @modal-mask-clicked="modalVisible = false">
            <template #default>
                <InputText ref="input" id="new-node-name" type="text" v-model="newNodeName" @keyup.enter="insertNode()" />
            </template>
        </Modal>
    </Teleport>
    <div class="container" @click.stop="createNode()">
        <div class="node" :style="calcNodeStyle(node)" v-for="node in nodes" @click.stop="dragNode(node)">
        </div>
</div>
</template>

<style scoped>
.container {
    position: relative;
    height: 100%;
    width: 100%;
}

.node {
    position: absolute;
    height: 100px;
    width: 100px;
    background-color: var(--surface-section);
    border: 1px solid var(--gray-500);
    border-radius: 4px;
}
</style>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue';
import Modal from '@/components/Modal.vue';
import InputText from 'primevue/inputtext';

interface Node {
    id: number,
    name: string,
    inputs?: Node[],
    outputs?: Node[],
    style: {
        top: string,
        left: string
    }
    isDragging: boolean
}
const modalVisible = ref(false);
const newNodeName = ref<string>('');
const input = ref<HTMLInputElement | null>(null);
const nodes = ref<Node[]>([]);

const mouseX = ref(0);
const mouseY = ref(0);

async function createNode() {
    modalVisible.value = true;
    await nextTick();
    input.value?.$el.focus();
}

function insertNode() {
    modalVisible.value = false;
    nodes.value.push({
        id: nodes.value.length,
        name: newNodeName.value,
        style: {
            top: `0px`,
            left: `0px`
        }
    } as Node)
}

function dragNode(node: Node) {
    node.style.top = `${mouseY.value - 57 - 50}px`;
    node.style.left = `${mouseX.value - 56 - 50}px`;
    node.isDragging = !node.isDragging;

}

function calcNodeStyle(node: Node) {
    if (!node.isDragging) {
        return node.style;
    }
    const top = mouseY.value - 57 - 50;
    const left = mouseX.value - 56 - 50;
    return {
        top: `${top}px`,
        left: `${left}px`
    }
}

function mouseUpdate(event) {
    mouseX.value = event.pageX
    mouseY.value = event.pageY
}

onMounted(() => window.addEventListener('mousemove', mouseUpdate));
onUnmounted(() => window.removeEventListener('mousemove', mouseUpdate));
</script>