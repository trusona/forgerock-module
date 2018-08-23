set -e

export openam_server_username="ec2-user"
export openam_server_host="52.13.216.71"
export openam_server_key="~/.ssh/forgerock.key"

function upload() {
  JAR="forgerock-${1}/target/trusona-forgerock-${1}-${PLUGIN_VERSION}-all.jar"
  scp -i ${openam_server_key} ${JAR} ${openam_server_username}@${openam_server_host}:~

}

PLUGIN_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version 2>/dev/null | egrep -ve "\[.*\]")

upload node
upload module

ssh -i ${openam_server_key} ${openam_server_username}@${openam_server_host} \
  'sudo mv *.jar /usr/share/tomcat7/webapps/openam/WEB-INF/lib/;sudo chown -R root:tomcat /usr/share/tomcat7/webapps/openam/WEB-INF/lib;sudo /etc/init.d/tomcat7 restart'
