@Grapes([
	  @Grab('org.eclipse.jetty:jetty-server:9.0.0.M5'),
	  @Grab('org.eclipse.jetty:jetty-servlet:9.0.0.M5'),
	  @Grab('javax.servlet:javax.servlet-api:3.0.1'),
	  @GrabExclude('org.eclipse.jetty.orbit:javax.servlet:3.0.0.v201112011016'),
	  @GrabConfig(systemClassLoader=true)
	])
@Grab(group='org.apache.lucene', module='lucene-core', version='5.2.1')
@Grab(group='org.apache.lucene', module='lucene-analyzers-common', version='5.2.1')
@Grab(group='org.apache.lucene', module='lucene-queryparser', version='5.2.1')

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.*
import groovy.servlet.*
 
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


def startJetty() {
  def server = new Server(30300)

  Path indexPath = FileSystems.getDefault().getPath("bio2rdf-index/")
  Directory dir = FSDirectory.open(indexPath)
  Analyzer analyzer = new StandardAnalyzer()
  
  DirectoryReader reader = DirectoryReader.open(dir)
  IndexSearcher searcher = new IndexSearcher(reader)
  def parser = new QueryParser("title", analyzer)
  
  def context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);

  context.setAttribute("searcher", searcher)
  context.setAttribute("parser", parser)
  context.resourceBase = '.'  
  context.addServlet(GroovyServlet, '/QueryBio2RDF.groovy')  
  context.setAttribute('version', '1.0')  
  server.start()
}
 
startJetty()


