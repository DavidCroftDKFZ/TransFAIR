FROM maven:eclipse-temurin AS build

WORKDIR /app

COPY . ./

ENV TZ=Europe/Berlin
RUN mvn install
RUN mv target/transFAIR-*.jar target/transFAIR.jar


FROM eclipse-temurin:17-focal

COPY --from=build /app/target/transFAIR.jar /app/

WORKDIR /app
COPY ./run.sh /app/
USER 1001

#CMD ["java", "-jar", "transFAIR.jar"]
CMD ["sh", "/app/run.sh"]
