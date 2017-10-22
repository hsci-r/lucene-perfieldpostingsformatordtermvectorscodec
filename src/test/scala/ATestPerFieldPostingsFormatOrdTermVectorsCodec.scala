import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.Test
import org.apache.lucene.store.MMapDirectory
import java.nio.file.FileSystems
import java.nio.file.Files
import org.junit.Before
import org.apache.lucene.store.Directory
import java.nio.file.Path
import org.junit.After
import java.util.Comparator
import java.io.File
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.index.IndexWriterConfig
import fi.seco.lucene.PerFieldPostingsFormatOrdTermVectorsCodec
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.document.Document
import org.apache.lucene.document.TextField
import org.apache.lucene.document.Field.Store
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.index.Term
import org.apache.lucene.document.Field
import org.apache.lucene.document.FieldType
import org.apache.lucene.index.UpgradeIndexMergePolicy
import org.apache.lucene.index.SegmentCommitInfo
import org.apache.lucene.codecs.PostingsFormat
import fi.seco.lucene.OrdExposingFSTOrdPostingsFormat
import org.apache.lucene.codecs.blocktreeords.BlockTreeOrdsPostingsFormat
import org.apache.lucene.codecs.compressing.OrdTermVectorsReader.TVTermsEnum

abstract class ATestPerFieldPostingsFormatOrdTermVectorsCodec(pf: PostingsFormat) {
  
  var dir: Directory = null
  
  @Before def setUp() {
    dir = new RAMDirectory()
  }
  
  @After def tearDown() {
    dir.close()
    dir = null
  }
  
  val fc = new PerFieldPostingsFormatOrdTermVectorsCodec()  
  val ft = new FieldType(TextField.TYPE_NOT_STORED)
  ft.setStoreTermVectors(true)
  
  @Test def testTermVectors() {
    var iwc = new IndexWriterConfig()
    val ic = new TermVectorFilteringLucene70Codec()
    iwc.setCodec(ic)
    var w = new IndexWriter(dir, iwc)
    var d = new Document()
    d.add(new Field("test","x testing 1 2 3", ft))
    d.add(new Field("test2","x", ft))
    w.addDocument(d)
    d = new Document()
    d.add(new Field("test","y testing 3 4 5", ft))
    d.add(new Field("test2","x", ft))
    w.addDocument(d)
    w.close()
    var s = new IndexSearcher(DirectoryReader.open(dir))
    var r = s.search(new TermQuery(new Term("test","testing")), 10)
    assertThat(r.totalHits, equalTo(2l))
    var tv = s.getIndexReader.getTermVector(r.scoreDocs(0).doc, "test")
    var tvi = tv.iterator()
    assertThat(tvi.next().utf8ToString(), equalTo("1"))
    assertThat(tvi.next().utf8ToString(), equalTo("2"))
    assertThat(tvi.next().utf8ToString(), equalTo("3"))
    assertThat(tvi.next().utf8ToString(), equalTo("testing"))
    assertThat(tvi.next().utf8ToString(), equalTo("x"))
    assertThat(tvi.next(), nullValue)
    tv = s.getIndexReader.getTermVector(r.scoreDocs(1).doc, "test")
    tvi = tv.iterator()
    assertThat(tvi.next().utf8ToString(), equalTo("3"))
    assertThat(tvi.next().utf8ToString(), equalTo("4"))
    assertThat(tvi.next().utf8ToString(), equalTo("5"))
    assertThat(tvi.next().utf8ToString(), equalTo("testing"))
    assertThat(tvi.next().utf8ToString(), equalTo("y"))
    assertThat(tvi.next(), nullValue)
    iwc = new IndexWriterConfig()
    fc.perFieldPostingsFormat = Map("test" -> pf, "test2" -> pf)
    iwc.setCodec(fc)
    iwc.setMergePolicy(new UpgradeIndexMergePolicy(iwc.getMergePolicy) {
      override protected def shouldUpgradeSegment(si: SegmentCommitInfo): Boolean = si.info.getCodec.getName != fc.getName
    })
    w = new IndexWriter(dir, iwc)
    w.forceMerge(1)
    w.close()
    s = new IndexSearcher(DirectoryReader.open(dir))
    r = s.search(new TermQuery(new Term("test","testing")), 10)
    assertThat(r.totalHits, equalTo(2l))
    tv = s.getIndexReader.getTermVector(r.scoreDocs(0).doc, "test")
    tvi = tv.iterator()
    assertThat(tvi.next().utf8ToString(), equalTo("1"))
    assertThat(tvi.next().utf8ToString(), equalTo("2"))
    assertThat(tvi.next().utf8ToString(), equalTo("3"))
    assertThat(tvi.next().utf8ToString(), equalTo("testing"))
    assertThat(tvi.next().utf8ToString(), equalTo("x"))
    assertThat(tvi.next(), nullValue)
    var tvi2 = tv.iterator().asInstanceOf[TVTermsEnum]
    assertThat(tvi2.nextOrd(), equalTo(0l))
    assertThat(tvi2.nextOrd(), equalTo(1l))
    assertThat(tvi2.nextOrd(), equalTo(2l))
    assertThat(tvi2.nextOrd(), equalTo(5l))
    assertThat(tvi2.nextOrd(), equalTo(6l))
    assertThat(tvi2.nextOrd(), equalTo(-1l))
    tv = s.getIndexReader.getTermVector(r.scoreDocs(1).doc, "test")
    tvi = tv.iterator()
    assertThat(tvi.next().utf8ToString(), equalTo("3"))
    assertThat(tvi.next().utf8ToString(), equalTo("4"))
    assertThat(tvi.next().utf8ToString(), equalTo("5"))
    assertThat(tvi.next().utf8ToString(), equalTo("testing"))
    assertThat(tvi.next().utf8ToString(), equalTo("y"))
    assertThat(tvi.next(), nullValue)
    tvi2 = tv.iterator().asInstanceOf[TVTermsEnum]
    assertThat(tvi2.nextOrd(), equalTo(2l))
    assertThat(tvi2.nextOrd(), equalTo(3l))
    assertThat(tvi2.nextOrd(), equalTo(4l))
    assertThat(tvi2.nextOrd(), equalTo(5l))
    assertThat(tvi2.nextOrd(), equalTo(7l))
    assertThat(tvi2.nextOrd(), equalTo(-1l))
  }
  
  @Test def testMerging() {
    var iwc = new IndexWriterConfig()
    val ic = new TermVectorFilteringLucene70Codec()
    iwc.setCodec(ic)
    var w = new IndexWriter(dir, iwc)
    var d = new Document()
    d.add(new Field("test","x testing 1 2 3", ft))
    d.add(new Field("test2","x", ft))
    w.addDocument(d)
    w.commit()
    d = new Document()
    d.add(new Field("test","y testing 3 4 5", ft))
    d.add(new Field("test2","x", ft))
    w.addDocument(d)
    w.commit()
    d = new Document()
    d.add(new Field("test","y testing 3 4 5 6", ft))
    d.add(new Field("test2","x y", ft))
    w.addDocument(d)
    w.close()
    var r = DirectoryReader.open(dir)
    assertEquals(3, r.getContext.leaves().size) 
    r.close()
    iwc = new IndexWriterConfig()
    fc.perFieldPostingsFormat = Map("test" -> pf, "test2" -> pf)
    iwc.setCodec(fc)
    iwc.setMergePolicy(new UpgradeIndexMergePolicy(iwc.getMergePolicy) {
      override protected def shouldUpgradeSegment(si: SegmentCommitInfo): Boolean = si.info.getCodec.getName != fc.getName
    })
    w = new IndexWriter(dir, iwc)
    w.forceMerge(1)
    w.close()
    r = DirectoryReader.open(dir)
    assertEquals(1, r.getContext.leaves().size) 
  }
  
}