export const getMicroEl = `
uvec4 getMicroEl(usampler2D tex, int index) {
	int texWidth = textureSize(tex, 0).x;
	int col = index % texWidth;
	int row = index / texWidth;
	return texelFetch(tex, ivec2(col, row), 0);
}`;