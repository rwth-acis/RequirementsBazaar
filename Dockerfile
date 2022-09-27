# Build final container without build dependencies etc.
FROM openjdk:17-jdk-alpine

ENV HTTP_PORT=8080
ENV HTTPS_PORT=8443
ENV LAS2PEER_PORT=9011

RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer
RUN apk add --update bash iproute2 && rm -f /var/cache/apk/*


WORKDIR /src
COPY --chown=las2peer:las2peer ./reqbaz/build/export/ .
COPY --chown=las2peer:las2peer docker-entrypoint.sh /src/docker-entrypoint.sh
COPY --chown=las2peer:las2peer gradle.properties /src/gradle.properties
RUN mkdir /src/log && chown -R las2peer:las2peer /src

# run the rest as unprivileged user
USER las2peer

EXPOSE $HTTP_PORT
EXPOSE $HTTPS_PORT
EXPOSE $LAS2PEER_PORT

ENTRYPOINT ["/src/docker-entrypoint.sh"]
