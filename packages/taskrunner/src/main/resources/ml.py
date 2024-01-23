import argparse
import json
import time

def read_input(input_pipe: str):
	line = ""
	with open(input_pipe, 'rb') as f:
		line = f.readline().strip()
	return json.loads(line.decode())


def write_output(output_pipe: str, output: dict):
	bs = (json.dumps(output) + "\n").encode()
	with open(output_pipe, 'wb') as f_out:
		f_out.write(bs)

def main():
	parser = argparse.ArgumentParser(description='Advanced Blockchain ML Task')
	parser.add_argument('--input_pipe', type=str, required=True, help='The name of the input pipe')
	parser.add_argument('--output_pipe', type=str, required=True, help='The name of the output pipe')
	args = parser.parse_args()

	input = read_input(args.input_pipe)

	for i in range(5):
		print("ML Task: {}".format(i))
		time.sleep(1)

	write_output(args.output_pipe, {"result": "ok"})

if __name__ == "__main__":
	main()
