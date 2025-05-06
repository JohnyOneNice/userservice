FROM amazoncorretto:17-alpine
COPY serviceone4.jar projectuser4.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djdk.util.systemMetrics.enabled=false","-Dspring.datasource.driver-class-name=org.postgresql.Driver","-jar","projectuser4.jar"]