FROM java:virtual-jre-alpine

MAINTAINER dkorotych@gmail.com

RUN apk add --no-cache curl bash zip unzip
SHELL ["/bin/bash", "-c", "-l"]

ARG LAZYBONES_VERSION
ENV LAZYBONES_VERSION ${LAZYBONES_VERSION:-0.8.3}

ENV GROUP_NAME lazybones
ENV USER_NAME lazybones
ENV APPLICATION_DIR /home/${USER_NAME}/app

RUN addgroup ${GROUP_NAME} \
 && adduser -s /bin/bash -D -G ${GROUP_NAME} ${USER_NAME} \
 && mkdir "${APPLICATION_DIR}" \
 && chown -R "${GROUP_NAME}:${USER_NAME}" /home/${USER_NAME}

USER ${USER_NAME}
RUN curl -s "https://get.sdkman.io" | /bin/bash
RUN source ${HOME}/.sdkman/bin/sdkman-init.sh
RUN sdk install lazybones ${LAZYBONES_VERSION}
RUN sdk flush archives \
 && sdk flush broadcast \
 && sdk flush candidates \
 && sdk flush temp

COPY entrypoint.sh /
ENV PATH /home/${USER_NAME}/.sdkman/candidates/lazybones/${LAZYBONES_VERSION}/bin:${PATH}
WORKDIR ${APPLICATION_DIR}
VOLUME ["${APPLICATION_DIR}"]

USER root
RUN chmod +x /entrypoint.sh

USER ${USER_NAME}
ENTRYPOINT ["/entrypoint.sh"]
