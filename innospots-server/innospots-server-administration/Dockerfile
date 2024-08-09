From innospots/innospots-base:latest

ENV APPLICATION_NAME innospots-server-administration

ENV SERVER_MAIN_CLASS io.innospots.administration.server.InnospotsAdministrationServer

ENV INNOSPOTS_HOME /innospots/$APPLICATION_NAME

ADD target/$APPLICATION_NAME.tar.gz /innospots/

ENTRYPOINT ["sh","/innospots/bin/app_entrypoint.sh"]