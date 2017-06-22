import org.apache.lucene.codecs.lucene62.Lucene62Codec
import org.apache.lucene.codecs.FilterCodec
import org.apache.lucene.codecs.TermVectorsFormat
import org.apache.lucene.codecs.compressing.TermVectorFilteringCompressingTermVectorsWriter
import org.apache.lucene.codecs.compressing.CompressionMode
import java.util.function.BiPredicate
import org.apache.lucene.index.FieldInfo
import org.apache.lucene.util.BytesRef

class TermVectorFilteringLucene62Codec extends FilterCodec("Lucene62",new Lucene62Codec()) {

  var termVectorFilter: BiPredicate[FieldInfo,BytesRef] = null

  override def termVectorsFormat = new TermVectorsFormat {
     override def vectorsReader(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,fieldInfos: org.apache.lucene.index.FieldInfos,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsReader = {
       return delegate.termVectorsFormat().vectorsReader(directory, segmentInfo, fieldInfos, context)
     }

     override def vectorsWriter(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsWriter = {
       return new TermVectorFilteringCompressingTermVectorsWriter(directory, segmentInfo, "", context, "Lucene50TermVectors", CompressionMode.FAST, 1 << 12, 1024, termVectorFilter)
     }

  }
}
