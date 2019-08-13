import fi.seco.lucene.{Lucene80PerFieldPostingsFormatOrdTermVectorsCodec, PerFieldPostingsFormatOrdTermVectorsCodec}
import org.apache.lucene.codecs.PostingsFormat
import org.apache.lucene.codecs.compressing.OrdTermVectorsReader.TVTermsEnum
import org.apache.lucene.document.{Document, Field, FieldType, TextField}
import org.apache.lucene.index._
import org.apache.lucene.search.{IndexSearcher, TermQuery}
import org.apache.lucene.store.{Directory, RAMDirectory}
import org.hamcrest.CoreMatchers._
import org.junit.Assert._
import org.junit.{After, Before, Test}

abstract class ATestPerFieldPostingsFormatOrdTermVectorsCodec(pf: PostingsFormat, fc: PerFieldPostingsFormatOrdTermVectorsCodec = new Lucene80PerFieldPostingsFormatOrdTermVectorsCodec()) {
  
  var dir: Directory = _
  
  @Before def setUp() {
    dir = new RAMDirectory()
  }
  
  @After def tearDown() {
    dir.close()
    dir = null
  }
  
  val ft = new FieldType(TextField.TYPE_NOT_STORED)
  ft.setStoreTermVectors(true)
  
  @Test def testTermVectors() {
    var iwc = new IndexWriterConfig()
    val ic = new TermVectorFilteringLucene80Codec()
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
    assertThat(r.totalHits.value, equalTo(2l))
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
    assertThat(r.totalHits.value, equalTo(2l))
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
    val ic = new TermVectorFilteringLucene80Codec()
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
