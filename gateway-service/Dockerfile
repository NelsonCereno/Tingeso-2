FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} gateway-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/gateway-service.jar"]