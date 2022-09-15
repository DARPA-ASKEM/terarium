import { assert, describe, expect, it } from 'vitest';

describe('basic tests to make sure it all works', () => {
	it('should have the correct square root', () => {
		assert.equal(Math.sqrt(4), 2);
	});

	it('should sum up to 2', () => {
		expect(1 + 1).eq(2);
	});

	it('should generate and match snapshot', () => {
		expect({ foo: 'bar' }).toMatchSnapshot();
	});
});
