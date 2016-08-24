package org.fgb.nlp

import cats.Show
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.util.CoreMap

object model {
  sealed trait Sentiment
  case object Positive extends Sentiment
  case object Negative extends Sentiment
  case object Neutral extends Sentiment

  type Mention = String
  type Verb = String
  type Attribute = String

  case class Fact(subject: String, relationType: String, attribute: String)
  case class AnnotatedText(annotations: Annotation, sentences: Seq[CoreMap], semanticGraph: SemanticGraph)
  case class TextAnalysis(sentiment: Option[Sentiment],
                          mentions: Seq[Mention],
                          verbs: Seq[Verb],
                          attributes: Seq[Attribute],
                          facts: Seq[Fact]
                         )

  object TextAnalysis {

    import cats.data.Xor
    import io.circe.{Decoder, DecodingFailure, Encoder, Json}
    import io.circe.syntax._
    import io.circe.generic.auto._

    // Note it may no be needed for Circe 0.5. Render Object as Strings
    implicit val decodeSentiment: Decoder[Sentiment] = Decoder.instance { cursor =>
      cursor.as[String].flatMap {
        case "Positive" => Xor.right(Positive)
        case "Negative" => Xor.right(Negative)
        case "Neutral" => Xor.right(Neutral)

        case _ => Xor.left(DecodingFailure("Sentiment", cursor.history))
      }
    }

    implicit val encodeSentiment: Encoder[Sentiment] = Encoder.instance {
      case Positive => Json.fromString("Positive")
      case Negative => Json.fromString("Negative")
      case Neutral => Json.fromString("Neutral")
    }


    implicit val textAnalysisShow = Show.show[TextAnalysis] { model =>
      model.asJson.spaces2
    }
  }
}
