import fi.seco.lucene.Lucene84PerFieldPostingsFormatOrdTermVectorsCodec
import org.apache.lucene.codecs.blocktreeords.BlockTreeOrdsPostingsFormat
class Test84BlockTreePostingsFormat extends ATestPerFieldPostingsFormatOrdTermVectorsCodec(new BlockTreeOrdsPostingsFormat(), new Lucene84PerFieldPostingsFormatOrdTermVectorsCodec()) {
  
}