@Grab(group='org.apache.lucene', module='lucene-core', version='5.2.1')
@Grab(group='org.apache.lucene', module='lucene-analyzers-common', version='5.2.1')
@Grab(group='org.apache.lucene', module='lucene-queryparser', version='5.2.1')


import groovy.json.*

import org.apache.lucene.analysis.*
import org.apache.lucene.analysis.standard.*
import org.apache.lucene.document.*
import org.apache.lucene.index.*
import org.apache.lucene.store.*
import org.apache.lucene.util.*
import org.apache.lucene.search.*
import org.apache.lucene.queryparser.classic.*

import java.nio.file.*

if (!application) {
  application = request.getApplication(true)
}
def searcher = application.searcher
def parser = application.parser
def q = request.getParameter("query")


def query = parser.parse("+\""+q+"\"")
ScoreDoc[] hits = searcher.search(query, null, 1000, Sort.RELEVANCE, true, true).scoreDocs
def results = []
hits.each { doc ->
  Document hitDoc = searcher.doc(doc.doc)
  def title = hitDoc.title
  Expando exp = new Expando()
  //  exp.uri = hitDoc.uri
  exp.id = hitDoc.identifier?.replaceAll("<","")?.replaceAll(">","")
  exp.title = hitDoc.title
  exp.description = hitDoc.description
  exp.type = hitDoc.type?.replaceAll("<","")?.replaceAll(">","")
  exp.dataset = hitDoc.dataset?.replaceAll("<","")?.replaceAll(">","")
  results << exp
}
println "<pre>"
println JsonOutput.toJson(results)

