FROM openjdk:17

COPY target/fhir-transFAIR.jar /app/

WORKDIR /app
USER 1001

CMD ["java", "-jar", "fhir-transFAIR.jar"]
