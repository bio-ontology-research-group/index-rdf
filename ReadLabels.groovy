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
IndexWriterConfig iwc = new IndexWriterConfig(analyzer)
iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE)
iwc.setRAMBufferSizeMB(32768.0)
IndexWriter writer = new IndexWriter(dir, iwc)

def ds = RDFDataMgr.loadDatasetGraph(args[0])

ds.listGraphNodes().each { node ->
  Graph graph = ds.getGraph(node)
  def model = ModelFactory.createModelForGraph(graph)
  def titleProp = ResourceFactory.createProperty("http://purl.org/dc/terms/title")
  def identifierProp = ResourceFactory.createProperty("http://purl.org/dc/terms/identifier")
  def typeProp = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
  def datasetProp = ResourceFactory.createProperty("http://rdfs.org/ns/void#inDataset")
  def descriptionProp = ResourceFactory.createProperty("http://purl.org/dc/terms/description")
  
  model.listStatements(null, titleProp, null).each { stmt ->
    println stmt.getSubject().toString()+"\t"+stmt.getLiteral()?.getString()
    Document doc = new Document()
    model.listStatements(stmt.getSubject(), identifierProp, null).each {
      doc.add(new TextField("identifier", it.getLiteral().getString(), Field.Store.YES))
    }
    model.listStatements(stmt.getSubject(), descriptionProp, null).each {
      doc.add(new TextField("description", it.getLiteral().getString(), Field.Store.YES))
    }
    model.listStatements(stmt.getSubject(), typeProp, null).each {
      doc.add(new TextField("type", it.getObject().toString(), Field.Store.YES))
    }
    model.listStatements(stmt.getSubject(), datasetProp, null).each {
      doc.add(new TextField("dataset", it.getObject().toString(), Field.Store.YES))
    }
    doc.add(new Field("uri", stmt.getSubject().toString(), Field.Store.YES, Field.Index.NO))
    doc.add(new TextField("title", stmt.getLiteral().getString(), Field.Store.YES))
    writer.addDocument(doc)
  }
}

writer.close()
