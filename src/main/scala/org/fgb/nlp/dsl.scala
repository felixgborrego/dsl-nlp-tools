package org.fgb.nlp

import cats.free.Free
import model._

// Algebraic data type representing the operations for nlp
object adt {
  trait AnalysisOp[+A]
  case class ParseText(text: String) extends AnalysisOp[AnnotatedText]
  case class ExtractSentiment(document: AnnotatedText) extends AnalysisOp[Option[Sentiment]]
  case class ExtractMentions(document: AnnotatedText) extends AnalysisOp[Seq[Mention]]
  case class ExtractVerbs(document: AnnotatedText) extends AnalysisOp[Seq[Verb]]
  case class ExtractAttributes(document: AnnotatedText) extends AnalysisOp[Seq[Attribute]]
  case class ExtractFacts(document: AnnotatedText) extends AnalysisOp[Seq[Fact]]
}

object dsl {

  import adt._

  def parseText(text: String): Free[AnalysisOp, AnnotatedText] =
    Free.liftF(ParseText(text))

  def extractSentiment(text: AnnotatedText): Free[AnalysisOp, Option[Sentiment]] =
    Free.liftF(ExtractSentiment(text))


  def extractMentions(text: AnnotatedText): Free[AnalysisOp, Seq[Mention]] =
    Free.liftF(ExtractMentions(text))

  def extractVerbs(text: AnnotatedText): Free[AnalysisOp, Seq[Verb]] =
    Free.liftF(ExtractVerbs(text))

  def extractAttributes(text: AnnotatedText): Free[AnalysisOp, Seq[Attribute]] =
    Free.liftF(ExtractAttributes(text))

  def extractFacts(text: AnnotatedText): Free[AnalysisOp, Seq[Fact]] =
    Free.liftF(ExtractFacts(text))


}

