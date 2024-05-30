const removeChildren = (parentElement: HTMLElement): HTMLElement => {
	while (parentElement.firstChild) {
		parentElement.removeChild(parentElement.firstChild);
	}
	return parentElement;
};

export default removeChildren;
