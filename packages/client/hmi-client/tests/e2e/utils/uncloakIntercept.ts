// eslint-disable-next-line import/no-extraneous-dependencies
import { Page, Route } from '@playwright/test';

export default async function authRoute(page: Page) {
	await page.route('**/silent-check-sso.html', (route: Route) => {
		route.fulfill({
			headers: {
				OIDC_access_token: 'fake token'
			},
			contentType: 'text/plain',
			body: 'Fake Authentication'
		});
	});
}
