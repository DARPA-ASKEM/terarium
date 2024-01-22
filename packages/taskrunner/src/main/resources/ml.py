import argparse
import time
import sys

# Create the parser
parser = argparse.ArgumentParser(description='Advanced Blockchain ML Task')

# Add the arguments
parser.add_argument('--input_pipe', type=str, required=True, help='The name of the input pipe')
parser.add_argument('--output_pipe', type=str, required=True, help='The name of the output pipe')

# Parse the arguments
args = parser.parse_args()

# Define the names of your pipes
input_pipe = args.input_pipe
output_pipe = args.output_pipe

# Define the names of your pipes
input_pipe = sys.argv[1]
output_pipe = sys.argv[2]

line = ""
with open(input_pipe, 'rb') as f:
	line = f.readline().strip()

print(line.decode())

time.sleep(5)

with open(output_pipe, 'wb') as f_out:
	f_out.write(line)
