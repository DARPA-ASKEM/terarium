import {Resource, Texture, BaseTexture, SCALE_MODES, MIPMAP_MODES } from 'pixi.js';

// taken from https://github.com/pixijs/pixijs/issues/6436#issuecomment-591695313
export class CustomBufferResource extends Resource {
	constructor(source, options) {
		const { width, height, internalFormat, format, type } = options || {};

		if (!width || !height || !internalFormat || !format || !type) {
			throw new Error(
				'CustomBufferResource width, height, internalFormat, format, or type invalid'
			);
		}

		super(width, height);

		(this as any).data = source;
		(this as any).internalFormat = internalFormat;
		(this as any).format = format;
		(this as any).type = type;
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
			gl[(this as any).internalFormat],
			baseTexture.width,
			baseTexture.height,
			0,
			gl[(this as any).format],
			gl[(this as any).type],
			(this as any).data
		);

		return true;
	}
}

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
}