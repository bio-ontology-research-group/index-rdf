@Grab(group='org.apache.jena', module='apache-jena-libs', version='2.13.0')
@Grab(group='org.apache.lucene', module='lucene-core', version='5.2.1')
@Grab(group='org.apache.lucene', module='lucene-analyzers-common', version='5.2.1')
@Grab(group='org.apache.lucene', module='lucene-queryparser', version='5.2.1')

import com.hp.hpl.jena.rdf.model.*
import org.apache.jena.riot.*
import com.hp.hpl.jena.vocabulary.*
import com.hp.hpl.jena.query.*
import com.hp.hpl.jena.graph.*

import org.apache.lucene.analysis.*
import org.apache.lucene.analysis.standard.*
import org.apache.lucene.document.*
import org.apache.lucene.index.*
import org.apache.lucene.store.*
import org.apache.lucene.util.*
import org.apache.lucene.search.*
import org.apache.lucene.queryparser.classic.*

import java.nio.file.*

Path indexPath = FileSystems.getDefault().getPath("bio2rdf-index/")
Directory dir = FSDirectory.open(indexPath)
Analyzer analyzer = new StandardAnalyzer()

DirectoryReader reader = DirectoryReader.open(dir)
IndexSearcher searcher = new IndexSearcher(reader)

def parser = new QueryParser("title", analyzer)
def query = parser.parse("+\""+args[0]+"\"")
ScoreDoc[] hits = searcher.search(query, null, 1000, Sort.RELEVANCE, true, true).scoreDocs
hits.each { doc ->
  Document hitDoc = searcher.doc(doc.doc)
  def title = hitDoc.type
  println title
}
