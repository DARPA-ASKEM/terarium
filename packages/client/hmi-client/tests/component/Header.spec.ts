import { test, expect } from '@playwright/experimental-ct-vue';
import Header from '@/components/Header.vue';

test.describe('test Header component', () => {
	test('should display the correct header', async ({ mount }) => {
		const component = await mount(Header);
		const nav = await component.locator('img');

		await expect(nav).toBeVisible();
	});
});
