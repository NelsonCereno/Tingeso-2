FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} descuento-cumpleanos-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/descuento-cumpleanos-service.jar"]