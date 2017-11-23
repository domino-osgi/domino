#!/bin/sh

set -e

if [ ! -f "staging-settings.xml" ]; then

  cat > staging-settings.xml << EOF
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-username</username>
      <password>your-password</password>
    </server>
  </servers>
</settings>
EOF

fi

echo "Please edit staging-settings.xml with propper connection details."
read

DOMINO_VERSION="1.1.2"
SCALA_VERSIONS="2.10.7 2.11.12 2.12.4"

for scalaVersion in $SCALA_VERSIONS; do

  SCALA_BIN_VERSION=$(echo $scalaVersion | cut -d. -f1,2)

  echo "Building for Scala ${scalaVersion}"
  SCALA_VERSION=${scalaVersion} mvn clean source:jar scala:doc-jar install

  if [ -n "$SKIP_UPLOAD" ] ; then
    echo "Skipping Upload"
  else

    echo "Uploading jar"
    SCALA_VERSION=${scalaVersion} mvn -s ./staging-settings.xml -P deploy gpg:sign-and-deploy-file -DpomFile=.polyglot.pom.scala -Dfile="target/domino_${SCALA_BIN_VERSION}-${DOMINO_VERSION}.jar"

    echo "Uploading sources"
    SCALA_VERSION=${scalaVersion} mvn -s ./staging-settings.xml -P deploy gpg:sign-and-deploy-file -DpomFile=.polyglot.pom.scala -Dfile="target/domino_${SCALA_BIN_VERSION}-${DOMINO_VERSION}-sources.jar" -Dclassifier=sources

    echo "Uploading javadoc"
    SCALA_VERSION=${scalaVersion} mvn -s ./staging-settings.xml -P deploy gpg:sign-and-deploy-file -DpomFile=.polyglot.pom.scala -Dfile="target/domino_${SCALA_BIN_VERSION}-${DOMINO_VERSION}-javadoc.jar" -Dclassifier=javadoc

  fi

done
