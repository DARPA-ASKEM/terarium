import { getMicroEl } from './matrix-utils.glsl';

export default `
#version 300 es

precision lowp usampler2D;
precision highp sampler2D;
precision highp float;

in vec2 vUvs;

uniform sampler2D uColor;
uniform usampler2D uMicroRow;
uniform usampler2D uMicroCol;

uniform vec2 uMicroElDim;

out vec4 fragColor;

${getMicroEl}

void main() {
	// get the total number of micro cols and rows
	float microColLen = uMicroElDim.x;
	float microRowLen = uMicroElDim.y;

	uint colIndex = getMicroEl(uMicroCol, int(vUvs.x * microColLen)).r;
	uint rowIndex = getMicroEl(uMicroRow, int(vUvs.y * microRowLen)).r;

	fragColor = texelFetch(uColor, ivec2(colIndex, rowIndex), 0);
}`;
