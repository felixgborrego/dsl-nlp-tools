package org.fgb.nlp

import java.util.Properties

import cats._
import edu.stanford.nlp.hcoref.CorefCoreAnnotations
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.simple.Sentence
import org.fgb.nlp.model._
import cats.std.list._
import edu.stanford.nlp.pipeline.CoreNLPProtos.DependencyGraph.Edge
import edu.stanford.nlp.semgraph.SemanticGraphEdge

import scala.collection.JavaConversions._

object Interpreter {

  import adt._

  // Interpreter using edu.stanford.nlp
  implicit val defaultInterpreter = new (AnalysisOp ~> Id) {
    override def apply[A](fa: AnalysisOp[A]): Id[A] = fa match {
      case ParseText(text) => StandfordNLP.parseDoc(text).asInstanceOf[A]
      case ExtractSentiment(document) => StandfordNLP.processSentiment(document).asInstanceOf[A]
      case ExtractMentions(document) => StandfordNLP.extractMentions(document).asInstanceOf[A]
      case ExtractVerbs(document) => StandfordNLP.extractVerbs(document).asInstanceOf[A]
      case ExtractAttributes(document) => StandfordNLP.extractAttributes(document).asInstanceOf[A]
      case ExtractFacts(document) => StandfordNLP.extractFacts(document).asInstanceOf[A]
    }
  }
}

private object StandfordNLP {

  private case class CoReference(target: String, source: String)

  private val props = new Properties()
  props.put("annotators", "tokenize, ssplit, pos, lemma, depparse, natlog, ner, parse, mention, coref, openie, sentiment")
  private val coreNLP = new StanfordCoreNLP(props)

  def parseDoc(text: String): AnnotatedText = {
    val annotated = coreNLP.process(text)
    val sentences = annotated.get(classOf[SentencesAnnotation])
    val sentenceCoreMap = sentences.head
    val sentence = new Sentence(sentenceCoreMap)
    val semanticGraph = sentence.dependencyGraph()
    AnnotatedText(annotated, sentences, semanticGraph)
  }

  def extractMentions(document: AnnotatedText): Seq[Mention] = {

    def extract(filterBy: SemanticGraphEdge => Boolean) =
      document.semanticGraph.edgeIterable().filter(filterBy)
        .map(_.getGovernor.backingLabel().originalText()).toSeq.distinct.filter(_.size > 2)

    val sourceDependencies = extract(edge => edge.getSource.tag() == "NN" || edge.getSource.tag() == "NNP")
    val targetDependencies = extract(edge => edge.getTarget.tag() == "NN" || edge.getTarget.tag() == "NNP")

    (sourceDependencies ++ targetDependencies).distinct
  }

  def extractVerbs(document: AnnotatedText): Seq[Verb] =
    document.semanticGraph.edgeIterable.filter { edge =>
      edge.getGovernor.tag().startsWith("VB")
    }.map(_.getGovernor.backingLabel().lemma()).toSeq.distinct

  def extractAttributes(document: AnnotatedText): Seq[Attribute] =
    document.semanticGraph.edgeIterable().filter { edge =>
      edge.getRelation.getShortName == "amod" || edge.getRelation.getShortName == "advmod"
    }.map(_.getDependent.backingLabel().originalText()).toSeq.distinct

  def extractFacts(document: AnnotatedText): Seq[Fact] = {
    val coreferences = extractCoReference(document)

    // replace pronouns for subjects
    val processCoref = Functor[List].lift { relation: Fact =>
      coreferences match {
        case Some(coref) if (coref.source == relation.subject) =>
          relation.copy(subject = coref.target)
        case _ => relation
      }
    }

    val triples = (for {
      sentence <- document.sentences.toList
      triple <- sentence.get(classOf[NaturalLogicAnnotations.RelationTriplesAnnotation])
    } yield {
      Fact(triple.subjectLemmaGloss(), triple.relationLemmaGloss(), triple.objectLemmaGloss())
    })

    processCoref(triples)
  }


  def processSentiment(document: AnnotatedText): Option[Sentiment] = {
    val sentiment = document.sentences.head.get(classOf[SentimentCoreAnnotations.SentimentClass])
    parseSentiment(sentiment)
  }


  // Extract references between subject and pronouns
  private def extractCoReference(document: AnnotatedText) = {
    val allCoref = for {
      chain <- document.annotations.get(classOf[CorefCoreAnnotations.CorefChainAnnotation]).values()
      corefMention <- chain.getMentionsInTextualOrder
    } yield {
      corefMention.mentionSpan
    }

    allCoref.toList match {
      case target :: source :: tail =>
        Some(CoReference(target, source.toLowerCase))
      case _ => None
    }
  }

  private def parseSentiment(text: String): Option[Sentiment] = Option(text).map {
    case "Positive" => Positive
    case "Negative" => Negative
    case "Neutral" => Neutral
  }
}

