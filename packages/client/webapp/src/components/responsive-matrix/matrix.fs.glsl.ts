export default `
#version 300 es

precision lowp usampler2D;
precision highp sampler2D;
precision highp float;

in vec2 vUvs;

uniform sampler2D uColor;
uniform usampler2D uMicroRow;
uniform usampler2D uMicroCol;

out vec4 fragColor;

vec4 getColorFromRowCol(sampler2D tex, int index) {
	int texWidth = textureSize(tex, 0).x;
	int col = index % texWidth;
	int row = index / texWidth;
	return texelFetch(tex, ivec2(col, row), 0);
}

void main() {
	float microColLen = float(textureSize(uMicroCol, 0).x);
	float microRowLen = float(textureSize(uMicroRow, 0).x);
	uint colIndex = texelFetch(uMicroCol, ivec2(int(vUvs.x * microColLen), 0), 0).r;
	uint rowIndex = texelFetch(uMicroRow, ivec2(int(vUvs.y * microRowLen), 0), 0).r;

	fragColor = texelFetch(uColor, ivec2(colIndex, rowIndex), 0);
}`;