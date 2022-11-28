export default `
#version 300 es

precision lowp usampler2D;
precision highp sampler2D;
precision highp float;

in vec2 vUvs;

uniform sampler2D uColor;
uniform usampler2D uMicroCol;
uniform usampler2D uMicroRow;

uniform float uNumCol;
uniform float uNumRow;
uniform float uScreenWidth;
uniform float uScreenHeight;
uniform float uWorldWidth;
uniform float uWorldHeight;
uniform float uViewportWorldWidth;
uniform float uViewportWorldHeight;
uniform bool uGridDisplayBorder;
uniform float uGridColDisplayLim;
uniform float uGridRowDisplayLim;
uniform float uGridColDisplayTransition;
uniform float uGridRowDisplayTransition;

out vec4 fragColor;

void main() {
	// get the total number of micro cols and rows
	float microColLen = float(textureSize(uMicroCol, 0).x);
	float microRowLen = float(textureSize(uMicroRow, 0).x);

	// calculate micro border proximity value
	vec2 microUvSize = 1. / vec2(microColLen, microRowLen);
	vec2 microUv = mod(vUvs, microUvSize) / microUvSize;
	vec2 microBorderProximity = mix(microUv, 1. - microUv, lessThan(microUv, vec2(0.5)));

	// get element neighbor indexes
	uint colIndex = texelFetch(uMicroCol, ivec2(int(vUvs.x * microColLen), 0), 0).r;
	uint rowIndex = texelFetch(uMicroRow, ivec2(int(vUvs.y * microRowLen), 0), 0).r;

	// get the row/col index of the micro-row/col and that of its immediate neighbor
	int colMicroNeighborIndex = microUv.x < 0.5 ? int(vUvs.x * microColLen) - 1 : int(vUvs.x * microColLen) + 1;
	uint colNeighborIndex = texelFetch(uMicroCol, ivec2(colMicroNeighborIndex, 0), 0).r;
	int rowMicroNeighborIndex = microUv.y < 0.5 ? int(vUvs.y * microRowLen) - 1 : int(vUvs.y * microRowLen) + 1;
	uint rowNeighborIndex = texelFetch(uMicroRow, ivec2(rowMicroNeighborIndex, 0), 0).r;

	// determine if border is at the external perimeter
	bool isExternalColBorder = (vUvs.x * microColLen < 0.5) || (vUvs.x * microColLen > microColLen - 0.5);
	bool isExternalRowBorder = (vUvs.y * microRowLen < 0.5) || (vUvs.y * microRowLen > microRowLen - 0.5);

	// determine if grid lines should be rendered
	bool shouldRenderCol = isExternalColBorder ? uGridDisplayBorder : colNeighborIndex != colIndex;
	bool shouldRenderRow = isExternalRowBorder ? uGridDisplayBorder : rowNeighborIndex != rowIndex;

	// initialize fragColor
	fragColor = vec4(0.);

	// render column grid lines
	if(shouldRenderCol) {
		// use interpolation to draw an anti-aliased line at the border
		vec4 colGrid = mix(
			vec4(0.),
			vec4(1.),
			smoothstep(1. - (uViewportWorldWidth / uScreenWidth * microColLen), 1., microBorderProximity.x)
		);

		// use interpolation to fade the line in and out depending on the line density
		fragColor = mix(
			fragColor,
			colGrid,
			1. - smoothstep(
				uGridColDisplayLim - uGridColDisplayTransition,
				uGridColDisplayLim + uGridColDisplayTransition,
				uViewportWorldWidth / uWorldWidth * uNumCol
			)
		);
	}

	// render row grid lines
	if(shouldRenderRow) {
		// use interpolation to draw an anti-aliased line at the border
		vec4 rowGrid = mix(
			vec4(0.),
			vec4(1.),
			smoothstep(1. - (uViewportWorldHeight / uScreenHeight * microRowLen), 1., microBorderProximity.y)
		);

		// combine both the row grid line frag and the col grid line frag with max
		// use interpolation to fade the line in and out depending on the line density
		fragColor = max(
			fragColor,
			mix(
				fragColor,
				rowGrid,
				1. - smoothstep(
					uGridRowDisplayLim - uGridRowDisplayTransition,
					uGridRowDisplayLim + uGridRowDisplayTransition,
					uViewportWorldHeight / uWorldHeight * uNumRow
				)
			)
		);
	}
}`;
