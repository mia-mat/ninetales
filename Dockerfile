# Build
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -B
RUN mvn dependency:copy-dependencies -DoutputDirectory=target/lib -B

# ---- Runtime
FROM eclipse-temurin:21-jdk

WORKDIR /app

LABEL internal-port="8080"

LABEL arachne.name="Ninetales Discord Bot"
LABEL arachne.version="1.3.0"

LABEL ninetales.update-note="Probably a bad idea"

# Copy compiled classes and dependency jars (not the fat jar)
COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/lib ./lib

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "classes:lib/*", "ws.mia.ninetales.NinetalesApplication"]