package asso.pipes.pull.numbers

import java.io.{FileInputStream, InputStream, PrintStream}
import java.util.Scanner

import asso.pipes.pull.{EndNode, MessageProducer, PullPipe, SourceNode}
import asso.pipes.{Eof, NoValue, NotNone, Value}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, blocking}
import scala.util.control.Breaks._

object ProducerConsumerFactory {
  def producerFromFile(filepath: String): SourceNode[Long] = new LongProducer(new FileInputStream(filepath))

  def producerFromConsole(): SourceNode[Long] = new LongProducer(System.in)

  def consumerToFile(filepath: String, messageProducer: MessageProducer[Long]): PullPipe[Long] => EndNode[Long] = messageProducer => new LongConsumer(new PrintStream(filepath), messageProducer)

  def consumerToConsole(messageProducer: MessageProducer[Long]): PullPipe[Long] => EndNode[Long] = messageProducer => new LongConsumer(System.out, messageProducer)
}

class LongProducer(private val is: InputStream) extends SourceNode[Long] {
  private val scanner = new Scanner(is)

  override def produce: Future[NotNone[Long]] = Future {
    blocking { // TODO is this correct
      if (scanner.hasNext()) {
        Value(scanner.next().toLong)
      } else {
        Eof()
      }
    }
  }
}

class LongConsumer(private val printer: PrintStream, private val pipe: PullPipe[Long]) extends EndNode[Long] {

  private def getValue = Await.result(pipe.pull, 5.second)

  override def consumeAll() = {
    // TODO make loop scala like
    breakable {
      while (true) {
        getValue match {
          case Value(value) => printer.println(value)
          case Eof() => break
        }
      }
    }
  }
}