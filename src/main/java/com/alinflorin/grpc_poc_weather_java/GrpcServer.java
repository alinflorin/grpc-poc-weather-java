package com.alinflorin.grpc_poc_weather_java;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import weather.*;
import weather.WeatherOuterClass.GetWeatherReply;
import weather.WeatherOuterClass.GetWeatherRequest;
import darksky.Darksky.GetWindRequest;
import darksky.Darksky.GetTempRequest;
import darksky.*;
import darksky.DarkSkyGrpc.DarkSkyBlockingStub;

public class GrpcServer {
  private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());
  private Server server;

  public void start() throws IOException {
    int port = 9090;
    server = ServerBuilder.forPort(port).addService(new WeatherService()).build().start();
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

  static class WeatherService extends WeatherGrpc.WeatherImplBase {
    @Override
    public void getWeather(GetWeatherRequest request, StreamObserver<GetWeatherReply> responseObserver) {
      GetWindRequest wReq = GetWindRequest.newBuilder().setAddress(request.getAddress())
          .setUseMetric(request.getUseMetric()).build();
      GetTempRequest tReq = GetTempRequest.newBuilder().setAddress(request.getAddress())
          .setUseMetric(request.getUseMetric()).build();
      ManagedChannel channel = ManagedChannelBuilder.forAddress("grpc-poc-darksky-mock-node", 3000).usePlaintext()
          .build();

      DarkSkyBlockingStub stub = DarkSkyGrpc.newBlockingStub(channel);
      var wReply = stub.getWind(wReq);
      var tReply = stub.getTemp(tReq);
      String summary = "";
      if (wReply.getCurrentWind() > 30) {
        summary += "windy";
      } else {
        summary += "no-wind";
      }
      summary += " ";
      if (tReply.getCurrentTemp() > 25) {
        summary += "hot";
      } else if (tReply.getCurrentTemp() < 12) {
        summary += "cold";
      } else {
        summary += "normal";
      }
      
      GetWeatherReply reply = GetWeatherReply.newBuilder()
          .setSummary(summary)
          .setCurrentTemp(tReply.getCurrentTemp())
          .setCurrentWindSpeed(wReply.getCurrentWind()).build();

      responseObserver.onNext(reply);
      responseObserver.onCompleted();
      channel.shutdown();
    }
  }
}