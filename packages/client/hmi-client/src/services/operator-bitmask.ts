// These values represent different interaction states on an operator node
// The operator header, border, etc. will be styled differently depending on which states are active
enum Status {
	Found = 0x0001,
	Hover = 0x0010,
	Focus = 0x0100,
	Drag = 0x1000
}

const addState = (status: number, state: Status) => {
	status |= state;
	return status;
};
const removeState = (status: number, state: Status) => {
	status &= ~state;
	return status;
};
const isStateActive = (status: number, state: Status) => status & state;

// Hover
export const addHover = (status: number) => addState(status, Status.Hover);
export const removeHover = (status: number) => removeState(status, Status.Hover);
export const isHover = (status: number) => isStateActive(status, Status.Hover);
// Found
export const addFound = (status: number) => addState(status, Status.Found);
export const removeFound = (status: number) => removeState(status, Status.Found);
export const isFound = (status: number) => isStateActive(status, Status.Found);
// Focus
export const addFocus = (status: number) => addState(status, Status.Focus);
export const removeFocus = (status: number) => removeState(status, Status.Focus);
export const isFocus = (status: number) => isStateActive(status, Status.Focus);
// Drag
export const addDrag = (status: number) => addState(status, Status.Drag);
export const removeDrag = (status: number) => removeState(status, Status.Drag);
export const isDrag = (status: number) => isStateActive(status, Status.Drag);
