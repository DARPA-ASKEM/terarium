export interface ServerError {
	timestamp: string;
	status: number;
	error: string;
	message: string;
	path: string;
	trace: string;
}
