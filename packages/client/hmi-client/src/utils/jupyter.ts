import { SessionContext } from '@jupyterlab/apputils';
import {
	ServerConnection,
	KernelManager,
	KernelSpecManager,
	SessionManager
} from '@jupyterlab/services';
import { CodeMirrorMimeTypeService } from '@jupyterlab/codemirror';
import {
  standardRendererFactories as initialFactories,  // TODO: Check
  RenderMimeRegistry
} from '@jupyterlab/rendermime';



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

export const newSession = (kernel, name) => {
    const sessionContext = new SessionContext({
        sessionManager,
        specsManager,
        name: name,
        kernelPreference: {
            name: kernel
        }
    });
 
    sessionContext.
	
	sessionContext.initialize();
    return sessionContext;
}
