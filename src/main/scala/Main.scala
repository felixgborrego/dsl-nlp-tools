
import cats.syntax.show._
import org.fgb.nlp.dsl._
import org.fgb.nlp.Interpreter.defaultInterpreter
import org.fgb.nlp.model._

object Main extends App {

  def analyseText(text: String): TextAnalysis = {
    val tasks = for {
      annotatedText <- parseText(text)
      sentiment <- extractSentiment(annotatedText)
      mentions <- extractMentions(annotatedText)
      verbs <- extractVerbs(annotatedText)
      attributes <- extractAttributes(annotatedText)
      facts <- extractFacts(annotatedText)
    } yield TextAnalysis(sentiment, mentions, verbs, attributes, facts)


    tasks.foldMap(defaultInterpreter)
  }


  val texts = Seq(
    "I went to Seville last Spring, It's an amazing city in the south of Spain"
  )

  texts.map { text =>
    analyseText(text)
  }.foreach { mpdel =>
    println(mpdel.show)
  }

}
