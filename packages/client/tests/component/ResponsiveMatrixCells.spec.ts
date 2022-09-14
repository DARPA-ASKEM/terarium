import { test, expect } from '@playwright/experimental-ct-vue';
import ResponsiveMatrixCells from '@/components/ResponsiveMatrixCells.vue';

test.describe('test Responsive Matrix component', () => {
	test('should display the correct message', async ({ mount }) => {
		const component = await mount(ResponsiveMatrixCells);

		await expect(component.locator('p')).toContainText('Test');
	});
});
