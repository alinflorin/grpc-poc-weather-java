package com.alinflorin.grpc_poc_weather_java;

import java.io.IOException;

public class Boot {
    public static void main(String[] args) throws IOException, InterruptedException {
        final GrpcServer server = new GrpcServer();
        server.start();
        server.blockUntilShutdown();
      }
}
