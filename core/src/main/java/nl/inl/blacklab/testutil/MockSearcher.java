package nl.inl.blacklab.testutil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;

import nl.inl.blacklab.externalstorage.ContentStore;
import nl.inl.blacklab.forwardindex.ForwardIndex;
import nl.inl.blacklab.perdocument.DocResults;
import nl.inl.blacklab.search.QueryExecutionContext;
import nl.inl.blacklab.search.Searcher;

public class MockSearcher extends Searcher {

	public MockSearcher() {
		mainContentsFieldName = Searcher.DEFAULT_CONTENTS_FIELD_NAME;
		hitsSettings().setContextSize(5);
	}

	@Override
	public boolean isEmpty() {
		//
		return false;
	}

	@Override
	public void rollback() {
		//

	}

	@Override
	public Document document(int doc) {
		//
		return null;
	}

	@Override
	public boolean isDeleted(int doc) {
		//
		return false;
	}

	@Override
	public int maxDoc() {
		//
		return 0;
	}

	@Override
	@Deprecated
	public Scorer findDocScores(Query q) {
		//
		return null;
	}

	@Override
	@Deprecated
	public TopDocs findTopDocs(Query q, int n) {
		//
		return null;
	}

	@Override
	public void getCharacterOffsets(int doc, String fieldName, int[] startsOfWords, int[] endsOfWords, boolean fillInDefaultsIfNotFound) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IndexReader getIndexReader() {
		return null;
	}

	@Override
	public ForwardIndex openForwardIndex(String fieldPropName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ContentStore openContentStore(File indexXmlDir, boolean create) {
		return null;
	}

	@Override
	public QueryExecutionContext getDefaultExecutionContext(String fieldName) {
		return QueryExecutionContext.getSimple(this, fieldName);
	}

	@Override
	public String getIndexName() {
		return null;
	}

	@Override
	public IndexWriter openIndexWriter(File indexDir, boolean create, Analyzer useAnalyzer)
			throws IOException, CorruptIndexException, LockObtainFailedException {
		return null;
	}

	@Override
	public IndexWriter getWriter() {
		return null;
	}

	@Override
	public File getIndexDirectory() {
		return null;
	}

	@Override
	public void delete(Query q) {
		//
	}

	@Override
	public DocResults queryDocuments(Query documentFilterQuery) {
		return null;
	}

	@Override
	@Deprecated
	public Map<String, Integer> termFrequencies(Query documentFilterQuery, String fieldName, String propName, String altName) {
		return null;
	}

	@Override
	@Deprecated
	public void collectDocuments(Query query, Collector collector) {
		//
	}

	@Override
	public List<String> getFieldTerms(String fieldName, int maxResults) {
		return null;
	}

	@Override
	public IndexSearcher getIndexSearcher() {
		return null;
	}

	public void setForwardIndex(String fieldPropName, ForwardIndex forwardIndex) {
		addForwardIndex(fieldPropName, forwardIndex);
	}

	@Override
	protected ContentStore openContentStore(String fieldName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Integer> docIdSet() {
		return null;
	}

}
