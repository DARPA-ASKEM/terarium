import sys
import os
import traceback
import subprocess
from taskrunner import TaskRunnerInterface
import PyPDF2

def cleanup():
    pass


def get_filename(id: str):
    return f"/tmp/{id}.pdf"


def create_temp_file(name, contents):
    with open(name, 'wb') as f:
        f.write(contents)


def delete_temp_file(name):
    try:
        os.remove(name)
    except:
        pass


def extract_text_from_pdf(filename):
    reader = PyPDF2.PdfReader(filename)
    text = ""
    for page in reader.pages:
        text += page.extract_text()
    return text


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="Embedding CLI")
        taskrunner.on_cancellation(cleanup)

        bs = taskrunner.read_input_bytes_with_timeout()

        ifilename = get_filename("input_" + taskrunner.id)
        ofilename = get_filename("output_" + taskrunner.id)

        create_temp_file(ifilename, bs)

        # Define the command and arguments
        command = ["ocrmypdf", "--force-ocr", ifilename, ofilename]

        # Run the command
        result = subprocess.run(command, capture_output=True, text=True)

        # Print the output
        if result.returncode != 0:
            taskrunner.log("Error running ocrmypdf")
            taskrunner.log("stderr:" + result.stderr)
            taskrunner.log("Return code:" + str(result.returncode))
            raise Exception("Error running ocrmypdf")

        if result.stdout != "":
            taskrunner.log("ocrmypdf stdout:")
            taskrunner.log(result.stdout)

        if result.stderr != "":
            taskrunner.log("ocrmypdf stderr:")
            taskrunner.log(result.stderr)

        taskrunner.log("Extracting text")
        text = extract_text_from_pdf(ofilename)
        taskrunner.log("Extracted text!")

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
