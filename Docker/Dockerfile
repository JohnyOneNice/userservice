FROM amazoncorretto:17-alpine
COPY serviceone8.jar projectuser8.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djdk.util.systemMetrics.enabled=false","-Dspring.datasource.driver-class-name=org.postgresql.Driver","-jar","projectuser8.jar"]