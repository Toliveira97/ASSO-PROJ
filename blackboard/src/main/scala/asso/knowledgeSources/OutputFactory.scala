package asso.knowledgeSources

import java.io.{FileOutputStream, PrintStream}

import asso.objects.{Eof, Message, Value}

object OutputFactory {
  def toFile(filePath: String) = {
    new Output[Long](new PrintStream(new FileOutputStream(filePath, false)));
  }
}


case class Output[T](private val printStream: PrintStream) extends KnowledgeSource[T]() {

  override def execute() {
    if (haveMessages()){
      val message: Message[T] = messagesQueue1.dequeue();
      message.setTopic(nextTopic)
      blackboard.addToQueue(message)
      message match {
        case Value(value1,_) => {
          printStream.println(value1.toString())
        }
        case Eof(_) => {
          println("Output Finished")
          receivedEof = true
        }
      }
    }
  }
}