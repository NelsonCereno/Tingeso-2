FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} reserva-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/reserva-service.jar"]