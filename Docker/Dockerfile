FROM openjdk:17
COPY SeregaProject.jar project.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.datasource.driver-class-name=org.postgresql.Driver","-jar", "project.jar"]