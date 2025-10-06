# Use OpenJDK 17 as the base image
FROM openjdk:17-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy all files from current directory to /app
COPY . /app

# Compile all Java files
RUN javac *.java

# Run your main Java GUI class
CMD ["java", "FoodAppGUI"]
