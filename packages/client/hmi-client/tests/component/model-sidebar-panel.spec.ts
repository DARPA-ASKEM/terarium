import { test, expect } from '@playwright/experimental-ct-vue';
import ModelSidebarPanel from '@/components/sidebar-panel/model-sidebar-panel.vue';

test.describe('test ModelSidebarPanel component', () => {
	test('should display the button to open model from code', async ({ mount }) => {
		const component = await mount(ModelSidebarPanel);
		const goToTheia = await component.locator('button');
		await expect(goToTheia).toBeVisible();
	});
});
