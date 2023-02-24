package com.vxksoftware

import zio.*

import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import scala.io.*

package object utils {

  def timed[R, E, A](label: String)(zio: ZIO[R, E, A])(handle: Long => UIO[Unit]): ZIO[R, E, A] =
    for {
      startTime <- Clock.currentTime(TimeUnit.SECONDS)
      result    <- zio
      endTime   <- Clock.currentTime(TimeUnit.SECONDS)
      _         <- handle(endTime - startTime)
    } yield result

  def timedPrint[R, E, A](label: String)(zio: ZIO[R, E, A]): ZIO[R, E, A] =
    timed(label)(zio)(time => Console.printLine(s"""$label executed in: $time seconds""").orDie)

  def acquire(url: => URL): ZIO[Any, IOException, Source] =
    ZIO.attemptBlockingIO(Source.fromURL(url))

  def release(source: => Source): ZIO[Any, Nothing, Unit] =
    ZIO.succeedBlocking(source.close())

  def source(url: => URL): ZIO[Scope, IOException, Source] =
    ZIO.acquireRelease(acquire(url))(release(_))
}
