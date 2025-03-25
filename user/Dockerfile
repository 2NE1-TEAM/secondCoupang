FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN chmod +x gradlew
CMD ["./gradlew", "bootRun"]
EXPOSE 8080