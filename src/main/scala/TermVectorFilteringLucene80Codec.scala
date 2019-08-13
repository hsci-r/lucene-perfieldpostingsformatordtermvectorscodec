import org.apache.lucene.codecs.lucene80.Lucene80Codec
import org.apache.lucene.codecs.FilterCodec
import org.apache.lucene.codecs.TermVectorsFormat
import org.apache.lucene.codecs.compressing.TermVectorFilteringCompressingTermVectorsWriter
import org.apache.lucene.codecs.compressing.CompressionMode
import java.util.function.BiPredicate
import org.apache.lucene.index.FieldInfo
import org.apache.lucene.util.BytesRef

class TermVectorFilteringLucene80Codec extends FilterCodec("Lucene80",new Lucene80Codec()) {

  var termVectorFilter: BiPredicate[FieldInfo,BytesRef] = _

  override def termVectorsFormat = new TermVectorsFormat {
     override def vectorsReader(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,fieldInfos: org.apache.lucene.index.FieldInfos,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsReader =
       delegate.termVectorsFormat().vectorsReader(directory, segmentInfo, fieldInfos, context)

     override def vectorsWriter(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsWriter =
       new TermVectorFilteringCompressingTermVectorsWriter(directory, segmentInfo, "", context, "Lucene50TermVectors", CompressionMode.FAST, 1 << 12, 1024, termVectorFilter)

  }
}
