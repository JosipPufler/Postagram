FROM eclipse-temurin:21-jdk

VOLUME /tmp

COPY target/app.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]