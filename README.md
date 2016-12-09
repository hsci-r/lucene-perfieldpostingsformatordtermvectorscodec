# lucene-fstordtermvectorscodec
Lucene codec for efficient storage and processing of term vectors based on storing term vectors as ords, and using the memory resident [FSTOrd term dictionary](https://lucene.apache.org/core/6_3_0/codecs/org/apache/lucene/codecs/memory/FSTOrdPostingsFormat.html) for efficient translations as well as frequency lookups.

**Note:** Doesn't allow normal indexwriting, but can only be created by migrating another ready index, e.g. in Scala:
```scala
val iwc = new IndexWriterConfig(analyzer)
iwc.setCodec(new FSTOrdTermVectorsCodec())
iwc.setMergePolicy(new UpgradeIndexMergePolicy(iwc.getMergePolicy()))
val miw = new IndexWriter(new MMapDirectory(FileSystems.getDefault.getPath(path)), iwc)
miw.forceMerge(1)
miw.commit()
miw.close()
miw.getDirectory.close()
```
