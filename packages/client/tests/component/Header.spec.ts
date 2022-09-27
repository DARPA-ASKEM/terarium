import { test, expect } from '@playwright/experimental-ct-vue';
import Header from '@/components/Header.vue';

test.describe('test Header component', () => {
	test('should display the correct header name', async ({ mount }) => {
		const component = await mount(Header);

		await expect(component.locator('header')).toContainText('TERArium');
	});
});
