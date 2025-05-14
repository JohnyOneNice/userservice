FROM amazoncorretto:17-alpine
COPY serviceone11.jar projectuser11.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djdk.util.systemMetrics.enabled=false","-Dspring.datasource.driver-class-name=org.postgresql.Driver","-jar","projectuser11.jar"]