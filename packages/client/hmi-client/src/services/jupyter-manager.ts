/**
 * Provide a higher level interface for interacting with jupyter-kernel
 *
 * Usage:
 *   const manager = new KernelSessionManager(newSession('beaker', 'Beaker'));
 * */
import { SessionContext } from '@jupyterlab/apputils';
import { createMessage } from '@jupyterlab/services/lib/kernel/messages';
import { EventEmitter } from '@/utils/emitter';
import { newSession, createMessageId } from './jupyter';

class KernelMessage extends EventEmitter {}

export class KernelSessionManager {
	map: Map<string, KernelMessage>;

	jupyterSession: SessionContext | null;

	constructor() {
		this.map = new Map();
		this.jupyterSession = null;
	}

	async init(kernelName: string, name: string, context: any) {
		const session = await newSession(kernelName, name);

		const iopubMessageHandler = (_session: any, message: any) => {
			if (message.header.msg_type === 'status') {
				return;
			}
			const msgType = message.header.msg_type;
			const msgId = message.parent_header.msg_id;

			if (this.map.has(msgId)) {
				this.map.get(msgId)?.emit(msgType, message);
			}
		};

		// Setup context
		session.kernelChanged.connect((_context, kernelInfo) => {
			if (!kernelInfo.newValue) return;

			const sessionKernel = kernelInfo.newValue;
			if (sessionKernel.name === kernelName) {
				session.iopubMessage.connect(iopubMessageHandler);

				const messageBody = {
					session: session.name || '',
					channel: 'shell',
					content: context,
					msgType: 'context_setup_request',
					msgId: createMessageId('context_setup')
				};
				sessionKernel.sendJupyterMessage(createMessage(messageBody));
			}

			// Assign
			this.jupyterSession = session;
		});
	}

	sendMessage(msgType: string, messageContent: any) {
		if (!this.jupyterSession) throw new Error('Session not ready');

		const msgId = createMessageId(msgType);
		const messageBody = {
			session: this.jupyterSession.name || '',
			channel: 'shell',
			content: messageContent,
			msgType,
			msgId
		};
		const contextMessage = createMessage(messageBody);
		const sessionKernel = this.jupyterSession.session?.kernel;
		if (sessionKernel) {
			const kernelMessage = new KernelMessage();
			this.map.set(msgId, kernelMessage);
			sessionKernel.sendJupyterMessage(contextMessage);
			return kernelMessage;
		}
		return null;
	}

	disposeMessage(msgId: string) {
		this.map.delete(msgId);
	}
}
