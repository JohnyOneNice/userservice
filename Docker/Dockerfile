FROM amazoncorretto:17-alpine
COPY serviceone16.jar projectuser16.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djdk.util.systemMetrics.enabled=false","-Dspring.datasource.driver-class-name=org.postgresql.Driver","-jar","projectuser16.jar"]