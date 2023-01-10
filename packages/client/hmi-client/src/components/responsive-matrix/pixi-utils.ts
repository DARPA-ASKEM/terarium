import { Viewport } from 'pixi-viewport';
import {
	Application,
	Point,
	Resource,
	Texture,
	BaseTexture,
	SCALE_MODES,
	MIPMAP_MODES
} from 'pixi.js';

// EMPTY_POINT used as a fallback value
const EMPTY_POINT = new Point();
export const viewport2Screen = (viewport: Viewport) => {
	const topLeft = viewport.toScreen(0, 0) || EMPTY_POINT;
	const topRight = viewport.toScreen(viewport.worldWidth, 0) || EMPTY_POINT;
	const bottomLeft = viewport.toScreen(0, viewport.worldHeight) || EMPTY_POINT;
	const bottomRight = viewport.toScreen(viewport.worldWidth, viewport.worldHeight) || EMPTY_POINT;
	return { topLeft, topRight, bottomLeft, bottomRight };
};

// taken from https://github.com/pixijs/pixijs/issues/6436#issuecomment-591695313
export class CustomBufferResource extends Resource {
	data: any;

	internalFormat: string;

	format: string;

	type: string;

	constructor(source, options) {
		const { width, height, internalFormat, format, type } = options || {};

		if (!width || !height || !internalFormat || !format || !type) {
			throw new Error(
				'CustomBufferResource width, height, internalFormat, format, or type invalid'
			);
		}

		super(width, height);

		this.data = source;
		this.internalFormat = internalFormat;
		this.format = format;
		this.type = type;
	}

	upload(renderer, baseTexture, glTexture) {
		const gl = renderer.gl;

		gl.pixelStorei(
			gl.UNPACK_PREMULTIPLY_ALPHA_WEBGL,
			baseTexture.alphaMode === 1 // PIXI.ALPHA_MODES.UNPACK but `PIXI.ALPHA_MODES` are not exported
		);

		glTexture.width = baseTexture.width;
		glTexture.height = baseTexture.height;

		gl.texImage2D(
			baseTexture.target,
			0,
			gl[this.internalFormat],
			baseTexture.width,
			baseTexture.height,
			0,
			gl[this.format],
			gl[this.type],
			this.data
		);

		return true;
	}
}

/**
 * Get the WebGL max texture size limit using the pixi application gl instance.
 * @param {Application} app
 */
export const getGlMaxTextureSize = (app: Application): number => {
	const { gl } = (app.renderer as any).context;
	return gl.getParameter(gl.MAX_TEXTURE_SIZE);
};

/**
 * Given a data size and a max texture size, find the dimensions of the smallest 2D texture required
 * to hold the data.
 * @param {number} dataSize
 * @param {number} maxTextureSize
 */
export const getTextureDim = (dataSize: number, maxTextureSize: number) => {
	const dataSizeToMaxRatio = dataSize / maxTextureSize;

	// if multiple rows are required, set number of columns to max texture size
	// otherwise set the number of columns to the data size with min of 1
	const x = dataSizeToMaxRatio >= 1 ? maxTextureSize : dataSize || 1;
	// set number of rows with a min of 1
	const y = Math.ceil(dataSizeToMaxRatio) || 1;

	return {
		x, // number of columns in texture
		y, // number of rows in texture
		n: x * y // number of elements in texture
	};
};

// see pg 124 of WebGL2 spec for valid internalFormat/format/type combinations
// https://registry.khronos.org/OpenGL/specs/es/3.0/es_spec_3.0.pdf

/**
 * Package a Uint32Array into a PIXI.JS texture.
 * @param {Uint32Array} data
 * @param {number} width
 * @param {height} height
 */
export const uint32ArrayToRedIntTex = (data: Uint32Array, width: number, height: number) => {
	const buffer = new CustomBufferResource(data, {
		width,
		height,
		internalFormat: 'R32UI',
		format: 'RED_INTEGER',
		type: 'UNSIGNED_INT'
	});

	return new Texture(
		new BaseTexture(buffer, { scaleMode: SCALE_MODES.NEAREST, mipmap: MIPMAP_MODES.OFF })
	);
};
