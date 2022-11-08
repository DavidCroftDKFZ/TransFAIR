FROM maven:eclipse-temurin AS build

WORKDIR /app

COPY . ./

RUN mvn install -Dcheckstyle.skip -DskipTests=true  && \
    mv target/transFAIR-*.jar target/transFAIR.jar


FROM eclipse-temurin:17-focal

COPY --from=build /app/target/transFAIR.jar /app/

WORKDIR /app
USER 1001

CMD ["java", "-jar", "transFAIR.jar"]
