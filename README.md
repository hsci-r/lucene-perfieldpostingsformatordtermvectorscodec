# lucene-fstordtermvectorscodec
Lucene codec for efficient storage and processing of term vectors based on storing term vectors as ords, and using the memory resident [FSTOrd term dictionary](https://lucene.apache.org/core/6_3_0/codecs/org/apache/lucene/codecs/memory/FSTOrdPostingsFormat.html) for efficient translations as well as frequency lookups.

**Note:** Doesn't allow normal indexwriting, but can only be created by migrating another ready index, e.g. in Scala:
```scala
val iwc = new IndexWriterConfig(analyzer)
val codec = new FSTOrdTermVectorsCoded()
iwc.setCodec(codec)
iwc.setMergePolicy(new UpgradeIndexMergePolicy(iwc.getMergePolicy()))
iwc.setMergePolicy(new UpgradeIndexMergePolicy(iwc.getMergePolicy()) {
  override protected def shouldUpgradeSegment(si: SegmentCommitInfo): Boolean =  !si.info.getCodec.equals(codec)
})
val miw = new IndexWriter(new MMapDirectory(FileSystems.getDefault.getPath(path)), iwc)
miw.forceMerge(1)
miw.commit()
miw.close()
miw.getDirectory.close()
```

The above restriction comes from the fact that in the normal default indexing chain, term vectors are written before the term dictionary. For my use case, I didn't want or need to muck around with the index chain to make it work. Luckily, it turned out that in segment merging, the process happens the other way round, so the term->ord dictionary is available when coding the term vectors.
