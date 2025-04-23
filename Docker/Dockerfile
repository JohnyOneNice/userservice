FROM amazoncorretto:17-alpine
COPY serviceone.jar projectuser.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djdk.util.systemMetrics.enabled=false","-Dspring.datasource.driver-class-name=org.postgresql.Driver","-jar","projectuser.jar"]