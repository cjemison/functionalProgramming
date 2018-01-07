package com.cjemison.frp;

import com.cjemison.frp.config.WebConfig;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;


public class App {
  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  @SuppressWarnings({"unchecked"})
  public static void main(String[] args) throws Exception {
    final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext
          (WebConfig.class);

    final RouterFunction<ServerResponse> routerFunction = (RouterFunction<ServerResponse>)
          context.getBean("routerFunction");

    final HttpHandler httpHandler = WebHttpHandlerBuilder.webHandler((WebHandler) toHttpHandler
          (routerFunction)).build();
    final ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(httpHandler);
    final Tomcat tomcat = new Tomcat();
    //tomcat.getConnector().setAsyncTimeout(5000);

    Connector nioConnector = new Connector(Http11Nio2Protocol.class.getName());
    nioConnector.setPort(8080);
    nioConnector.setRedirectPort(8080);
    nioConnector.setSecure(false);
    nioConnector.setAsyncTimeout(30000);
    nioConnector.setEnableLookups(true);
    nioConnector.setProperty("maxThreads", "1000");

    tomcat.getService().removeConnector(tomcat.getConnector());
    tomcat.getService().addConnector(nioConnector);
    tomcat.setConnector(nioConnector);

    final Context rootContext =
          tomcat.addContext("", System.getProperty("java.io.tmpdir"));
    Tomcat.addServlet(rootContext, "httpHandlerServlet", servlet);
    rootContext.addServletMappingDecoded("/", "httpHandlerServlet");
    tomcat.start();
    LOGGER.info("Application Started");
    tomcat.getServer().await();
  }
}
