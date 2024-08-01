import Lara from '@primevue/themes/lara';

export default {
	theme: {
		preset: Lara,
		options: {
			cssLayer: {
				name: 'primevue',
				order: 'tailwind-base, primevue, tailwind-utilities'
			}
		}
	}
};
