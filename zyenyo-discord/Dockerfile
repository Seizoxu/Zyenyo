FROM maven as build
ENV HOME=/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD pom.xml $HOME
RUN mvn verify --fail-never
COPY . .
RUN mvn package

FROM eclipse-temurin:17-jdk-jammy as runner
WORKDIR /app
COPY --from=build /app/target target
COPY ./ZBotData ZBotData

CMD java -cp /app/target/zyenyo*.jar zyenyo.Zyenyo $BOT_TOKEN $MONGO_URI $ZYENYO_ENVIRONMENT 



