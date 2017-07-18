import org.apache.lucene.codecs.blocktreeords.BlockTreeOrdsPostingsFormat

class TestBlockTreePostingsFormat extends ATestPerFieldPostingsFormatOrdTermVectorsCodec(new BlockTreeOrdsPostingsFormat()) {
  
}