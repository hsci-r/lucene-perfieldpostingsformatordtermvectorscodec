package fi.seco.lucene

import java.util.function.Predicate

import org.apache.lucene.codecs.compressing.{CompressionMode, OrdTermVectorsReader, OrdTermVectorsWriter}
import org.apache.lucene.codecs.lucene50.Lucene50PostingsFormat
import org.apache.lucene.codecs.lucene80.Lucene80Codec
import org.apache.lucene.codecs.perfield.PerFieldPostingsFormat
import org.apache.lucene.codecs.{FilterCodec, PostingsFormat, TermVectorsFormat}
import org.apache.lucene.util.BytesRef

class Lucene80PerFieldPostingsFormatOrdTermVectorsCodec extends FilterCodec("Lucene80PerFieldPostingsFormatOrdTermVectorsCodec",new Lucene80Codec()) with PerFieldPostingsFormatOrdTermVectorsCodec {

  var perFieldPostingsFormat: Map[String, PostingsFormat] = Map.empty
  var termVectorFilter: Predicate[BytesRef] = _
  
  val lucene50PostingsFormat = new Lucene50PostingsFormat()
  
  private val pfPostingsFormat = new PerFieldPostingsFormat() {
    override def getPostingsFormatForField(field: String): PostingsFormat = perFieldPostingsFormat.getOrElse(field,lucene50PostingsFormat)
  }
  
  override def postingsFormat() = pfPostingsFormat 
    
  private val ordTermVectorsFormat = new TermVectorsFormat {
     override def vectorsReader(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,fieldInfos: org.apache.lucene.index.FieldInfos,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsReader =
       new OrdTermVectorsReader(directory, segmentInfo, "",fieldInfos, context, "OrdTermVectors", CompressionMode.FAST_DECOMPRESSION, pfPostingsFormat)
     override def vectorsWriter(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsWriter =
       new OrdTermVectorsWriter(directory, segmentInfo, "", context, "OrdTermVectors", CompressionMode.FAST_DECOMPRESSION, 1 << 12, 10, pfPostingsFormat, termVectorFilter)
  }

  override def termVectorsFormat() = ordTermVectorsFormat
  
}
