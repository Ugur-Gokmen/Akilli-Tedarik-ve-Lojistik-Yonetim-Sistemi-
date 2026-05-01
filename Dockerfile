# 1. Aşama: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Sadece pom.xml kopyala ve bağımlılıkları çek
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Tüm kaynak kodunu kopyala
COPY src ./src

# Projeyi paketle
RUN mvn clean package -DskipTests -B

# 2. Aşama: Run
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# target altındaki JAR dosyasını kopyala
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
