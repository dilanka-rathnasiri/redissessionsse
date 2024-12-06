FROM amazoncorretto:21-alpine
WORKDIR /app

COPY target/redissessionsse-0.0.1.jar /app/app.jar

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
