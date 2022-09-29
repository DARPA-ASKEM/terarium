import { assert, describe, expect, it } from 'vitest';
import { add } from '../src/index';

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

describe('test index functions', () => {
	it('should add two numbers correctly', () => {
		expect(add(1, 1)).eq(2);
	});
});
