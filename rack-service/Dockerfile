FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} rack-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/rack-service.jar"]