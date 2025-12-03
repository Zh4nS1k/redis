FROM eclipse-temurin:17-jdk-jammy
LABEL maintainer="aluahub"

WORKDIR /app
VOLUME /tmp

COPY bookService.jar service.jar

ENTRYPOINT ["java", "-jar", "service.jar"]
