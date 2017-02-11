FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/sltapp.jar /sltapp/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/sltapp/app.jar"]
