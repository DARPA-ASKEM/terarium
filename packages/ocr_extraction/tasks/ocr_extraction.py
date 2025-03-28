import sys
import requests
import io
import traceback
import mimetypes
import base64



from taskrunner import TaskRunnerInterface

url = "http://localhost:8002/predict"


def get_content_type(file_name):
    # Additional custom MIME mappings for Markdown (not covered by the mimetypes library)
    custom_mime_types = {
        ".md": "text/markdown",
    }

    # Extract MIME type based on the file extension
    mime_type, _ = mimetypes.guess_type(file_name)

    # Check for known extensions and return custom mappings if needed
    if mime_type:
        return mime_type
    else:
        # Check for Markdown files or unknown types
        for ext, mime in custom_mime_types.items():
            if file_name.endswith(ext):
                return mime

    # Return a fallback if the type cannot be determined
    return "application/octet-stream"


def cleanup():
    pass


def main():
    exitCode = 0
    try:
        taskrunner = TaskRunnerInterface(description="OCR Extraction taskrunner")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()

        # "bytes" is a byte-array from object-mapper
        bs = base64.b64decode(input_dict["bytes"])

        filename = input_dict["filename"]

        files = {"file": (filename, io.BytesIO(bs), get_content_type(filename))}

        llm_model = "azure"
        if "llm" in input_dict:
            llm_model = input_dict["llm"]

        response = requests.post(url, files=files, data={"llm_model": llm_model })

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
