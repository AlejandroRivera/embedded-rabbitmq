FROM maven:3.5.4-jdk-7-slim

# Install Erlang
RUN apt-get update && \
    apt-get install -y \
      erlang \
    && rm -rf /var/lib/apt/lists/*
