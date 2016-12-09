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

class FSTOrdTermVectorsCodec extends FilterCodec("FSTOrdTermVectors62",new Lucene62Codec()) {
  val termMap = new java.util.HashMap[SegmentInfo,java.util.Map[FieldInfo,FST[java.lang.Long]]]
  override def postingsFormat = new PostingsFormat("FSTOrd50") {
    
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

  override def termVectorsFormat = new TermVectorsFormat {
     override def vectorsReader(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,fieldInfos: org.apache.lucene.index.FieldInfos,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsReader = {
       return new OrdTermVectorsReader(directory, segmentInfo, "",fieldInfos, context, "OrdTermVectors", CompressionMode.FAST_DECOMPRESSION, termMap.get(segmentInfo))
     }
     override def vectorsWriter(directory: org.apache.lucene.store.Directory,segmentInfo: org.apache.lucene.index.SegmentInfo,context: org.apache.lucene.store.IOContext): org.apache.lucene.codecs.TermVectorsWriter = {
       val ret = new OrdTermVectorsWriter(directory, segmentInfo, "", context, "OrdTermVectors", CompressionMode.FAST_DECOMPRESSION, 1 << 12, 1024, termMap.get(segmentInfo))
       return ret
     }

  }
}