package fi.hsci.lucene

import org.apache.lucene.codecs.{FilterCodec, TermVectorsFormat}
import org.apache.lucene.codecs.compressing.{CompressionMode, TermVectorFilteringCompressingTermVectorsWriter}
import org.apache.lucene.codecs.lucene87.Lucene87Codec
import org.apache.lucene.index.FieldInfo
import org.apache.lucene.util.BytesRef

import java.util.function.BiPredicate

class TermVectorFilteringLucene87Codec extends FilterCodec("Lucene87",new Lucene87Codec(Lucene87Codec.Mode.BEST_COMPRESSION)) {

  var termVectorFilter: BiPredicate[FieldInfo,BytesRef] = _

  override def termVectorsFormat = new TermVectorsFormat {
     override def vectorsReader(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,fieldInfos: org.apache.lucene.index.FieldInfos,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsReader =
       delegate.termVectorsFormat().vectorsReader(directory, segmentInfo, fieldInfos, context)

     override def vectorsWriter(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsWriter =
       new TermVectorFilteringCompressingTermVectorsWriter(directory, segmentInfo, "", context, "Lucene50TermVectorsData", CompressionMode.FAST, 1 << 12, 10, termVectorFilter)

  }
}
