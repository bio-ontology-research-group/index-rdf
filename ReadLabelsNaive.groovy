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
import java.util.zip.GZIPInputStream

Path indexPath = FileSystems.getDefault().getPath("bio2rdf-index/")
Directory dir = FSDirectory.open(indexPath)
Analyzer analyzer = new StandardAnalyzer()
IndexWriterConfig iwc = new IndexWriterConfig(analyzer)
iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
iwc.setRAMBufferSizeMB(32768.0)
IndexWriter writer = new IndexWriter(dir, iwc)

InputStream fileStream = new FileInputStream(args[0])
InputStream gzipStream = new GZIPInputStream(fileStream)
Reader decoder = new InputStreamReader(gzipStream)
BufferedReader buffered = new BufferedReader(decoder)

def map = [:]

buffered.eachLine { line ->
  def toks = line.split(" ")

  Expando exp = null
  
  if (map[toks[0]] == null) {
    exp = new Expando()
    exp.uri = toks[0]
    map[toks[0]] = exp
  } else {
    exp = map[toks[0]]
  }
  
  if (toks[1] == "<http://purl.org/dc/terms/title>") {
    def str = line.substring(line.indexOf("\"")+1)
    str = str.substring(0, str.indexOf("\""))
    exp.title = str
  }
  if (toks[1] == "<http://purl.org/dc/terms/identifier>") {
    def str = line.substring(line.indexOf("\"")+1)
    str = str.substring(0, str.indexOf("\""))
    exp.identifier = str
  }
  if (toks[1] == "<http://purl.org/dc/terms/description>") {
    def str = line.substring(line.indexOf("\"")+1)
    str = str.substring(0, str.indexOf("\""))
    exp.description = str
  }
  if (toks[1] == "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") {
    if (exp.type == null) {
      exp.type = new TreeSet()
    }
    exp.type.add(toks[2])
  }
  if (toks[1] == "<http://rdfs.org/ns/void#inDataset>") {
    if (exp.dataset == null) {
      exp.dataset = new TreeSet()
    }
    exp.dataset.add(toks[2])
  }
}
println "Indexing "+map.size()+" entries."
map.each { k, exp ->
  Document doc = new Document()
  doc.add(new TextField("identifier", k , Field.Store.YES))
  if (exp.title) {
    doc.add(new TextField("title", exp.title , Field.Store.YES))
  }
  if (exp.uri) {
    doc.add(new TextField("uri", exp.uri , Field.Store.YES))
  }
  if (exp.description) {
    doc.add(new TextField("description", exp.description , Field.Store.YES))
  }
  if (exp.identifier) {
    doc.add(new TextField("identifier", exp.identifier , Field.Store.YES))
  }
  exp.type?.each { type ->
    doc.add(new TextField("type", type , Field.Store.YES))
  }
  exp.dataset?.each { dataset ->
    doc.add(new TextField("dataset", dataset , Field.Store.YES))
  }
  writer.addDocument(doc)
}
writer.close()
