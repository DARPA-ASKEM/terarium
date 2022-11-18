import { test, expect } from '@playwright/experimental-ct-vue';
import ResponsiveMatrix from '@/components/responsive-matrix/matrix.vue';

test.describe('test Responsive Matrix component', () => {
	test('should display the correct message', async ({ mount }) => {
		const component = await mount(ResponsiveMatrix);

		await expect(component.locator('p')).toContainText('Responsive Matrix Cells');
	});
});
