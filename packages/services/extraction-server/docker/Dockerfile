# Use an official Python runtime as a parent image
FROM python:3.9-slim

RUN apt-get update && apt-get install -y supervisor ffmpeg libsm6 libxext6

COPY docker/requirements.txt .

# Install any needed packages specified in requirements.txt
RUN pip install --trusted-host pypi.python.org -r requirements.txt

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY ../src /app

# Make port 5000 available to the world outside this container
EXPOSE 5000

# Run app.py when the container launches
CMD ["supervisord", "-c", "/app/supervisord.conf"]
