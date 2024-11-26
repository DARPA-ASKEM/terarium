import sys
import os
import traceback
import subprocess
from taskrunner import TaskRunnerInterface

def cleanup():
    pass


def get_filename(id: str, ext : str):
    return f"/tmp/{id}.{ext}"


def create_temp_file(name, contents):
    with open(name, 'wb') as f:
        f.write(contents)


def delete_temp_file(name):
    try:
        os.remove(name)
    except:
        pass

def read_file_to_string(filename):
    with open(filename, 'r', encoding='utf-8') as file:
        content = file.read()
    return content

def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="Embedding CLI")
        taskrunner.on_cancellation(cleanup)

        bs = taskrunner.read_input_bytes_with_timeout()

        ifilename = get_filename("input_" + taskrunner.id, "pdf")
        ofilename = get_filename("output_" + taskrunner.id, "txt")

        create_temp_file(ifilename, bs)

        # Define the command and arguments
        command = ["pdftotext", ifilename, ofilename]

        # Run the command
        result = subprocess.run(command, capture_output=True, text=True)

        # Print the output
        if result.returncode != 0:
            taskrunner.log("Error running pdftotext")
            taskrunner.log("stderr:" + result.stderr)
            taskrunner.log("Return code:" + str(result.returncode))
            raise Exception("Error running pdftotext")

        if result.stdout != "":
            taskrunner.log("pdftotext stdout:")
            taskrunner.log(result.stdout)

        if result.stderr != "":
            taskrunner.log("pdftotext stderr:")
            taskrunner.log(result.stderr)

        taskrunner.log("Extracting text")
        text = read_file_to_string(ofilename)

        taskrunner.write_output_dict_with_timeout({"response": text})

    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.write(traceback.format_exc())
        sys.stderr.flush()
        exitCode = 1

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exitCode)


if __name__ == "__main__":
    main()
