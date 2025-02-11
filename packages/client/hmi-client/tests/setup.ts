class MockWorker {
	onmessage: ((event: MessageEvent) => void) | null = null;

	onerror: ((event: ErrorEvent) => void) | null = null;

	postMessage(_: unknown): void {
		// Simulate worker behavior if needed
	}

	terminate(): void {
		// Simulate worker termination if needed
	}
}

// Assign the mock to global.Worker
(global as any).Worker = MockWorker;
