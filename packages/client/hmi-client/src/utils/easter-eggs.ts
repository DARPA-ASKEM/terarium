/* Matrix effect easter egg: This gets triggered 1 in 10 times a person clicks the Matrix button */
export const matrixEffect = () => {
	if (Math.random() > 0.1) return;
	const canvas = document.getElementById('matrix-canvas') as HTMLCanvasElement | null;
	if (!canvas) return;
	const ctx = (canvas as HTMLCanvasElement)?.getContext('2d');

	// eslint-disable-next-line no-multi-assign
	const w = (canvas.width = document.body.offsetWidth);
	// eslint-disable-next-line no-multi-assign
	const h = (canvas.height = document.body.offsetHeight);
	const cols = Math.floor(w / 20) + 1;
	const ypos = Array(cols).fill(0);

	if (ctx) {
		ctx.fillStyle = '#FFF';
		ctx.fillRect(0, 0, w, h);
	}

	function matrix() {
		if (ctx) {
			ctx.fillStyle = '#FFF1';
			ctx.fillRect(0, 0, w, h);

			ctx.fillStyle = '#1B8073';
			ctx.font = '15pt monospace';

			ypos.forEach((y, ind) => {
				const text = String.fromCharCode(Math.random() * 128);
				const x = ind * 20;
				ctx.fillText(text, x, y);
				if (y > 100 + Math.random() * 10000) ypos[ind] = 0;
				else ypos[ind] = y + 20;
			});
		}
	}

	const intervalId = setInterval(matrix, 33);

	// after 4 seconds begin the fade out
	setTimeout(() => {
		if (canvas) {
			canvas.style.opacity = '0';
		}
	}, 3000);

	// after 5 seconds clear the canvas, stop the interval, and reset the opacity
	setTimeout(() => {
		clearInterval(intervalId);
		if (ctx) {
			ctx.clearRect(0, 0, w, h);
		}
		if (canvas) {
			canvas.style.opacity = '1';
		}
	}, 4000);
};
