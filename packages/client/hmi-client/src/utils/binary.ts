function b64EncodeUnicode(str) {
	return btoa(
		encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (_match, p1) => String.fromCharCode(Number(`0x${p1}`)))
	);
}

function b64DecodeUnicode(str) {
	return decodeURIComponent(
		atob(str)
			.split('')
			.map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
			.join('')
	);
}

export { b64EncodeUnicode, b64DecodeUnicode };
