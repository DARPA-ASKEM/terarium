import {
	BasicRenderer,
	DeltaRenderer,
	Renderer,
	flattenGraph,
	traverseGraph,
	traverseNode,
	getAStarPath
} from './core';

import simplifyPath from './utils/simplify';
import { moveTo, moveToLabel } from './fn/move-to';
import { group, ungroup } from './fn/group';
import { highlight } from './fn/highlight';
import unHighlight from './fn/un-highlight';

import type { INode, IEdge, IGraph } from './types';

const graphScaffolder = {
	Renderer,
	BasicRenderer,
	DeltaRenderer,
	flattenGraph,
	traverseGraph,
	traverseNode,
	getAStarPath,
	moveTo,
	moveToLabel,
	highlight,
	unHighlight,
	group,
	ungroup,
	simplifyPath
};

export {
	INode,
	IEdge,
	IGraph
}

export default graphScaffolder;

