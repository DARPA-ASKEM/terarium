from flask import Flask, request, jsonify
from celery import Celery
from celery.result import AsyncResult
import os
from werkzeug.utils import secure_filename
from pdf_extraction import extract_text, extract_images_base64
import logging
import requests
from tempfile import NamedTemporaryFile
import jwt
import time

logging.basicConfig(level=logging.DEBUG)
UPLOAD_FOLDER = "uploads"
OUTPUT_FOLDER = "output"
ALLOWED_EXTENSIONS = {"pdf"}

app = Flask(__name__)
app.config["broker_url"] = os.getenv("IP_ADDRESS", "amqp://terarium:terarium123@host.docker.internal:5672//")
app.config["result_backend"] = "rpc://"

celery = Celery(app.name, broker=app.config["broker_url"])
celery.conf.update(app.config)
app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(OUTPUT_FOLDER, exist_ok=True)


def allowed_file(filename):
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route("/convertpdfurl", methods=["GET"])
def convert_pdf_url():
    """
    This function downloads a PDF file from a given URL, saves it as a temporary file, and then
    initiates a task to extract text and images from the PDF using the specified extraction method and
    image extraction option.
    :return: If the response status code is 200, a task ID is returned as a JSON object with a 202
    status code. If the response status code is not 200, an error message is returned as a JSON object
    with a 400 status code.
    """

    url = request.args.get("url")
    extraction_method = request.args.get("extraction_method")
    extract_images = request.args.get("extract_images")
    logging.info(url)
    logging.info(extraction_method)
    logging.info(extract_images)
    response = requests.get(url)
    if response.status_code == 200:
        with NamedTemporaryFile(delete=False) as tmp_file:
            tmp_file.write(response.content)
            pdf_path = tmp_file.name

        task = extract_text_and_images_task.apply_async(
            args=[pdf_path, extraction_method, extract_images]
        )

        task_id = task.id
        return jsonify({"task_id": task_id}), 202
    else:
        return (
            jsonify({"error": "Unable to download the PDF file from the provided URL"}),
            400,
        )


@app.route("/convertpdftask", methods=["POST"])
def convert_pdf_task():
    """
    This function converts a PDF file to text and images using different extraction methods and returns
    a task ID for asynchronous processing.
    :return: a JSON response with a task ID and a status code of 202 (Accepted) if the request is
    successful. If there is an error, it returns a JSON response with an error message and an
    appropriate status code (e.g. 400 for bad request).
    """

    if "file" not in request.files:
        return jsonify(error="No file part in the request"), 400

    extraction_method = request.form.get("extraction_method", "pydf2")
    extract_images = request.form.get("extract_images", False)
    logging.info("Extracting using:")
    logging.info(extraction_method)
    logging.info(extract_images)
    file = request.files["file"]
    if file.filename == "":
        return jsonify(error="No selected file"), 400

    filename = secure_filename(file.filename)

    file_path = os.path.join(UPLOAD_FOLDER, filename)
    file.save(file_path)

    if not allowed_file(file.filename):
        return jsonify(error="File type not allowed"), 400

    if extraction_method not in ["pypdf2", "pdfminer", "pymupdf"]:
        return (
            jsonify(
                error=f"Unsupported extraction method '[{extraction_method}]', supported methods are [pypdf2, pdfminer, pymupdf]"
            ),
            400,
        )

    task = extract_text_and_images_task.apply_async(
        args=[file_path, extraction_method, extract_images]
    )
    task_id = task.id

    return jsonify({"task_id": task_id}), 202


@app.route("/task-result/<task_id>", methods=["GET"])
def task_result(task_id):
    """
    The `task_result` function returns the status and result of a given task ID in a JSON response.

    :param task_id: a unique identifier for a task that has been submitted to a task queue or scheduler.
    It is used to retrieve the status and result of the task
    :return: The function `task_result` returns a JSON response containing the status and result of a
    task with the given task ID. If the task is ready, the result will be included in the response,
    otherwise it will be `None`.
    """
    try:
        task = AsyncResult(task_id)

        if task.ready():
            response_data = {
                "task_id": task_id,
                "status": task.status,
                "result": task.result,
            }
        else:
            response_data = {"task_id": task_id, "status": task.status, "result": None}
    except Exception:
        logging.error("Unable to find task_id: " + task_id)
        response_data = {"task_id": task_id, "status": "error", "result": None}

    return jsonify(response_data)


@celery.task(bind=True)
def extract_text_and_images_task(self, file_path, extraction_method, extract_images):
    """
    This function extracts text and images from a file using a specified extraction method and returns
    the extracted data as a dictionary.

    :param file_path: The file path of the PDF file that needs to be extracted
    :param extraction_method: Specifies the python package to use to extract the text from the PDF
    :param extract_images: A boolean value that determines whether or not to extract images from the file.
    :return: a dictionary with two keys: "text" and "images". The value of "text" is a list of strings
    that were extracted from a file using a specified extraction method. The value of "images" is a list
    of base64-encoded images that were extracted from the file if the "extract_images" parameter was set
    to True, otherwise it is an empty list.
    """
    try:
        logging.info("Extracting.... ")
        extracted_text_list = extract_text(file_path, extraction_method)
        extracted_text_list = [
            text_chunk.decode("utf-8").strip() for text_chunk in extracted_text_list
        ]
        extracted_text = " ".join(extracted_text_list).replace("\n", "")
        if extract_images:
            extracted_images = extract_images_base64(file_path)
        else:
            extracted_images = []
        # logging.info('Fininished Extracting in: ', time.time() - s_time)

    except Exception as e:
        return jsonify(error=str(e)), 500

    finally:
        if os.path.exists(file_path):
            os.remove(file_path)
    response_data = {"text": extracted_text, "images": extracted_images}
    return response_data


@app.route("/convertpdf", methods=["POST"])
def convert_pdf():
    """
    This function converts a PDF file to text and extracts images, using a specified extraction
    method and returns the results in JSON format.
    :return: a JSON response containing either an error message and a status code (if there is an
    error), or a dictionary containing the extracted text and images from the uploaded PDF file.
    """
    if "file" not in request.files:
        return jsonify(error="No file part in the request"), 400

    file = request.files["file"]
    if file.filename == "":
        return jsonify(error="No selected file"), 400

    if not allowed_file(file.filename):
        return jsonify(error="File type not allowed"), 400

    extraction_method = request.args.get("extraction_method")
    if extraction_method not in ["pypdf2", "pdfminer", "pymupdf"]:
        return (
            jsonify(
                error=f"Unsupported extraction method '[{extraction_method}]', supported methods are [pypdf2, pdfminer, pymupdf]"
            ),
            400,
        )

    filename = secure_filename(file.filename)
    file_path = os.path.join(UPLOAD_FOLDER, filename)

    try:
        file.save(file_path)
        extracted_text = extract_text(file_path, extraction_method)
        text_data = [text_chunk.decode("utf-8") for text_chunk in extracted_text]
        if request.args.get("extract_images") == "true":
            extracted_images = extract_images_base64(file_path)
        else:
            extracted_images = []

    except Exception as e:
        return jsonify(error=str(e)), 500

    finally:
        if os.path.exists(file_path):
            os.remove(file_path)

    response_data = {"text": text_data, "images": extracted_images}
    return jsonify(response_data)


if __name__ == "__main__":
    app.run()
