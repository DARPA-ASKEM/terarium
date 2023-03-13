/**
 * This function returns a base64 image that is dynamically generated based on the values passed into it
 * It requires p5.js
 */

import p5 from 'p5';

// Because toDataURL is not part of the official documentation
// https://github.com/processing/p5.js/issues/2326
interface IRenderer extends p5.Renderer {
	toDataURL(): string;
}
interface Ip5 extends p5 {
	canvas: IRenderer;
}

interface IPlaceholderArgs {
	contributors: number;
	models: number;
	datasets: number;
	papers: number;
}

// Define variables
let placeholderCanvas: p5 | null = null;
const imageHeight = 133;
const imageWidth = 208;
let contributors: number;
let models: number;
let datasets: number;
let papers: number;

function drawContributors(p: p5) {
	const maxContributors = 20;
	const scaleFactor = 10;
	let spacerX = maxContributors - (contributors / maxContributors) * scaleFactor;
	let spacerY = maxContributors - (contributors / maxContributors) * scaleFactor;
	const dotSize = spacerX / 2;

	const randomShift = p.random(-60, 60);
	const from = p.color(172 + randomShift / 4, 15, 180); // lighter color
	const to = p.color(172 + randomShift, 20, 80); // darker color

	for (let side = -1; side <= 1; side += 2) {
		spacerX = maxContributors - (contributors / maxContributors) * scaleFactor;
		spacerY = maxContributors - (contributors / maxContributors) * scaleFactor;

		for (let iY = 10; iY < imageHeight; iY += spacerY) {
			for (let iX = imageWidth / 2; iX < imageWidth && iX > -imageWidth; iX += spacerX * side) {
				p.fill(p.lerpColor(from, to, iY / imageHeight));
				p.circle(iX, iY, dotSize);

				for (let h = dotSize; h > 2; h--) {
					p.fill(
						p.lerpColor(p.lerpColor(from, to, iY / imageHeight), p.color(0, 0, 100, 0.1), 0.25)
					);
					p.ellipse(iX - dotSize / 10, iY - dotSize / 10, h * 0.8, h * 0.8);
				}
			}

			spacerY *= 1.1;
			spacerX *= 1.1;
		}
	}
}

// Squiggly lines across the screen for each dataset
function drawDatasets(p: p5) {
	function squigglyLine(y, number, lineNumber) {
		// draws a squiggly line across the screen at position y
		// color changes according to y position
		p.colorMode('hsb');
		const randomShift = p.random(-60, 60);
		const from = p.color(72 + randomShift, 61, 139, 0.5);
		const to = p.color(218 + randomShift, 165, 32, 0.5);

		// store points so we can draw a thick line and thin line with the same points
		const numPoints = 10;
		const squiggleHeight = 15;
		const linePosition = [] as number[];

		// draw squiggle
		p.noFill();
		p.strokeWeight((lineNumber / number) * 4);
		p.stroke(p.lerpColor(from, to, y / imageHeight));

		p.beginShape();
		p.curveVertex(0, y);
		p.curveVertex(0, y);
		for (let i = 1; i < numPoints; i++) {
			linePosition[i] = y + p.random(-squiggleHeight, squiggleHeight);
			p.curveVertex((imageWidth / numPoints) * i, linePosition[i]);
		}
		p.curveVertex(imageWidth, y);
		p.curveVertex(imageWidth, y);
		p.endShape();

		// draw middle layer of squiggle
		p.noFill();
		p.strokeWeight((lineNumber / number) * 5);
		p.stroke(p.lerpColor(p.lerpColor(from, to, y / imageHeight), p.color(0, 0, 100), 0.25));

		p.beginShape();
		p.curveVertex(0, y - 2);
		p.curveVertex(0, y - 2);
		for (let i = 1; i < numPoints; i++) {
			p.curveVertex((imageWidth / numPoints) * i, linePosition[i] - 2);
		}
		p.curveVertex(imageWidth, y - 2);
		p.curveVertex(imageWidth, y - 2);
		p.endShape();

		// draw top highlight of squiggle
		p.noFill();
		p.strokeWeight(lineNumber / number);
		p.stroke(72 + randomShift, 20, 200, 0.6);

		p.beginShape();
		p.curveVertex(0, y - 2);
		p.curveVertex(0, y - 2);
		for (let i = 1; i < numPoints; i++) {
			p.curveVertex((imageWidth / numPoints) * i, linePosition[i] - 2);
		}
		p.curveVertex(imageWidth, y - 2);
		p.curveVertex(imageWidth, y - 2);
		p.endShape();
	}

	for (let i = 1; i <= datasets; i++) {
		const ySpacing = (imageHeight / datasets) * (i / 1.5);
		squigglyLine(ySpacing, datasets, i);
	}
}

// Cone-shaped slashes at random places across screen for each model
function drawModels(p: p5) {
	for (let i = 1; i <= models; i++) {
		p.colorMode('hsb');
		const strokeHue = p.random(180, 360);
		const strokeFrom = p.color(strokeHue, 30, 70, 0.5);
		const strokeTo = p.color(strokeHue, 50, 50, 0.5);
		p.strokeWeight(2);
		const topX = p.random(0, imageWidth);
		const bottomX = p.random(0, imageWidth);
		for (let j = 0; j <= 10; j++) {
			p.stroke(p.lerpColor(strokeFrom, strokeTo, j / 10));
			p.line(topX, 0, bottomX + j, imageHeight);
		}
	}
}

// Circles at random heights for each paper, decreasing in size as number increases
function drawPapers(p: p5) {
	function drawCircle(cx, cy, circleSize) {
		p.noStroke();
		p.colorMode('hsb');
		const randomHue = p.random(50, 200);

		// back
		p.fill(randomHue, 100, 50);
		p.circle(cx, cy, circleSize);

		// feathered hot spot
		for (let i = 1; i < 50; i++) {
			p.fill(randomHue, 10, 100, 0.05);
			p.ellipse(
				cx - circleSize / 8,
				cy - circleSize / 8,
				circleSize * (i / 50),
				circleSize * (i / 50)
			);
		}
	}

	for (let i = 1; i <= papers; i++) {
		const xPos = p.map(i % 10, 0, 10, 20, imageWidth - 10, true);
		const yPos = p.random(10, imageHeight - 20);
		const circleSize = p.map(papers, 0, 50, 20, 10, true);
		drawCircle(xPos, yPos, circleSize * p.random(1, 2));
	}
}

// Setup the instance mode of p5
// https://p5js.org/reference/#/p5/p5
const sketch = (p: p5) => {
	// Create the canvas
	p.setup = () => {
		p.createCanvas(imageWidth, imageHeight);
		p.noCanvas(); // Do not display the canvas in the DOM
		p.noLoop();
		p.background(240);
	};

	p.draw = (): string => {
		drawContributors(p);
		if (models > 0) drawModels(p);
		if (datasets > 0) drawDatasets(p);
		if (papers > 0) drawPapers(p);
		return (p as Ip5).canvas.toDataURL();
	};
};

export function placeholder(args: IPlaceholderArgs): string {
	// Set the value for each datum.
	// Limit to 25 to keep the graph tidy.
	contributors = Math.min(args.contributors, 25);
	models = Math.min(args.models, 25);
	datasets = Math.min(args.datasets, 25);
	papers = Math.min(args.papers, 25);

	// Create the p5 sketch
	if (!placeholderCanvas) {
		placeholderCanvas = new p5(sketch); /* eslint-disable-line new-cap */
	}

	// Add all of this within the canvas and export the base64 image
	return placeholderCanvas.draw() as unknown as string;
}
