FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} descuento-clientes-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/descuento-clientes-service.jar"]