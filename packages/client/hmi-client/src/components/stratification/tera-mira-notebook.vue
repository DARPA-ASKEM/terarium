<template>
	<h4>Code Editor</h4>
	<Textarea class="codeBox" v-model="codeAsText" />
	<Button label="Run" @click="hitMira" />
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import { SessionContext } from '@jupyterlab/apputils/lib/sessioncontext';
import { newSession, JupyterMessage } from '@/services/jupyter';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';

const INITIAL_CODE =
	"from mira.metamodel import ControlledConversion, NaturalConversion, Concept, Template \n infected = Concept(name='infected population', identifiers={'ido': '0000511'}) \n susceptible = Concept(name='susceptible population', identifiers={'ido': '0000514'}) \n immune = Concept(name='immune population', identifiers={'ido': '0000592'}) \n \n t1 = ControlledConversion(controller=infected, subject=susceptible, outcome=infected,) \n t2 = NaturalConversion(subject=infected, outcome=immune) \n t1.dict() \n ";

const codeAsText = ref(INITIAL_CODE);
const jupyterSession: SessionContext = await newSession('beaker_kernel', 'Beaker Kernel');

async function hitMira() {
	if (!jupyterSession) return;

	console.log('Trying to hit mira');
	console.log(codeAsText);
	const messageBody = {
		session: jupyterSession.session?.name || '',
		channel: 'shell',
		content: codeAsText as any,
		msgType: 'context_setup_request',
		msgId: 'tgpt-context_setup_request'
	};
	const message: JupyterMessage = createMessage(messageBody);
	console.log(jupyterSession.session);
	const kernel = jupyterSession.session?.kernel as IKernelConnection;
	kernel?.sendJupyterMessage(message);
	console.log(jupyterSession.session);
}

onUnmounted(() => {
	if (!jupyterSession) return;
	jupyterSession.shutdown();
});
</script>

<style scoped>
.codeBox {
	width: 100%;
	height: 50%;
}
</style>
