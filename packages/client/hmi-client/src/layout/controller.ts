import { IGraph } from '@graph-scaffolder/index';

export default class LayoutController {
	private _isReady = false;

	private _isBusy = false;

	private worker: Promise<Worker>;

	private taskQueue: any[] = [];

	constructor() {
		this.worker = new Promise((resolve, reject) => {
			const worker = new Worker(new URL('./worker.ts', import.meta.url), {
				type: 'module'
			});
			worker.onmessage = (e: any) => {
				if (e.data) {
					this._isReady = true;
					resolve(worker);
				} else {
					console.error('Pyodide Init Error');
					reject();
				}
			};
		});
	}

	get isReady() {
		return this._isReady;
	}

	get isBusy() {
		return this._isBusy;
	}

	async runLayout<V, E>(graphData: IGraph<V, E>) {
		return new Promise((...promise) => {
			this.taskQueue.push({
				action: 'runLayout',
				params: [graphData],
				promise
			});
			this.queueTask();
		});
	}

	private async queueTask() {
		const worker = await this.worker;

		if (this.taskQueue.length && !this._isBusy) {
			this._isBusy = true;
			const { action, params, promise } = this.taskQueue.pop();
			worker.onmessage = (e) => {
				this._isBusy = false;
				this.queueTask();
				promise[0](e.data);
			};
			worker.onerror = (e) => {
				console.error(e);
				this._isBusy = false;
				this.queueTask();
			};
			worker.postMessage({
				action,
				params
			});
		}
	}
}

// Single instance for the app
export const layoutInstance = new LayoutController();
