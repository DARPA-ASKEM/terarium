#!/bin/bash

function image_exists() {
    if [ "$(docker images -q extraction-services 2> /dev/null)" == "" ]; then
        return 1
    else
        return 0
    fi
}

case "$1" in
    build)
        echo "Building Docker image..."
        docker build -t extraction-services -f docker/Dockerfile .
    ;;

    start)
        if ! image_exists; then
            echo "Image not found. Building Docker image..."
            docker build -t extraction-services -f docker/Dockerfile .
        fi

        case "$2" in
            dev)
                echo "Starting Docker container in development mode..."
                docker run -d --name extraction-services -p 5000:5000 --rm -v "$(pwd)/src":/app extraction-services
                docker logs -f extraction-services
            ;;

            prod)
                echo "Starting Docker container in production mode..."
                docker run -d --name extraction-services -p 5000:5000 --rm extraction-services
            ;;

            *)
                echo "Usage: ./helper.sh start {dev|prod}"
                exit 1
            ;;
        esac
    ;;

    stop)
        echo "Stopping Docker container..."
        docker stop extraction-services
    ;;

		rebuild)
			echo "Stopping Docker container..."
			docker stop extraction-services
			echo "Building Docker image..."
			docker build -t extraction-services -f docker/Dockerfile .
			echo "Starting Docker container in development mode..."
			docker run -d --name extraction-services -p 5000:5000 --rm -v "$(pwd)/src":/app extraction-services
			docker logs -f extraction-services
		;;

    logs)
        echo "Showing logs..."
        docker logs -f extraction-services
    ;;

    *)
        echo "Usage: ./helper.sh {build|start|stop|logs}"
        exit 1
    ;;
esac
