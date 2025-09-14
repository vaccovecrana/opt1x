FROM busybox:stable-glibc as runtime

COPY ./libz.so.1 /lib/
COPY ./build/native/nativeCompile/opt1x-linux-amd64 /opt1x-linux-amd64
COPY ./build/resources/main/NOTICE ./build/resources/main/LICENSE /
RUN rm -rfv /bin/*

EXPOSE 7070

ENTRYPOINT ["/opt1x-linux-amd64"]