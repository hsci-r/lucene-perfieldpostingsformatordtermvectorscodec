package fi.seco.lucene

import org.apache.lucene.codecs.FilterCodec
import org.apache.lucene.codecs.lucene62.Lucene62Codec
import org.apache.lucene.codecs.memory.FSTOrdPostingsFormat
import org.apache.lucene.codecs.PostingsFormat
import org.apache.lucene.codecs.memory.FSTOrdTermsWriter
import org.apache.lucene.codecs.lucene50.Lucene50PostingsWriter
import org.apache.lucene.util.IOUtils
import org.apache.lucene.codecs.lucene50.Lucene50PostingsReader
import org.apache.lucene.codecs.memory.FSTOrdTermsAndOrdsExposingTermsReader
import org.apache.lucene.index.Fields
import org.apache.lucene.codecs.compressing.CompressingTermVectorsFormat
import org.apache.lucene.codecs.compressing.CompressionMode
import org.apache.lucene.codecs.TermVectorsFormat
import org.apache.lucene.codecs.compressing.CompressingTermVectorsReader
import org.apache.lucene.codecs.compressing.CompressingTermVectorsWriter
import scala.collection.mutable.HashMap
import org.apache.lucene.index.SegmentInfo
import org.apache.lucene.util.fst.BytesRefFSTEnum
import org.apache.lucene.util.BytesRef
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.util.fst.FST
import scala.collection.JavaConverters._
import org.apache.lucene.index.FieldInfo
import org.apache.lucene.codecs.compressing.OrdTermVectorsWriter
import org.apache.lucene.codecs.memory.FSTOrdTermsExposingTermsWriter
import org.apache.lucene.codecs.compressing.OrdTermVectorsReader
import org.apache.lucene.codecs.memory.FSTOrdOrdsExposingTermsReader
import org.apache.lucene.codecs.lucene50.Lucene50TermVectorsFormat
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat.Mode
import java.util.function.Predicate
import org.apache.lucene.codecs.perfield.PerFieldPostingsFormat
import org.apache.lucene.codecs.lucene50.Lucene50PostingsFormat

class PerFieldFSTOrdTermVectorsCodec extends FilterCodec("PerFieldFSTOrdTermVectors62",new Lucene62Codec(Mode.BEST_COMPRESSION)) {
  
  private val termMap = new java.util.HashMap[SegmentInfo,java.util.Map[FieldInfo,FST[java.lang.Long]]]
  
  var termVectorFields: Set[String] = Set.empty
  var termVectorFilter: Predicate[BytesRef] = null
  
  private val otherPostingsFormat = new Lucene50PostingsFormat() 
  private val termVectorPostingsFormat = new PostingsFormat("FSTOrd50") {
    override def fieldsConsumer(state: org.apache.lucene.index.SegmentWriteState): org.apache.lucene.codecs.FieldsConsumer = {
      val postingsWriter = new Lucene50PostingsWriter(state)
      var success = false;
      try {
        val ret = new FSTOrdTermsExposingTermsWriter(state, postingsWriter, (segmentInfo: SegmentInfo, fieldMap: java.util.Map[FieldInfo,FST[java.lang.Long]]) => termMap.put(segmentInfo, fieldMap))
        success = true;
        return ret;
      } finally {
        if (!success)
          IOUtils.closeWhileHandlingException(postingsWriter);
      }
    }
    override def fieldsProducer(state: org.apache.lucene.index.SegmentReadState): org.apache.lucene.codecs.FieldsProducer = {
      val postingsReader = new Lucene50PostingsReader(state);
      var success = false;
      try {
        val ret = new FSTOrdTermsAndOrdsExposingTermsReader(state, postingsReader, (segmentInfo: SegmentInfo, fieldMap: java.util.Map[FieldInfo,FST[java.lang.Long]]) => termMap.put(segmentInfo, fieldMap))
        success = true;
        return ret;
      } finally {
        if (!success)
          IOUtils.closeWhileHandlingException(postingsReader);
      }
    }
  }
  
  private val pfPostingsFormat = new PerFieldPostingsFormat() {
    override def getPostingsFormatForField(field: String): PostingsFormat = if (!termVectorFields.contains(field)) otherPostingsFormat else termVectorPostingsFormat
  }
  
  override def postingsFormat() = pfPostingsFormat 
    
  private val ordTermVectorsFormat = new TermVectorsFormat {
     override def vectorsReader(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,fieldInfos: org.apache.lucene.index.FieldInfos,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsReader = {
       return new OrdTermVectorsReader(directory, segmentInfo, "",fieldInfos, context, "OrdTermVectors", CompressionMode.FAST_DECOMPRESSION, termMap.get(segmentInfo))
     }
     override def vectorsWriter(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsWriter = {
       val ret = new OrdTermVectorsWriter(directory, segmentInfo, "", context, "OrdTermVectors", CompressionMode.FAST_DECOMPRESSION, 1 << 12, 1024, termMap.get(segmentInfo), termVectorFilter)
       return ret
     }
  }

  override def termVectorsFormat() = ordTermVectorsFormat
  
}