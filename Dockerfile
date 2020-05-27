FROM adoptopenjdk/openjdk14:armv7l-debian-jdk-14.0.1_7
RUN jlink --no-header-files --no-man-pages --compress=2 --strip-java-debug-attributes --output /jvm --add-modules \
java.base,java.compiler,java.instrument,java.logging,java.management,java.naming,java.prefs,\
java.rmi,java.security.jgss,java.security.sasl,java.transaction.xa,java.xml,\
jdk.httpserver,jdk.management,jdk.unsupported,jdk.naming.dns,jdk.crypto.ec,java.desktop,jdk.naming.dns
WORKDIR /build
COPY ./target/*.jar app.jar
RUN jar -xf app.jar
RUN mkdir /app && cp -r META-INF /app && cp -r BOOT-INF/classes/* /app

FROM arm32v7/debian:stretch-slim
WORKDIR /data/symphony
COPY rsa rsa
COPY --from=0 /jvm /jvm
COPY --from=0 /build/BOOT-INF/lib /lib
COPY --from=0 /app .
ENTRYPOINT [ "/jvm/bin/java", "-cp", ".:/lib/*", "com.symphony.hackathon.BotApplication", "--spring.config.location=application.yaml" ]
