FROM ubuntu:14.04
MAINTAINER István Koren <koren ÄT dbis.rwth-aachen.de>

# Let the container know that there is no tty
ENV DEBIAN_FRONTEND noninteractive

# Update base image
RUN sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list
RUN apt-get update -y
RUN apt-get upgrade -y

# Install build tools
RUN apt-get install -y \
                     openjdk-7-jdk \
                     git

# Set jdk7 as the default JDK
RUN ln -fs /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java /etc/alternatives/java

# create mount point
RUN mkdir /build
WORKDIR /build
VOLUME ["/build"]

# Clone and build code on run
CMD ant clean_all && \
    ant generate_configs  && \
    ant jar
