package fi.seco.lucene

import org.apache.lucene.codecs.PostingsFormat
import org.apache.lucene.codecs.lucene50.Lucene50PostingsWriter
import org.apache.lucene.codecs.memory.FSTOrdTermsWriter
import org.apache.lucene.codecs.lucene50.Lucene50PostingsReader
import org.apache.lucene.util.IOUtils
import org.apache.lucene.codecs.memory.FSTOrdOrdsExposingTermsReader

class OrdExposingFSTOrdPostingsFormat extends PostingsFormat("OEFSTOrd50") {
    override def fieldsConsumer(state: org.apache.lucene.index.SegmentWriteState): org.apache.lucene.codecs.FieldsConsumer = {
      val postingsWriter = new Lucene50PostingsWriter(state)
      var success = false;
      try {
        val ret = new FSTOrdTermsWriter(state, postingsWriter)
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
        val ret = new FSTOrdOrdsExposingTermsReader(state, postingsReader)
        success = true;
        return ret;
      } finally {
        if (!success)
          IOUtils.closeWhileHandlingException(postingsReader);
      }
    }
  }