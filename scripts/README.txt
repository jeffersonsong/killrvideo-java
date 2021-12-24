Setup in Linux

Open up firewall
sudo ufw status
sudo ufw allow 50101

Add "host.docker.internal:host-gateway" to extra-host
    extra_hosts:
      - "host.docker.internal:host-gateway"
      - "backend:$KILLRVIDEO_BACKEND"

    environment:
       KILLRVIDEO_YOUTUBE_API_KEY: xxxx

Modify run-docker-backend-external.sh
export KILLRVIDEO_BACKEND='172.17.0.1'

https://stackoverflow.com/questions/31324981/how-to-access-host-port-from-docker-container/43541732#43541732