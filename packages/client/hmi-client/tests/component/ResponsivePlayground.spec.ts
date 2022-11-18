import { test, expect } from '@playwright/experimental-ct-vue';
import ResponsivePlayground from '@/views/ResponsivePlayground.vue';

test.describe('test Responsive Matrix component', () => {
	test('should display the correct message', async ({ mount }) => {
		const component = await mount(ResponsivePlayground);

		await expect(component.locator('p')).toContainText('Responsive Matrix Cells');
	});
});
