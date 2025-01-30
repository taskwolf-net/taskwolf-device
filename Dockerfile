FROM openjdk:21

COPY /build/libs/device-1.0.0-SNAPSHOT.jar device.jar
COPY /locale/ /locale/