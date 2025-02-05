import { IGraph } from '@graph-scaffolder/index';
import { runDagreLayout } from '@/services/graph';

const runLayout = <V, E>(graphData: IGraph<V, E>) => runDagreLayout(graphData, true);

postMessage(true);

const map = new Map<string, Function>();
map.set('runLayout', runLayout);

onmessage = function (e) {
	const { action, params } = e.data;

	const func = map.get(action);
	if (func) {
		// eslint-disable-next-line
		return postMessage(func.apply(null, params));
	}
	return '';
};
