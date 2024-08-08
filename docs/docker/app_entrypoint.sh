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

if [[ -z "$APPLICATION_NAME" ]]; then
  echo "ERROR: Please set the APPLICATION_NAME variable in your environment, which usually set in application DockerFile, ENV setting."
  exit 1
fi

if [[ -z "$SERVER_MAIN_CLASS" ]]; then
  echo "ERROR: Please set the SERVER_MAIN_CLASS variable in your environment, which usually set in application DockerFile, ENV setting."
  exit 1
fi


INNOSPOTS_HOME=/innospots/${APPLICATION_NAME}


if [[ ! -d "${INNOSPOTS_HOME}" ]]; then
  mkdir "${INNOSPOTS_HOME}"
fi

# APPLICATION_NAME, system env
# SERVER_MAIN_CLASS, system env

if [[ -z "$JVM_MEM_OPTS" ]]; then
    JAVA_OPT="-Xms128m -Xmx1g -Xss256k -XX:MaxMetaspaceSize=192m -XX:MetaspaceSize=192m"
else
    JAVA_OPT="${JVM_MEM_OPTS}"
fi


PROFILE="${ENV_PROFILE}"

if [[ ${LOGROOTPATH} ]]; then
  LOG_DIR=${LOGROOTPATH}
else
  LOG_DIR=${INNOSPOTS_HOME}/logs
fi

export JAVA="${JAVA_HOME}/bin/java"
export CONFIG_DIR=${INNOSPOTS_HOME}/config
export LOG_DIR


if [[ ${CLASSPATH} ]]; then
    CLASSPATH="${CLASSPATH}:${CONFIG_DIR}:${INNOSPOTS_HOME}/lib/*"
else
    CLASSPATH="${CONFIG_DIR}:${INNOSPOTS_HOME}/lib/*"
fi


JAVA_MAJOR_VERSION=$($JAVA -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p')
if [[ "$JAVA_MAJOR_VERSION" -ge "9" ]]; then
  JAVA_OPT="${JAVA_OPT} -Xlog:gc*:file=${LOG_DIR}/innospots_gc.log:time,tags:filecount=10,filesize=102400"
else
  JAVA_OPT="${JAVA_OPT} -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext"
  JAVA_OPT="${JAVA_OPT} -Xloggc:${LOG_DIR}/innospots_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
fi

JAVA_OPT="${JAVA_OPT} -XX:+UseG1GC"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}/java_heapdump.hprof"
JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages"
JAVA_OPT="${JAVA_OPT} -XX:+ParallelRefProcEnabled"
JAVA_OPT="${JAVA_OPT} -XX:MaxGCPauseMillis=100"
JAVA_OPT="${JAVA_OPT} -XX:+UseStringDeduplication"


if [[ -n "${PROFILE}" ]]; then
  JAVA_OPT="${JAVA_OPT} -Dspring.profiles.active=${PROFILE}"
fi


execute() {
  # start
  echo "${JAVA} ${JAVA_OPT} -cp ${CLASSPATH} ${SERVER_MAIN_CLASS}"
  "${JAVA}" ${JAVA_OPT} -cp "${CLASSPATH}" "${SERVER_MAIN_CLASS}"
}


#create log directory
mkLogDir(){
    if [[ ! -d "${LOG_DIR}" ]]; then
        mkdir "${LOG_DIR}"
    fi
}


startup(){
    mkLogDir
    echo "================================================================================================================"
    echo "Starting application ${SERVER_MAIN_CLASS}, working directory:${INNOSPOTS_HOME}"
    execute
    sleep 1
}

#starting service
startup


