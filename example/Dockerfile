FROM nginx:alpine-perl

ARG RELYING_PARTY_ID=5dfc5568-fbd3-4c1c-80f6-d0cf3d9edc82
ARG DEEPLINK_URL=https://F776D8B7-405B-466D-8F37-9F2D5CCA47DB.staging.applaunchexpress.com/trucode
ARG TRUCODE_URL=https://api.trusona.net
ARG FORGEROCK_REALM=trusona

ENV NGINX_ROOT /usr/share/nginx/html
WORKDIR ${NGINX_ROOT}

COPY *.html $NGINX_ROOT/
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

RUN sed -i "s#RELYING_PARTY_ID#${RELYING_PARTY_ID}#" index.html
RUN sed -i "s#DEEPLINK_URL#${DEEPLINK_URL}#" index.html
RUN sed -i "s#TRUCODE_URL#${TRUCODE_URL}#" index.html
RUN sed -i "s#FORGEROCK_REALM#${FORGEROCK_REALM}#" index.html