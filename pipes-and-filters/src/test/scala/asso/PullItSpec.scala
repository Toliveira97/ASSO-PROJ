package asso

import java.io.{File, FileNotFoundException, PrintWriter}

import org.scalatest._

import scala.io.Source

object RandomName {
  private var curr = 0

  def nextName: String = {
    curr += 1
    s"ASSO-Proj-test-$curr"
  }
}

class ItSpec extends FlatSpec with Matchers {
  def createFile(body: String = ""): String = {
    val file = File.createTempFile(RandomName.nextName, "txt")
    val printer = new PrintWriter(file)
    printer.print(body)
    printer.close()
    file.toString
  }

  def readFileAsNums(filepath: String): Seq[Long] = {
    val str = Source.fromFile(filepath).mkString

    if (str == "") {
      return Seq()
    }

    str.split("\\s+")
      .map(word => word.toLong)
  }


  "The algorithm" should "should work" in {
    val in1 = createFile("2 3 8 4 7 10 11")
    val in2 = createFile("10 5 5")
    val in3 = createFile("2 4 9 12 11 15 1")
    val in4 = createFile("3 2 3 10 2 5")
    val out = createFile()
    val expectedNums = Seq(8, -1, -3) // manually calculated to be correct

    Main.TestableMain(Array("pull", out, in1, in2, in3, in4))

    val actualNums = readFileAsNums(out)
    expectedNums shouldEqual actualNums
  }

  "The algorithm with empty inputs" should "should produce empty output" in {
    val expectedNums = Seq()

    val out = createFile()
    Main.TestableMain(Array("pull", out, createFile(), createFile(), createFile(), createFile()))

    val actualNums = readFileAsNums(out)
    expectedNums shouldEqual actualNums
  }

  "An invalid command" should "fail" in {
    val invalidCommand = "ASDFGQWE"
    try {
      Main.TestableMain(Array(invalidCommand))
      fail()
    } catch {
      case e: IllegalArgumentException => e.getMessage should include(invalidCommand)
    }
  }

  "An invalid number of commands" should "fail" in {
    try {
      Main.TestableMain(Array("pull", createFile(), createFile()))
      fail()
    } catch {
      case e: IllegalArgumentException => e.getMessage should include("must specify one output")
    }
  }

  "Invalid text in a source file" should "fail" in {
    val invalidText = "awwe"
    try {
      Main.TestableMain(Array("pull", createFile(), createFile("2"), createFile("2"), createFile("2"), createFile(invalidText)))
      fail()
    } catch {
      case e: NumberFormatException => e.getMessage should include(invalidText)
    }
  }

  "Non existing source file" should "fail" in {
    val invalidSource = createFile() + "QQQQQQQ"
    try {
      Main.TestableMain(Array("pull", createFile(), createFile("2"), createFile("2"), createFile("2"), invalidSource))
      fail()
    } catch {
      case e: FileNotFoundException => e.getMessage should include(invalidSource)
    }
  }

}