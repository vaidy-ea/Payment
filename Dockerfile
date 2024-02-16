FROM openjdk:11

# Copy the JAR package into the image
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} cpPaymentProcessing.jar

EXPOSE 8899
ENTRYPOINT ["java","-jar","/cpPaymentProcessing.jar"]