import { test, expect } from '@playwright/test';
import authRoute from './utils/uncloakIntercept';

test.describe('Data-Explorer page test', () => {
	test.beforeEach(async ({ page }) => {
		await authRoute(page);

		// Go to the starting (base) url before each test as defined in the config
		await page.goto('/');
	});

	test('should display the data-explorer', async ({ page }) => {
		await page.locator('button.data-explorer').click();
		const dataExplorer = await page.locator('.data-explorer-container');
		await expect(dataExplorer).toBeVisible();
	});
});
