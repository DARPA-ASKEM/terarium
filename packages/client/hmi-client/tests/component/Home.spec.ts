import { test, expect } from '@playwright/experimental-ct-vue';
import Home from '@/views/Home.vue';

test.describe('test Home component', () => {
	test('option to create a new project is avialable', async ({ mount }) => {
		const component = await mount(Home);
		await expect(component).toContainText('New Project');
	});
});
