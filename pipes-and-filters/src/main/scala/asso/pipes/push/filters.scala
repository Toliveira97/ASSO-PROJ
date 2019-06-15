package asso.pipes.push

import asso.pipes.{Eof, Message, Optional, Value}
import scala.collection.mutable.Queue

import scala.concurrent.{ExecutionContext, Future}
import asso.pipes.LongOperations

trait MessageProducer[A] {
  def produce: Future[Message[A]]
}

trait SourceNode[A] extends MessageProducer[A]

trait EndNode[A] {
  def consumeAll(): Unit
}

class SimpleFilter[In, Out](private val source: PushPipe[In], private val operation: In => Optional[Out], implicit private val ec: ExecutionContext, private val queue: Queue[In]) extends MessageProducer[Out] {

  override def produce: Future[Message[Out]] = {
    for {opt <- source.push}
      yield opt match {
        case Value(value) => {
          queue += value
          operation(value)
        }
        case Eof() => {
          Eof()
        }
      }
  }
}

class JoinFilter[In1, In2, Out](private val source1: PushPipe[In1], private val source2: PushPipe[In2], private val operation: (In1, In2) => Optional[Out], implicit private val ec: ExecutionContext, private val queue1: Queue[In1], private val queue2: Queue[In2]) extends MessageProducer[Out] {

  override def produce: Future[Message[Out]] = {
    // separate for parallelism
    val fut1 = source1.push
    val fut2 = source2.push

    for {
      opt1 <- fut1
      opt2 <- fut2
    } yield (opt1, opt2) match {
      case (Eof(), _) => {
        Eof()
      }
      case (_, Eof()) => {
        Eof()
      }
      case (Value(val1), Value(val2)) => {
        queue1 += val1
        queue2 += val2
        operation(val1, val2)
      }
    }
  }
}