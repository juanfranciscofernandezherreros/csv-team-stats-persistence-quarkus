FROM eclipse-temurin:21-jre
WORKDIR /work
COPY target/quarkus-app/ /work/
ENV JAVA_OPTS="-XX:MaxRAMPercentage=60"
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /work/quarkus-run.jar"]
