From innospots-base:latest

ENV APPLICATION_NAME innospots-server-administration

ENV SERVER_MAIN_CLASS io.innospots.administration.server.InnospotsAdministrationServer

ADD target/$APPLICATION_NAME.tar.gz /innospots/

ENTRYPOINT ["sh","/innospots/bin/app_entrypoint.sh"]