import { test, expect } from '@playwright/experimental-ct-vue';
import ModelSidebarPanel from '@/components/sidebar-panel/model-sidebar-panel.vue';

test.describe('test ModelSidebarPanel component', () => {
	test('should display the correct header', async ({ mount }) => {
		const component = await mount(ModelSidebarPanel);
		const header = await component.locator('header');

		await expect(header).toHaveText('Model Space');
	});

	test('should display the button to open model from code', async ({ mount }) => {
		const component = await mount(ModelSidebarPanel);
		const goToTheia = await component.locator('button');
		await expect(goToTheia).toBeVisible();
	});
});
