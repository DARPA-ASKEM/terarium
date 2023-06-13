import { SessionContext } from '@jupyterlab/apputils';
import {
	ServerConnection,
	KernelManager,
	KernelSpecManager,
	SessionManager
} from '@jupyterlab/services';
import { CodeMirrorMimeTypeService } from '@jupyterlab/codemirror';
import {
	standardRendererFactories as initialFactories,
	RenderMimeRegistry
} from '@jupyterlab/rendermime';
import { JSONObject } from '@lumino/coreutils';
import * as messages from '@jupyterlab/services/lib/kernel/messages';
import * as kernel from '@jupyterlab/services/lib/kernel/kernel';
import { KernelConnection as JupyterKernelConnection } from '@jupyterlab/services/lib/kernel';

declare module '@jupyterlab/services/lib/kernel/messages' {
	export function createMessage(options: JSONObject): JupyterMessage;
}

declare module '@jupyterlab/services/lib/kernel/kernel' {
	interface IKernelConnection {
		sendJupyterMessage<T extends JupyterMessage>(msg: T): void;
		sendShellMessage<T extends JupyterMessage>(msg: T): void;
	}
}
declare module '@jupyterlab/services' {
	class KernelConnection extends JupyterKernelConnection implements kernel.IKernelConnection {
		sendJupyterMessage<T extends JupyterMessage>(msg: T): void;
		sendShellMessage<T extends JupyterMessage>(msg: T): void;
	}
}
JupyterKernelConnection.prototype.sendJupyterMessage = function (msg) {
	return this.sendShellMessage(msg);
};

export type JupyterMessageType =
	| 'code_cell'
	| 'visualization'
	| 'llm_request'
	| 'llm_response'
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

export interface IJupyterMessageContent {
	request?: string;
	response?: string;
	text?: string;
	code?: string;
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
	content: IJupyterMessageContent;
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
	parent_header: messages.IHeader | {};
}

export declare type JupyterMessage = IJupyterMessage | messages.Message;

// TODO: These settings should be pulled from the environment variables or appropriate config setup.
export const serverSettings = ServerConnection.makeSettings({
	baseUrl: '/chatty/',
	appUrl: 'http://localhost:8078/chatty/',
	wsUrl: 'ws://localhost:8078/chatty_ws/',
	token: import.meta.env.VITE_JUPYTER_TOKEN
});

export const kernelManager = new KernelManager({
	serverSettings
});
export const specsManager = new KernelSpecManager({
	serverSettings
});
export const sessionManager = new SessionManager({
	kernelManager,
	serverSettings
});

export const mimeService = new CodeMirrorMimeTypeService();
export const renderMime = new RenderMimeRegistry({ initialFactories });

export const newSession = (kernelName: string, name: string) => {
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
