FROM openjdk:11

# Copy the JAR package into the image
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} cpPaymentProcessing.jar

COPY agent/applicationinsights-agent-3.5.0.jar applicationinsights-agent-3.5.0.jar

COPY agent/applicationinsights.json applicationinsights.json

EXPOSE 8899
ENTRYPOINT["java", "-javaagent:applicationinsights-agent-3.5.0.jar", "-jar", "/cpPaymentProcessing.jar"]