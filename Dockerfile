# cp /lib/x86_64-linux-gnu/libc.so.6  target/ && cp /lib64/ld-linux-x86-64.so.2 target/ && docker build -t rmannibucau/drone-plugin-mattermost . && docker push rmannibucau/drone-plugin-mattermost
FROM scratch
COPY target/libc.so.6 /lib/x86_64-linux-gnu/libc.so.6
COPY target/ld-linux-x86-64.so.2 /lib64/ld-linux-x86-64.so.2
COPY target/drone-plugin-mattermost /opt/rmannibucau/drone-plugin-mattermost
ENTRYPOINT ["/lib64/ld-linux-x86-64.so.2", "/opt/rmannibucau/drone-plugin-mattermost"]
