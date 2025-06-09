FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} descuento-personas-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/descuento-personas-service.jar"]