import { EventEmitter, EventName, EventCallback } from '@/utils/emitter';
import { SessionContext } from '@jupyterlab/apputils';
import { ServerConnection, KernelManager, KernelSpecManager, SessionManager } from '@jupyterlab/services';
import { CodeMirrorMimeTypeService } from '@jupyterlab/codemirror';
import { standardRendererFactories as initialFactories, RenderMimeRegistry } from '@jupyterlab/rendermime';
import { JSONObject } from '@lumino/coreutils';
import * as messages from '@jupyterlab/services/lib/kernel/messages';
// import * as kernel from '@jupyterlab/services/lib/kernel/kernel';
import { KernelConnection as JupyterKernelConnection } from '@jupyterlab/services/lib/kernel';
import API from '@/api/api';
import { v4 as uuidv4 } from 'uuid';
import { createMessage as createMessageWrapper } from '@jupyterlab/services/lib/kernel/messages';
import { IKernelConnection } from '@jupyterlab/services/lib/kernel/kernel';
import { CsvAsset } from '@/types/Types';

declare module '@jupyterlab/services/lib/kernel/messages' {
	export function createMessage(options: JSONObject): JupyterMessage;
}

declare module '@jupyterlab/services/lib/kernel/kernel' {
	interface IKernelConnection {
		sendJupyterMessage<T extends JupyterMessage>(msg: T): void;
	}
}

// declare module '@jupyterlab/services' {
// 	class KernelConnection extends JupyterKernelConnection implements kernel.IKernelConnection {
// 		sendJupyterMessage<T extends JupyterMessage>(msg: T): void;
// 		sendShellMessage<T extends JupyterMessage>(msg: T): void;
// 	}
// }

// @ts-ignore: prototype stuff
JupyterKernelConnection.prototype.sendJupyterMessage = function (msg) {
	return this.sendShellMessage(msg);
};

export type JupyterMessageType =
	| 'code_cell'
	| 'save_dataset_request'
	| 'save_dataset_response'
	| 'download_request'
	| 'download_response'
	| 'dataset'
	| 'model_preview'
	| 'visualization'
	| 'llm_thought'
	| 'llm_request'
	| 'llm_response'
	| 'compile_expr_request'
	| 'save_amr_request'
	| 'construct_amr_request'
	| 'compile_expr_response'
	| 'save_amr_response'
	| 'construct_amr_response'
	| messages.MessageType;

export interface IJupyterHeader<T extends JupyterMessageType> {
	/**
	 * ISO 8601 timestamp for when the message is created
	 */
	date: string;
	/**
	 * Message id, typically UUID, must be unique per message
	 */
	msg_id: string;
	/**
	 * Message type
	 */
	msg_type: T;
	/**
	 * Session id, typically UUID, should be unique per session.
	 */
	session: string;
	/**
	 * The user sending the message
	 */
	username: string;
	/**
	 * The message protocol version, should be 5.1, 5.2, 5.3, etc.
	 */
	version: string;
}
// Lower case staes to match the naming in the messages.
export enum KernelState {
	unknown = 'unknown',
	starting = 'starting',
	idle = 'idle',
	busy = 'busy',
	terminating = 'terminating',
	restarting = 'restarting',
	autorestarting = 'autorestarting',
	dead = 'dead'
}

export interface IJupyterMessageContent {
	request?: string;
	response?: string;
	text?: string;
	declaration?: any;
	code?: string;
	language?: string;
	data?: any;
}

export interface IJupyterMessage<T extends JupyterMessageType = JupyterMessageType> {
	/**
	 * An optional list of binary buffers.
	 */
	buffers?: (ArrayBuffer | ArrayBufferView)[];
	/**
	 * The channel on which the message is transmitted.
	 */
	channel: messages.Channel;
	/**
	 * The content of the message.
	 */
	content: IJupyterMessageContent | any;
	/**
	 * The message header.
	 */
	header: IJupyterHeader<T>;
	/**
	 * Metadata associated with the message.
	 */
	metadata: JSONObject;
	/**
	 * The parent message
	 */
	parent_header: messages.IHeader | any;
}

export interface INotebookItem {
	query_id: string;
	query: string | null;
	timestamp: string;
	messages: JupyterMessage[];
	resultingCsv: CsvAsset | null;
	executions: any[];
	autoRun?: boolean;
	selected: boolean;
}

export declare type JupyterMessage = IJupyterMessage | messages.Message;

// TODO: These settings should be pulled from the environment variables or appropriate config setup.
let serverSettings;
export const getServerSettings = () => serverSettings;
let kernelManager;
export const getKernelManager = () => kernelManager;

let specsManager;
export const getSpecsManager = () => specsManager;

let sessionManager;
export const getSessionManager = () => sessionManager;

export const mimeService = new CodeMirrorMimeTypeService();

export const renderMime = new RenderMimeRegistry({ initialFactories });
let initialized = false;

export const createMessageId = (msgType) => {
	// const timestamp = Date
	const uuid = uuidv4().replaceAll('-', '').slice(0, 16);
	return `tgpt-${uuid}-${msgType}`;
};

export const newSession = async (kernelName: string, name: string) => {
	if (!initialized) {
		const settingsResponse = await API.get('/tgpt/configuration');
		const settings = settingsResponse.data;

		serverSettings = ServerConnection.makeSettings(settings);
		kernelManager = new KernelManager({
			serverSettings
		});
		sessionManager = new SessionManager({
			kernelManager,
			serverSettings
		});
		specsManager = new KernelSpecManager({
			serverSettings
		});
		initialized = true;
	}

	const sessionContext = new SessionContext({
		sessionManager,
		specsManager,
		name,
		kernelPreference: {
			name: kernelName
		}
	});

	sessionContext.initialize();
	return sessionContext;
};

export interface IMessageHistory {
	message: string;
	messageType: string;
}

class KernelMessage extends EventEmitter {
	// Make fluent interface: https://en.wikipedia.org/wiki/Fluent_interface
	register(eventName: EventName, fn: EventCallback): KernelMessage {
		this.on(eventName, fn);
		return this;
	}
}

/**
 * Provide a higher level interface for interacting with jupyter-kernel session
 *
 * Usage:
 *   const manager = new KernelSessionManager(newSession('beaker_kernel', 'Beaker Kernel'));
 *   await manager.init('beaker_kernel', 'Beaker Kernel', ...)
 *   const msgHandle = manager.sendMessage( ... )
 *   msgHandle.register('response_1', ...)
 *   msgHandle.register('response_2', ...)
 * */
export class KernelSessionManager {
	map: Map<string, KernelMessage>;

	jupyterSession: SessionContext | null;

	constructor() {
		this.map = new Map();
		this.jupyterSession = null;
	}

	async init(kernelName: string, name: string, context: any) {
		const session = await newSession(kernelName, name);

		// Dispatch
		const iopubMessageHandler = (_session: any, message: any) => {
			const msgType = message.header.msg_type;
			const msgId = message.parent_header.msg_id;

			if (this.map.has(msgId)) {
				this.map.get(msgId)?.emit(msgType, message);
			}
		};

		const anyMessageHandler = (_session: any, message: any) => {
			// const msgDirection = message.direction;
			const msgType = `any_${message.msg.header.msg_type}`;
			const msgId = message.msg.parent_header.msg_id;

			if (this.map.has(msgId)) {
				this.map.get(msgId)?.emit(msgType, message);
			}
		};

		// Assign
		this.jupyterSession = session;

		// Setup context
		session.kernelChanged.connect((_context, kernelInfo) => {
			if (!kernelInfo.newValue) return;

			const sessionKernel = kernelInfo.newValue as IKernelConnection;
			if (sessionKernel.name === kernelName) {
				sessionKernel.anyMessage.connect(anyMessageHandler);
				session.iopubMessage.connect(iopubMessageHandler); // TODO: May want to replace iopubMessageHandler with anyMessageHandler
				const messageBody = {
					session: session.name || '',
					channel: 'shell',
					content: context,
					msgType: 'context_setup_request',
					msgId: createMessageId('context_setup')
				};
				sessionKernel.sendJupyterMessage(createMessageWrapper(messageBody));
			}
		});

		// A bit of a hacky way to wait for the kernel to be setup in order
		// to send custom messages
		const interval = 100;
		const threshold = 100;
		return new Promise((resolve, reject) => {
			let counter = 0;
			const id = setInterval(() => {
				console.log('waiting...', this.jupyterSession?.session?.kernel);
				counter++;
				if (this.jupyterSession?.session?.kernel) {
					window.addEventListener('beforeunload', this.shutdown.bind(this)); // bind(this) ensures that this instance of the class is used
					clearInterval(id);
					resolve(true);
				}
				if (counter > threshold) {
					clearInterval(id);
					reject(new Error(`Failed to connect to jupyter session after ${interval * threshold}ms`));
				}
			}, interval);
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
		const contextMessage = createMessageWrapper(messageBody);
		const sessionKernel = this.jupyterSession.session?.kernel as IKernelConnection;
		if (sessionKernel) {
			const kernelMessage = new KernelMessage();
			this.map.set(msgId, kernelMessage);
			sessionKernel.sendJupyterMessage(contextMessage);
			return kernelMessage;
		}
		throw new Error(`Unable to send message ${msgType}: ${messageBody}`);
	}

	disposeMessage(msgId: string) {
		this.map.delete(msgId);
	}

	shutdown() {
		window.removeEventListener('beforeunload', this.shutdown.bind(this)); // bind(this) ensures that this instance of the class is used
		this.jupyterSession?.shutdown();
	}
}
