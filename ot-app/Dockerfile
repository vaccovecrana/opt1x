FROM busybox:stable-glibc as runtime

COPY ./libz.so.1 /lib/
COPY ./build/native/nativeCompile/opt1x-linux-amd64 /opt1x-linux-amd64
RUN rm -rfv /bin/*

ENTRYPOINT ["/opt1x-linux-amd64"]