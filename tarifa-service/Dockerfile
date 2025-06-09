FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} tarifa-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/tarifa-service.jar"]