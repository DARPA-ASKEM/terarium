enum OperatorInteractionStatus {
	Found = 0x0001,
	Hover = 0x0010,
	Focus = 0x0100,
	Drag = 0x1000
}

function addState(status: number, state: OperatorInteractionStatus) {
	// eslint-disable-next-line no-bitwise
	status |= state;
	return status;
}

function removeState(status: number, state: OperatorInteractionStatus) {
	// eslint-disable-next-line no-bitwise
	status &= ~state;
	return status;
}

function isStateActive(status: number, state: OperatorInteractionStatus) {
	// eslint-disable-next-line no-bitwise
	return status & state;
}

// Hover
export function addHover(status: number) {
	return addState(status, OperatorInteractionStatus.Hover);
}

export function removeHover(status: number) {
	return removeState(status, OperatorInteractionStatus.Hover);
}

export function isHover(status: number) {
	return isStateActive(status, OperatorInteractionStatus.Hover);
}

// Found
export function addFound(status: number) {
	return addState(status, OperatorInteractionStatus.Found);
}

export function removeFound(status: number) {
	return removeState(status, OperatorInteractionStatus.Found);
}

export function isFound(status: number) {
	return isStateActive(status, OperatorInteractionStatus.Found);
}

// Focus
export function addFocus(status: number) {
	return addState(status, OperatorInteractionStatus.Focus);
}

export function removeFocus(status: number) {
	return removeState(status, OperatorInteractionStatus.Focus);
}

export function isFocus(status: number) {
	return isStateActive(status, OperatorInteractionStatus.Focus);
}

// Drag
export function addDrag(status: number) {
	console.log(addState(status, OperatorInteractionStatus.Drag));
	return addState(status, OperatorInteractionStatus.Drag);
}

export function removeDrag(status: number) {
	return removeState(status, OperatorInteractionStatus.Drag);
}

export function isDrag(status: number) {
	return isStateActive(status, OperatorInteractionStatus.Drag);
}
