# ✅ Use a lightweight Java runtime image
FROM eclipse-temurin:21-jre-jammy

# ✅ Set working directory inside the container
WORKDIR /app

# ✅ Copy the built JAR file into the container
COPY target/lendrix-0.0.1-SNAPSHOT.jar app.jar

# ✅ Expose the port your Spring Boot app uses
EXPOSE 8080

# ✅ Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
