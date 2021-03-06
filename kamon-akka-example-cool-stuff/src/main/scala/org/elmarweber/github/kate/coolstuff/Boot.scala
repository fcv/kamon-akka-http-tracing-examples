package org.elmarweber.github.kate.coolstuff

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import kamon.Kamon
import kamon.jaeger.Jaeger
import org.elmarweber.github.kate.lib.api.{AnalyticsPipelineApiClient, ProfileHttpApiClient}
import org.elmarweber.github.kate.lib.httpclient.HttpClient
import org.elmarweber.github.kate.lib.kamon.LogSpanReporter

object Boot extends App with ServiceRoute with StrictLogging {
  Kamon.addReporter(new LogSpanReporter())
  Kamon.addReporter(new Jaeger())

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val profileApi = new ProfileHttpApiClient(HttpClient.fromEndpoint(Configuration.service.profile.endpoint).build())
  val analyticsPipelineApi = new AnalyticsPipelineApiClient(HttpClient.fromEndpoint(Configuration.service.analyticsPipeline.endpoint).build())

  Http().bindAndHandle(serviceRoute, Configuration.service.http.interface, Configuration.service.http.port).transform(
    binding => logger.info(s"REST interface bound to ${binding.localAddress} "), { t => logger.error(s"Couldn't bind interface: ${t.getMessage}", t); sys.exit(1) }
  )
}
