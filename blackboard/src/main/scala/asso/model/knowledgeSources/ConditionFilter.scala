package asso.model.knowledgeSources

import asso.model.objects.{Eof, Value}

case class ConditionFilter[T](val condition: (T) => Boolean) extends KnowledgeSource[T] {

  override def execute() {
    if (haveMessages()){
      val message = messagesQueue1.dequeue()
      println("CondFilter processing message")
      message.setTopic(nextTopic)
      message match {
        case Value(value1) => {
          println("CondFilter processing value " + value1)
          if (condition(value1)) {
            // advance object stage and put it back in the blackboard
            blackboard.addToQueue(message)
          }
        }
        case Eof() => {
          blackboard.addToQueue(message);
          println("Condition Filter Finished")
          receivedEof = true;
        }
      }
    }

  }
}