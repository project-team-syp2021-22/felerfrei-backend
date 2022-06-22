FROM openjdk:17-jdk-alpine
COPY target/felerfrei-1.0.jar felerfrei-1.0.jar

# create direcotries
RUN mkdir -p ./images/img
RUN mkdir -p ./orderconfirmations
RUN mkdir -p ./conf
COPY ./conf ./conf

ENTRYPOINT ["java", "-jar", "/felerfrei-1.0.jar"]