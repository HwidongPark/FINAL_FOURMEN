FROM openjdk:17

WORKDIR /app


COPY . /app

# gradlew와 startServer.sh 실행 가능하도록 만듦
RUN chmod +x gradlew startServer.sh

COPY build.gradle settings.gradle /build/
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

EXPOSE 80

RUN sed -i -e 's/\r$//' startServer.sh

#CMD ["java", "-jar", "./build/libs/TeamFourmen-0.0.1-SNAPSHOT.jar"]
CMD ./startServer.sh