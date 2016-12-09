package org.apache.lucene.codecs.memory

import org.apache.lucene.codecs.PostingsWriterBase
import org.apache.lucene.index.SegmentWriteState
import org.apache.lucene.index.FieldInfo
import org.apache.lucene.util.fst.FST
import org.apache.lucene.index.SegmentInfo
import scala.collection.JavaConverters._
import org.apache.lucene.index.SegmentReadState
import org.apache.lucene.codecs.PostingsReaderBase

class FSTOrdTermsAndOrdsExposingTermsReader(state: SegmentReadState, postingsReader: PostingsReaderBase, processTerms: (SegmentInfo,java.util.Map[FieldInfo,FST[java.lang.Long]]) => Unit) extends FSTOrdOrdsExposingTermsReader(state, postingsReader) {
  val map = new java.util.HashMap[FieldInfo,FST[java.lang.Long]]
  fields.values().asScala.foreach(f => map.put(f.fieldInfo,f.index))
  processTerms(state.segmentInfo, map)
}