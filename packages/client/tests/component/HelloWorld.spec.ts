import { test, expect } from '@playwright/experimental-ct-vue';
import HelloWorld from '@/components/HelloWorld.vue';

test.describe('test Hello World component', () => {
	test('should display the correct message', async ({ mount }) => {
		const component = await mount(HelloWorld, {
			props: {
				msg: 'Hello World'
			}
		});

		await expect(component.locator('h1')).toContainText('Hello World');
	});

	test('should have the correct styling on link', async ({ mount }) => {
		const component = await mount(HelloWorld, {
			props: {
				msg: 'Hello World'
			}
		});

		const link = await component.locator('p.read-the-docs');
		// NOTE: needs to be RGB or RGBA not HEX representation
		await expect(link).toHaveCSS('color', 'rgb(136, 136, 136)');
	});
});
