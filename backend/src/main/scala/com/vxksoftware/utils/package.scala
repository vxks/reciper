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

  def urlAcquire(url: => URL): ZIO[Any, IOException, Source] =
    ZIO.attemptBlockingIO(Source.fromURL(url))

  def release(source: => Source): ZIO[Any, Nothing, Unit] =
    ZIO.succeedBlocking(source.close())

  def urlSource(url: => URL): ZIO[Scope, IOException, Source] =
    ZIO.acquireRelease(urlAcquire(url))(release(_))
}
