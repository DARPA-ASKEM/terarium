import { test, expect } from '@playwright/experimental-ct-vue';
import Navbar from '@/components/Navbar.vue';

test.describe('test Navbar component', () => {
	test('should display the correct navbar', async ({ mount }) => {
		const component = await mount(Navbar);
		const nav = await component.locator('img');

		await expect(nav).toBeVisible();
	});
});
