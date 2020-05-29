FROM openjdk:15-jdk
RUN jlink --no-header-files --no-man-pages --compress=2 --strip-debug --output /jvm --add-modules \
java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,\
java.management.rmi,jdk.crypto.ec,java.naming,java.prefs,java.rmi,java.scripting,java.security.jgss,\
java.sql,java.xml,jdk.attach,jdk.httpserver,jdk.jdi,jdk.unsupported,jdk.naming.dns,jdk.management
WORKDIR /build
COPY ./target/*.jar app.jar
RUN jar -xf app.jar
RUN mkdir /app && cp -r META-INF /app && cp -r BOOT-INF/classes/* /app

FROM debian:stretch-slim
WORKDIR /data/symphony
COPY --from=0 /jvm /jvm
COPY --from=0 /build/BOOT-INF/lib /lib
COPY --from=0 /app .
ENTRYPOINT [ "/jvm/bin/java", "-cp", ".:/lib/*", "com.symphony.hackathon.BotApplication", "--spring.config.location=application.yaml" ]
