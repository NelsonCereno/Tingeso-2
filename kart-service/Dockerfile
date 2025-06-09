FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} kart-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/kart-service.jar"]