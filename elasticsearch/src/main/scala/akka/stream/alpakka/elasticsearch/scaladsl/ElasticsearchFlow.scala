/*
 * Copyright (C) 2016-2018 Lightbend Inc. <http://www.lightbend.com>
 */

package akka.stream.alpakka.elasticsearch.scaladsl

import akka.NotUsed
import akka.stream.alpakka.elasticsearch._
import akka.stream.alpakka.elasticsearch.impl
import akka.stream.scaladsl.Flow
import org.elasticsearch.client.RestClient
import spray.json._

/**
 * Scala API to create Elasticsearch flows.
 */
object ElasticsearchFlow {

  /**
   * Creates a [[akka.stream.scaladsl.Flow]] for type `T` from [[WriteMessage]] to sequences
   * of [[WriteResult]].
   */
  def create[T](indexName: String,
                typeName: String,
                settings: ElasticsearchWriteSettings = ElasticsearchWriteSettings.Default)(
      implicit client: RestClient,
      writer: JsonWriter[T]
  ): Flow[WriteMessage[T, NotUsed], Seq[WriteResult[T, NotUsed]], NotUsed] =
    create[T](indexName, typeName, settings, new SprayJsonWriter[T]()(writer))

  /**
   * Creates a [[akka.stream.scaladsl.Flow]] for type `T` from [[WriteMessage]] to sequences
   * of [[WriteResult]].
   */
  def create[T](indexName: String, typeName: String, settings: ElasticsearchWriteSettings, writer: MessageWriter[T])(
      implicit client: RestClient
  ): Flow[WriteMessage[T, NotUsed], Seq[WriteResult[T, NotUsed]], NotUsed] =
    Flow
      .fromGraph(
        new impl.ElasticsearchFlowStage[T, NotUsed](
          indexName,
          typeName,
          client,
          settings,
          writer
        )
      )
      .mapAsync(1)(identity)

  /**
   * Creates a [[akka.stream.scaladsl.Flow]] for type `T` from [[WriteMessage]] to lists of [[WriteResult]]
   * with `passThrough` of type `C`.
   */
  def createWithPassThrough[T, C](indexName: String,
                                  typeName: String,
                                  settings: ElasticsearchWriteSettings = ElasticsearchWriteSettings.Default)(
      implicit client: RestClient,
      writer: JsonWriter[T]
  ): Flow[WriteMessage[T, C], Seq[WriteResult[T, C]], NotUsed] =
    createWithPassThrough[T, C](indexName, typeName, settings, new SprayJsonWriter[T]()(writer))

  /**
   * Creates a [[akka.stream.scaladsl.Flow]] for type `T` from [[WriteMessage]] to lists of [[WriteResult]]
   * with `passThrough` of type `C`.
   */
  def createWithPassThrough[T, C](indexName: String,
                                  typeName: String,
                                  settings: ElasticsearchWriteSettings,
                                  writer: MessageWriter[T])(
      implicit client: RestClient
  ): Flow[WriteMessage[T, C], Seq[WriteResult[T, C]], NotUsed] =
    Flow
      .fromGraph(
        new impl.ElasticsearchFlowStage[T, C](
          indexName,
          typeName,
          client,
          settings,
          writer
        )
      )
      .mapAsync(1)(identity)

  private final class SprayJsonWriter[T](implicit writer: JsonWriter[T]) extends MessageWriter[T] {
    override def convert(message: T): String = message.toJson.toString()
  }

}
