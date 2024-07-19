import { addHover, removeHover, isHover, addDrag, removeDrag, isDrag } from '@/services/operator-bitmask';
import { describe, expect, it } from 'vitest';

let interactionStatus = 0;

describe('test bitmasking for different interaction in the workflow UI', () => {
	it('add hover state', () => {
		interactionStatus = addHover(interactionStatus);
		expect(interactionStatus).to.eq(0x0010);
	});
	it('add drag state', () => {
		interactionStatus = addDrag(interactionStatus);
		expect(interactionStatus).to.eq(0x1010);
	});
	it('check if hover state is on', () => {
		expect(isHover(interactionStatus)).toBeTruthy();
	});
	it('check if drag state is on', () => {
		expect(isDrag(interactionStatus)).toBeTruthy();
	});
	it('remove hover state', () => {
		interactionStatus = removeHover(interactionStatus);
		expect(interactionStatus).to.eq(0x1000);
	});
	it('remove drag state', () => {
		interactionStatus = removeDrag(interactionStatus);
		expect(interactionStatus).to.eq(0);
	});
});
