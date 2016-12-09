package org.apache.lucene.codecs.memory

import org.apache.lucene.codecs.PostingsWriterBase
import org.apache.lucene.index.SegmentWriteState
import org.apache.lucene.index.FieldInfo
import org.apache.lucene.util.fst.FST
import org.apache.lucene.index.SegmentInfo
import scala.collection.JavaConverters._

class FSTOrdTermsExposingTermsWriter(state: SegmentWriteState, postingsWriter: PostingsWriterBase, processTerms: (SegmentInfo,java.util.Map[FieldInfo,FST[java.lang.Long]]) => Unit) extends FSTOrdTermsWriter(state,postingsWriter) {
  override def close() {
    super.close()
    val map = new java.util.HashMap[FieldInfo,FST[java.lang.Long]]
    fields.asScala.foreach(f => map.put(f.fieldInfo,f.dict))
    processTerms(state.segmentInfo, map)
  }
}