package fi.seco.lucene

import org.apache.lucene.codecs.FilterCodec
import org.apache.lucene.codecs.lucene70.Lucene70Codec
import org.apache.lucene.codecs.PostingsFormat
import org.apache.lucene.codecs.compressing.CompressionMode
import org.apache.lucene.codecs.TermVectorsFormat
import org.apache.lucene.util.BytesRef
import org.apache.lucene.codecs.compressing.OrdTermVectorsWriter
import org.apache.lucene.codecs.compressing.OrdTermVectorsReader
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat.Mode
import java.util.function.Predicate
import org.apache.lucene.codecs.perfield.PerFieldPostingsFormat
import org.apache.lucene.codecs.lucene50.Lucene50PostingsFormat

class PerFieldPostingsFormatOrdTermVectorsCodec extends FilterCodec("PerFieldPostingsFormatOrdTermVectorsCodec",new Lucene70Codec(Mode.BEST_COMPRESSION)) {

  var perFieldPostingsFormat: Map[String, PostingsFormat] = Map.empty
  var termVectorFilter: Predicate[BytesRef] = null
  
  val lucene50PostingsFormat = new Lucene50PostingsFormat()
  
  private val pfPostingsFormat = new PerFieldPostingsFormat() {
    override def getPostingsFormatForField(field: String): PostingsFormat = perFieldPostingsFormat.get(field).getOrElse(lucene50PostingsFormat)
  }
  
  override def postingsFormat() = pfPostingsFormat 
    
  private val ordTermVectorsFormat = new TermVectorsFormat {
     override def vectorsReader(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,fieldInfos: org.apache.lucene.index.FieldInfos,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsReader = {
       return new OrdTermVectorsReader(directory, segmentInfo, "",fieldInfos, context, "OrdTermVectors", CompressionMode.FAST_DECOMPRESSION, pfPostingsFormat)
     }
     override def vectorsWriter(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsWriter = {
       val ret = new OrdTermVectorsWriter(directory, segmentInfo, "", context, "OrdTermVectors", CompressionMode.FAST_DECOMPRESSION, 1 << 12, 1024, pfPostingsFormat, termVectorFilter)
       return ret
     }
  }

  override def termVectorsFormat() = ordTermVectorsFormat
  
}