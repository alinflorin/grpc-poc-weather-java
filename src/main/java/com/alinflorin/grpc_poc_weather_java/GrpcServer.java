package com.alinflorin.grpc_poc_weather_java;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcServer {
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());
    private Server server;

    public void start() throws IOException {
        int port = 9090;
        server = ServerBuilder.forPort(port)
            // .addService(new GreeterImpl())
            .build()
            .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run() {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                GrpcServer.this.stop();
            } catch (InterruptedException e) {
              e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
          }
        });
      }
    
      public void stop() throws InterruptedException {
        if (server != null) {
          server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
      }

      public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
          server.awaitTermination();
        }
      }
}