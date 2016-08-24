Simple DSL using FreeMonad for Natural Language processing
----------------------------------------------------------

> Warning: this code is just an experiment to play around with NLP and [Free Monad](http://typelevel.org/cats/tut/freemonad.html) .

The current implementation uses [Standford NLP lib](http://nlp.stanford.edu/)
  
###Usage

```
import org.fgb.nlp.dsl._

 // text to be analyzed 
 val text = "I went to Seville last Spring, it's an amazing city in the south of Spain"
            
 // Use the custom dsl to extract information              
 val tasks = for {
      annotatedText <- parseText(text)
      sentiment <- extractSentiment(annotatedText)
      mentions <- extractMentions(annotatedText)
      verbs <- extractVerbs(annotatedText)
      attributes <- extractAttributes(annotatedText)
      facts <- extractFacts(annotatedText)
    } yield TextAnalysis(sentiment, mentions, verbs, attributes, facts)


 val result = tasks.foldMap(Interpreter.default)
 
 println(result.show)
```    

The above code uses an embedded DSL build with [Cats Free Monad](http://typelevel.org/cats/tut/freemonad.html) 
and execute the DSL with the default interpreter (that will rely on on Stanford NLP lib)

`sbt -mem 4096 run` output example: 

```json
{
  "sentiment" : "Positive",
  
  "mentions" : [ "Seville", "Spring", "city", "south", "Spain" ],
  "verbs" : [ "go" ],
  "attributes" : [ "amazing" ],
  "facts" : [
    { "subject" : "Seville", "relationType" : "be", "attribute" : "city" },
    { "subject" : "Seville", "relationType" : "be",  "attribute" : "amazing" },
    { "subject" : "I", "relationType" : "go at_time", "attribute" : "spring" },
    { "subject" : "I", "relationType" : "go at_time", "attribute" : "last spring" },
    { "subject" : "Seville", "relationType" : "be city in","attribute" : "south of Spain" },
    { "subject" : "I", "relationType" : "go to", "attribute" : "Seville" },
    { "subject" : "amazing city", "relationType" : "be in", "attribute" : "south of Spain" },
    { "subject" : "Seville", "relationType" : "be","attribute" : "amazing city"},
    { "subject" : "Seville", "relationType" : "be city in",  "attribute" : "south"},
    { "subject" : "Seville", "relationType" : "be amazing city in", "attribute" : "south of Spain" }
  ]
}
```
