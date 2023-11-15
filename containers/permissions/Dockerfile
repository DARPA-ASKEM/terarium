FROM debian:12.2

ADD https://apt.fury.io/authzed/gpg.key /authzed-gpg.key

RUN apt update && apt install -y curl ca-certificates gpg jq
RUN cat /authzed-gpg.key | apt-key add -
RUN echo "deb https://apt.fury.io/authzed/ * *" > /etc/apt/sources.list.d/fury.list
RUN apt update && apt install -y zed

WORKDIR /work
COPY /script.sh .
COPY /schema .

ENTRYPOINT ["sh", "./script.sh"]

