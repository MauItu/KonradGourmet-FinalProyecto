# Imagen de cada servicio Spring Boot. El jar lo deja ./plataforma/compilar.sh en <modulo>/target/app.jar.
FROM eclipse-temurin:21-jre-alpine
ARG MODULO
WORKDIR /app
COPY ${MODULO}/target/app.jar app.jar
# En local cada servicio tiene una sola réplica y poca memoria (DEC-58).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
ENTRYPOINT ["java", "-jar", "app.jar"]
