import argparse
import json
import time
import signal
import sys
import os
import concurrent.futures


READ_CHUNK_SIZE = 1024*1024


def read_input(input_pipe: str):
	chunks = []
	with open(input_pipe, "rb") as f:
		while True:
			chunk = f.read(READ_CHUNK_SIZE)
			if chunk == b"":
				break
			chunks.append(chunk)
	return json.loads(b"".join(chunks).decode("utf-8"))

def read_input_with_timeout(input_pipe: str, timeout_seconds: int):
	with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
		future = executor.submit(read_input, input_pipe)
		try:
			return future.result(timeout=timeout_seconds)
		except concurrent.futures.TimeoutError:
			print('Reading from input pipe {} timed out'.format(input_pipe), flush=True)
			raise TimeoutError('Reading from input pipe timed out')

def write_output(output_pipe: str, output: dict):
	bs = json.dumps(output, separators=(',', ':')).encode()
	with open(output_pipe, 'wb') as f_out:
		f_out.write(bs)
		return

def write_output_with_timeout(output_pipe: str, output: dict, timeout_seconds: int):
	with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
		future = executor.submit(write_output, output_pipe, output)
		try:
			return future.result(timeout=timeout_seconds)
		except concurrent.futures.TimeoutError:
			print('Writing to output pipe {} timed out'.format(output_pipe), flush=True)
			raise TimeoutError('Writing to output pipe timed out')

def write_progress(progress_pipe: str, output: dict):
	bs = json.dumps(output, separators=(',', ':')).encode()
	with open(progress_pipe, 'wb') as f_out:
		f_out.write(bs)
		return

def write_progress_with_timeout(progress_pipe: str, output: dict, timeout_seconds: int):
	with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
		future = executor.submit(write_progress, progress_pipe, output)
		try:
			return future.result(timeout=timeout_seconds)
		except concurrent.futures.TimeoutError:
			print('Writing to progress pipe {} timed out'.format(progress_pipe), flush=True)
			raise TimeoutError('Writing to progress pipe timed out')

def finish_progress_with_timeout(progress_pipe: str, timeout_seconds: int):
	with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
		future = executor.submit(write_progress, progress_pipe, {"done":True})
		try:
			return future.result(timeout=timeout_seconds)
		except concurrent.futures.TimeoutError:
			print('Writing to progress pipe {} timed out'.format(progress_pipe), flush=True)
			raise TimeoutError('Writing to progress pipe timed out')

def signal_handler(sig, frame):
	print('Process cancelled, cleanup logic goes here', flush=True)
	sys.exit(1)

def main():
	signal.signal(signal.SIGINT, signal_handler)
	signal.signal(signal.SIGTERM, signal_handler)

	parser = argparse.ArgumentParser(description='Echo Task')
	parser.add_argument('--id', type=str, required=True, help='The request id')
	parser.add_argument('--input_pipe', type=str, required=True, help='The name of the input pipe')
	parser.add_argument('--output_pipe', type=str, required=True, help='The name of the output pipe')
	parser.add_argument('--progress_pipe', type=str, required=True, help='The name of the progress pipe')
	args = parser.parse_args()

	print("Task {} : attemping to read input from {}".format(args.id, args.input_pipe), flush=True)

	input = read_input_with_timeout(args.input_pipe, 5)

	# Tests use this to simulate a failing task
	if "should_fail" in input:
		print("Task {} : failing".format(args.id), flush=True)
		sys.exit(1)

	for i in range(5):
		print("Task {}: {}".format(args.id, i), flush=True)
		if "include_progress" in input:
			write_progress_with_timeout(args.progress_pipe, {"progress": i}, 5)
		time.sleep(1)

	# remove the progress pipe
	if "include_progress" in input:
		finish_progress_with_timeout(args.progress_pipe, 5)

	print("Task {} : attemping to write output to {}".format(args.id, args.output_pipe), flush=True)
	write_output_with_timeout(args.output_pipe, input, 5)

if __name__ == "__main__":
	main()
