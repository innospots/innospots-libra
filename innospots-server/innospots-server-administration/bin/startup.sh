#!/usr/bin/env bash

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#JAVA_HOME
if [[ -z "$JAVA_HOME" ]]; then
  echo "ERROR: Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better!"
  exit 1
fi

CLASSPATH="${JAVA_HOME}/lib/tools.jar:${JAVA_HOME}/lib/dt.jar"


#Profile env
if [[ -n "$1" ]]; then
  PROFILE=$1
else
  PROFILE="dev"
fi

#Spring profile
export PROFILE
export SERVER_MAIN_CLASS="io.innospots.administration.server.InnospotsAdministrationServer"


APP_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd | sed 's/\/bin//')
INNOS_ROOT=$(dirname "${APP_DIR}")

export APP_DIR INNOS_ROOT
export PID_FILE=${APP_DIR}/proc_pid

export JAVA_HOME
export JAVA="${JAVA_HOME}/bin/java"
export CONFIG_DIR=${APP_DIR}/config
export LOG_PATH="${APP_DIR}/logs"
export LOG_FILE="${LOG_PATH}/innospot.log"


#LOG_FILE="/dev/null"


echo "application deploy directory: ${APP_DIR}"

#CLASSPATH="${CONFIG_DIR}:${APP_DIR}/lib/*"
CLASSPATH="${CLASSPATH}:${APP_DIR}/lib/*:${APP_DIR}/config:${INNOS_ROOT}/ext_config:${INNOS_ROOT}/ext_lib/*"

echo "application ext lib directory: ${INNOS_ROOT}/ext_lib/"


if [[ "${PROFILE}" == "dev" ]]; then
  JAVA_OPT="-Xms256m -Xmx1g -Xss1m"
else
  JAVA_OPT="-Xms2g -Xmx2g -Xss1m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
  JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_PATH}/java_heapdump.hprof"
  JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages"
fi

JAVA_MAJOR_VERSION=$($JAVA -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p')
if [[ "$JAVA_MAJOR_VERSION" -ge "9" ]]; then
  JAVA_OPT="${JAVA_OPT} -Xlog:gc*:file=${LOG_PATH}/innospot_gc.log:time,tags:filecount=10,filesize=102400"
else
  JAVA_OPT="${JAVA_OPT} -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext"
  JAVA_OPT="${JAVA_OPT} -Xloggc:${LOG_PATH}/gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
fi

if [[ ${PROFILE} ]]; then
  JAVA_OPT="${JAVA_OPT} -Dspring.profiles.active=${PROFILE}"
fi


#JAVA_OPT="${JAVA_OPT} -Dinnospot.home=${APP_DIR}"
#JAVA_OPT="${JAVA_OPT} --spring.config.additional-location=${CONFIG_DIR}"
#JAVA_OPT="${JAVA_OPT} --logging.config=${CONFIG_DIR}/log4j2.xml"

JAVA_OPT="${JAVA_OPT} -DINNOS_ROOT=${APP_DIR}"


initLog() {
  if [ ! -d "${LOG_PATH}" ]; then
    mkdir "${LOG_PATH}"
  fi

  echo "log out directory: ${LOG_PATH}"

  # check the start.out log output file
  if [ ! -f "${LOG_FILE}" ]; then
    touch "${LOG_FILE}"
  fi
}


TPID=0

#execute java -jar
execute() {
  # start
  echo "${JAVA} ${JAVA_OPT} -cp ${CLASSPATH} ${SERVER_MAIN_CLASS} > ${LOG_FILE}"
  nohup "${JAVA}" ${JAVA_OPT} -cp "${CLASSPATH}" "${SERVER_MAIN_CLASS}" > "${LOG_FILE}" 2>&1 &
}


#write pid file
writePid() {
  if [[ ! -f ${PID_FILE} ]]; then
    touch "${PID_FILE}"
  fi
  echo "application pid : $!"
  echo $! >"${PID_FILE}"
}

#remove pid file
rmPid() {
  if [[ -e ${PID_FILE} ]]; then
    rm -f "${PID_FILE}"
  fi
}

getPid(){
  TPID=$(pgrep -f "active=${PROFILE}.*${SERVER_MAIN_CLASS}")
}

startup() {
  getPid
  initLog
  echo "================================================================================================================"
  if [[ ${TPID} -ne 0 ]]; then
    echo "${SERVER_MAIN_CLASS} profile=${PROFILE} already started (PID=${TPID})"
    echo "================================================================================================================"
  else
    rmPid
    echo "Starting ${SERVER_MAIN_CLASS}"
    execute
    sleep 1
    writePid
    getPid
    if [[ ${TPID} -ne 0 ]]; then
      echo "${SERVER_MAIN_CLASS} profile=${PROFILE} (PID=${TPID})...[Success]"
      echo "================================================================================================================"
      echo "tail log file : ${LOG_FILE}"
    else
      echo -e "\033[31m ${SERVER_MAIN_CLASS} profile=${PROFILE} boot failed. [Failed] \033[0m"
      echo "================================================================================================================"
      rmPid
    fi
  fi
}


startup
