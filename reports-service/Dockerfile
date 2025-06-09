FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} reports-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/reports-service.jar"]