# Use Debian 12.2 as the base image
FROM debian:12.8

# Add the GPG key for the repository
ADD https://apt.fury.io/authzed/gpg.key /authzed-gpg.key

# Set shell to bash with pipefail option for better error handling
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Install required packages in a single RUN command to reduce layers
# Specifying version numbers for the packages is recommended for reproducibility
RUN apt update && \
    apt install -y --no-install-recommends \
    curl=7.88.1-10+deb12u4 \
    ca-certificates=20230311 \
    gpg=2.2.40-1.1 \
    jq=1.6-2.1 && \
    apt-key add - < /authzed-gpg.key && \
    echo "deb https://apt.fury.io/authzed/ * *" > /etc/apt/sources.list.d/fury.list && \
    apt update && \
    apt install -y --no-install-recommends zed=2.1.11-1 && \
    apt clean && \
    rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /work

# Copy necessary files
COPY /script.sh .
COPY /schema .

# Set the default entrypoint
ENTRYPOINT ["bash", "./script.sh"]
