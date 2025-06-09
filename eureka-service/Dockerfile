FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} eureka-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/eureka-service.jar"]