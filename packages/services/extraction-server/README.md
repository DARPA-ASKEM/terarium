# PDF Conversion API

This PDF Conversion API is a Flask-based application that allows you to convert PDF files to text and extract images. It provides asynchronous processing using Celery and supports multiple extraction methods. The API accepts PDF files either as direct uploads or via URL.

## Features

1. PDF to text and image extraction
2. File upload and URL download
3. Asynchronous processing with Celery
4. Image extraction in base64-encoded format
5. Support for PyPDF2, PDFMiner, and PyMuPDF extraction methods

## Endpoints

1. `/convertpdfurl` (GET): Accepts a URL to download the PDF file, extraction method, and image extraction options. Returns a task ID for asynchronous processing.
2. `/convertpdftask` (POST): Accepts a PDF file upload, extraction method, and image extraction options. Returns a task ID for asynchronous processing.
3. `/task-result/<task_id>` (GET): Accepts a task ID and returns the status and result of the task in a JSON response.
4. `/convertpdf` (POST): Accepts a PDF file upload and converts it to text and images synchronously. Returns the results in a JSON response.

## Dependencies

The image requires a local running instance of the stack.  More specifically, the service requires a rabbitmq server for its messages.

## Docker Usage

This project includes a `run.sh` script to manage the Docker image and container for the PDF Conversion API. The script provides a simple way to build, start, stop, and restart the application, as well as view logs.

### Commands

```bash
./run.sh build
```

Builds the Docker image.

```bash
./run.sh start {dev|prod}
```

Starts the Docker container for the application in either development or production mode. If the image is not found, it will be built automatically.

- In development mode, the application's source code is mounted as a volume, allowing you to see changes without rebuilding the image.
- In production mode, the application runs using the built image.

```bash
./run.sh stop
```

Stops the running Docker container for the application.

```bash
./run.sh restart
```

Rebuilds the Docker image, stops the running container (if any), and starts a new container in dev mode.

```bash
./run.sh logs
```

Displays the logs for the running Docker container.
