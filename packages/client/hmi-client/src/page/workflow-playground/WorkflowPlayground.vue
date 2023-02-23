<template>
    <Teleport to="body">
        <Modal v-if="modalVisible" @modal-mask-clicked="modalVisible = false">
            <template #default>
                <InputText ref="input" id="new-node-name" type="text" v-model="newNodeName" @keyup.enter="insertNode()" />
            </template>
        </Modal>
    </Teleport>
    <div class="container" @click.stop="clickBackground()">
        <div class="node" :style="calcNodeStyle(node)" v-for="node in nodes" @click.stop="dragNode(node)">
            <div class="port-left" @click.stop="createNodePath(node, -1)"></div>
            {{ node.name }}
            <div class="port-right" @click.stop="createNodePath(node, 1)"></div>
        </div>
        <svg stroke="black" stroke-width="1" v-for="path in paths">
            <path :d="drawPath(path)"></path>
        </svg>
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
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.port-left,
.port-right {
    height: 8px;
    width: 8px;
    border-radius: 4px;
    position: relative;
    border: 1px solid black;
    background-color: var(--surface-section);
}

.port-left:hover,
.port-right:hover {
    height: 16px;
    width: 16px;
    border-radius: 8px;
}

.port-left {
    left: -4px;
}

.port-left:hover {
    left: -8px;
}

.port-right {
    left: 4px;
}

.port-right:hover {
    left: 8px;
}


svg {
    position: absolute;
    height: 100%;
    width: 100%;
}
</style>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue';
import Modal from '@/components/Modal.vue';
import InputText from 'primevue/inputtext';

interface Position {
    x: number,
    y: number
}

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
    position: Position
}

interface Path {
    startPosition: Position,
    endPosition?: Position
}

const modalVisible = ref(false);
const newNodeName = ref<string>('');
const input = ref<HTMLInputElement | null>(null);
const nodes = ref<Node[]>([]);
const paths = ref<Path[]>([])
const isCreatingNodePath = ref(false);
const activeNodePathIndex = ref<number>(0);

const mouseX = ref(0);
const mouseY = ref(0);

function clickBackground() {
    if (isCreatingNodePath.value) {
        paths.value = paths.value.filter((p, i) => i !== activeNodePathIndex.value);
        isCreatingNodePath.value = false;
    } else {
        createNode();
    }
}

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
        },
        position: {
            x: 50,
            y: 50
        }
    } as Node);
    newNodeName.value = '';
}

function dragNode(node: Node) {
    node.style.top = `${mouseY.value - 57 - 50}px`;
    node.style.left = `${mouseX.value - 56 - 50}px`;
    node.position.x = mouseX.value - 56;
    node.position.y = mouseY.value - 57;
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

function createNodePath(node: Node, direction: number) {
    if (!isCreatingNodePath.value) {
        const newPath: Path = {
            startPosition: {
                x: node.position.x + (50 * direction),
                y: node.position.y
            },
        }
        paths.value.push(newPath);
        activeNodePathIndex.value = paths.value.length;
        isCreatingNodePath.value = true;
    }
}

function drawPath(path: Path) {
    if (isCreatingNodePath.value) {
        return `M ${path.startPosition.x} ${path.startPosition.y}
        L ${mouseX.value - 56} ${mouseY.value - 57}`;
    } else {
        return `M 0 0 L 0 0`;
    }
}

function mouseUpdate(event) {
    mouseX.value = event.pageX
    mouseY.value = event.pageY
}

onMounted(() => window.addEventListener('mousemove', mouseUpdate));
onUnmounted(() => window.removeEventListener('mousemove', mouseUpdate));
</script>