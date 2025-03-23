import sys
import requests
import io
import traceback

from taskrunner import TaskRunnerInterface


url = "http://localhost:8002/predict"


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="OCR Extraction taskrunner")
        taskrunner.on_cancellation(cleanup)

        bs = taskrunner.read_input_bytes_with_timeout()

        files = {"file": ("uploaded_file.pdf", io.BytesIO(bs), "application/pdf")}
        response = requests.post(url, files=files)

        response_json = response.json()

        taskrunner.write_output_dict_with_timeout({"response": response_json})

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
