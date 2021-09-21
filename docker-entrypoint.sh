#!/usr/bin/env bash

set -e

# print all comands to console if DEBUG is set
if [[ ! -z "${DEBUG}" ]]; then
  set -x
fi

# set some helpful variables
export SERVICE_PROPERTY_FILE='etc/de.rwth.dbis.acis.bazaar.service.BazaarService.properties'
export WEB_CONNECTOR_PROPERTY_FILE='etc/i5.las2peer.webConnector.WebConnector.properties'

export SERVICE_VERSION=$(awk -F "=" '/service.version/ {print $2}' gradle.properties)
export SERVICE_NAME=$(awk -F "=" '/service.name/ {print $2}' gradle.properties)
export SERVICE_CLASS=$(awk -F "=" '/service.class/ {print $2}' gradle.properties)
export SERVICE=${SERVICE_NAME}.${SERVICE_CLASS}@${SERVICE_VERSION}
export POSTGRES_DATABASE='reqbaz'

# check mandatory variables
[[ -z "${POSTGRES_USER}" ]] &&
  echo "Mandatory variable POSTGRES_USER is not set. Add -e POSTGRES_USER=reqbaz to your arguments." && exit 1
[[ -z "${POSTGRES_PASSWORD}" ]] &&
  echo "Mandatory variable POSTGRES_PASSWORD is not set. Add -e POSTGRES_PASSWORD=mypasswd to your arguments." && exit 1

# set defaults for optional service parameters
[[ -z "${POSTGRES_HOST}" ]] && export POSTGRES_HOST='postgres'
[[ -z "${POSTGRES_PORT}" ]] && export POSTGRES_PORT='5432'

[[ -z "${SERVICE_PASSPHRASE}" ]] && export SERVICE_PASSPHRASE='Passphrase'
[[ -z "${BAZAAR_LANG}" ]] && export BAZAAR_LANG='en'
[[ -z "${BAZAAR_COUNTRY}" ]] && export BAZAAR_COUNTRY='us'
[[ -z "${BASE_URL}" ]] && export BASE_URL='http://localhost:8080/bazaar/'
[[ -z "${FRONTEND_BASE_URL}" ]] && export FRONTEND_BASE_URL='http://localhost:5000/'
[[ -z "${ACTIVITY_TRACKER_SERVICE}" ]] && export ACTIVITY_TRACKER_SERVICE=''
[[ -z "${ACTIVITY_TRACKER_ORIGIN}" ]] && export ACTIVITY_TRACKER_ORIGIN='https://requirements-bazaar.org'
[[ -z "${SMTP_SERVER}" ]] && export SMTP_SERVER=''
[[ -z "${EMAIL_FROM_ADDRESS}" ]] && export EMAIL_FROM_ADDRESS=''
[[ -z "${EMAIL_SUMMARY_TIME_PERIOD_IN_MINUTES}" ]] && export EMAIL_SUMMARY_TIME_PERIOD_IN_MINUTES=''

# set defaults for optional web connector parameters
[[ -z "${START_HTTP}" ]] && export START_HTTP='TRUE'
[[ -z "${START_HTTPS}" ]] && export START_HTTPS='FALSE'
[[ -z "${SSL_KEYSTORE}" ]] && export SSL_KEYSTORE=''
[[ -z "${SSL_KEY_PASSWORD}" ]] && export SSL_KEY_PASSWORD=''
[[ -z "${CROSS_ORIGIN_RESOURCE_DOMAIN}" ]] && export CROSS_ORIGIN_RESOURCE_DOMAIN='*'
[[ -z "${CROSS_ORIGIN_RESOURCE_MAX_AGE}" ]] && export CROSS_ORIGIN_RESOURCE_MAX_AGE='60'
[[ -z "${ENABLE_CROSS_ORIGIN_RESOURCE_SHARING}" ]] && export ENABLE_CROSS_ORIGIN_RESOURCE_SHARING='TRUE'
[[ -z "${OIDC_PROVIDERS}" ]] && export OIDC_PROVIDERS='https://api.learning-layers.eu/o/oauth2,https://accounts.google.com'

# configure service properties

function set_in_service_config() {
  sed -i "s?${1}[[:blank:]]*=.*?${1}=${2}?g" ${SERVICE_PROPERTY_FILE}
}
set_in_service_config dbUserName ${POSTGRES_USER}
set_in_service_config dbPassword ${POSTGRES_PASSWORD}
set_in_service_config dbUrl "jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DATABASE}"
set_in_service_config lang ${BAZAAR_LANG}
set_in_service_config country ${BAZAAR_COUNTRY}
set_in_service_config baseURL ${BASE_URL}
set_in_service_config frontendBaseURL ${FRONTEND_BASE_URL}
set_in_service_config activityTrackerService ${ACTIVITY_TRACKER_SERVICE}
set_in_service_config activityOrigin ${ACTIVITY_TRACKER_ORIGIN}
set_in_service_config smtpServer ${SMTP_SERVER}
set_in_service_config emailFromAddress ${EMAIL_FROM_ADDRESS}
set_in_service_config emailSummaryTimePeriodInMinutes ${EMAIL_SUMMARY_TIME_PERIOD_IN_MINUTES}

# configure web connector properties

function set_in_web_config() {
  sed -i "s?${1}[[:blank:]]*=.*?${1}=${2}?g" ${WEB_CONNECTOR_PROPERTY_FILE}
}
set_in_web_config httpPort ${HTTP_PORT}
set_in_web_config httpsPort ${HTTPS_PORT}
set_in_web_config startHttp ${START_HTTP}
set_in_web_config startHttps ${START_HTTPS}
set_in_web_config sslKeystore ${SSL_KEYSTORE}
set_in_web_config sslKeyPassword ${SSL_KEY_PASSWORD}
set_in_web_config crossOriginResourceDomain ${CROSS_ORIGIN_RESOURCE_DOMAIN}
set_in_web_config crossOriginResourceMaxAge ${CROSS_ORIGIN_RESOURCE_MAX_AGE}
set_in_web_config enableCrossOriginResourceSharing ${ENABLE_CROSS_ORIGIN_RESOURCE_SHARING}
set_in_web_config oidcProviders ${OIDC_PROVIDERS}

# set pod ip in pastry conf
if [[ ! -z "${POD_IP}" ]]; then
  echo external_address = ${POD_IP}:${LAS2PEER_PORT} > etc/pastry.properties
  echo socket_bindAddress = ${POD_IP} >> etc/pastry.properties
fi

# wait for any bootstrap host to be available
if [[ ! -z "${BOOTSTRAP}" ]]; then
  echo "Waiting for any bootstrap host to become available..."
  for host_port in ${BOOTSTRAP//,/ }; do
    arr_host_port=(${host_port//:/ })
    host=${arr_host_port[0]}
    port=${arr_host_port[1]}
    if { </dev/tcp/${host}/${port}; } 2>/dev/null; then
      echo "${host_port} is available. Continuing..."
      break
    fi
  done
fi

# prevent glob expansion in lib/*
set -f
LAUNCH_COMMAND='java -cp lib/*:service/* i5.las2peer.tools.L2pNodeLauncher -s service -p '"${LAS2PEER_PORT} ${SERVICE_EXTRA_ARGS}"
if [[ ! -z "${BOOTSTRAP}" ]]; then
  LAUNCH_COMMAND="${LAUNCH_COMMAND} -b ${BOOTSTRAP}"
fi

# start the service within a las2peer node
if [[ -z "${@}" ]]; then
  exec ${LAUNCH_COMMAND} startService\("'""${SERVICE}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector
else
  exec ${LAUNCH_COMMAND} ${@}
fi
