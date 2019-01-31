FROM openjdk:8-jre-alpine

COPY ./target/scala-2.11/reprobate-assembly-1.0-SNAPSHOT.jar reprobate.jar

EXPOSE 8473

CMD ["sh", "-c", "java -Xss8m -Xmx512m -jar reprobate.jar"]