import { pickBy, isNil } from 'lodash';

export function toQueryString(object: any) {
	return new URLSearchParams(pickBy(object, (v) => !isNil(v))).toString();
}
