import argparse
import json
import time
import signal
import sys
import concurrent.futures

def read_input(input_pipe: str):
	with open(input_pipe, 'rb') as f:
		line = f.readline().strip()
		return json.loads(line.decode())

def read_input_with_timeout(input_pipe: str, timeout_seconds: int):
	with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
		future = executor.submit(read_input, input_pipe)
		try:
			return future.result(timeout=timeout_seconds)
		except concurrent.futures.TimeoutError:
			raise TimeoutError('Reading from input pipe timed out')

def write_output(output_pipe: str, output: dict):
	bs = (json.dumps(output) + "\n").encode()
	with open(output_pipe, 'wb') as f_out:
		f_out.write(bs)
		return

def write_output_with_timeout(output_pipe: str, output: dict, timeout_seconds: int):
	with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
		future = executor.submit(write_output, output_pipe, output)
		try:
			return future.result(timeout=timeout_seconds)
		except concurrent.futures.TimeoutError:
			raise TimeoutError('Writing to output pipe timed out')

def signal_handler(sig, frame):
	print('Process cancelled, cleanup logic goes here')
	sys.exit(1)

def main():
	signal.signal(signal.SIGINT, signal_handler)
	signal.signal(signal.SIGTERM, signal_handler)

	parser = argparse.ArgumentParser(description='Advanced Blockchain ML Task')
	parser.add_argument('--input_pipe', type=str, required=True, help='The name of the input pipe')
	parser.add_argument('--output_pipe', type=str, required=True, help='The name of the output pipe')
	args = parser.parse_args()

	input = read_input_with_timeout(args.input_pipe, 5)

	# Tests use this to simulate a failing task
	if "should_fail" in input:
		print("ML Task: failing")
		sys.exit(1)

	for i in range(5):
		print("ML Task: {}".format(i))
		time.sleep(1)

	write_output_with_timeout(args.output_pipe, {"result": "ok"}, 5)

if __name__ == "__main__":
	main()
