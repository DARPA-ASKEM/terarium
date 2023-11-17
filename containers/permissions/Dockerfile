FROM debian:12.2

ADD https://apt.fury.io/authzed/gpg.key /authzed-gpg.key

RUN apt-get update && apt-get install -y curl ca-certificates gpg jq
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-key add - < /authzed-gpg.key
RUN echo "deb https://apt.fury.io/authzed/ * *" > /etc/apt/sources.list.d/fury.list
RUN apt-get update && apt-get install -y zed

WORKDIR /work
COPY /script.sh .
COPY /schema .

ENTRYPOINT ["bash", "./script.sh"]

