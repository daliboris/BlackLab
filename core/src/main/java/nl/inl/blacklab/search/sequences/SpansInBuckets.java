/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.inl.blacklab.search.sequences;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.search.spans.Spans;

import nl.inl.blacklab.search.Hit;
import nl.inl.blacklab.search.Span;
import nl.inl.blacklab.search.lucene.HitQueryContext;

/**
 * Interface to retrieve whole sequences of certain matches (in "buckets") instead of individual
 * matches like with Spans.
 *
 * This is useful for efficiently processing sequences of related matches (i.e. fetch some content
 * for all matches in one document).
 *
 * N.B. Note that in these classes, we avoid the term 'group' and 'grouping' because we already use
 * these terms for the generic way of grouping spans (nl.inl.blacklab.search.grouping), while this
 * is more focused on speed and efficiency of certain specific operations.
 *
 * Specifically, SpansInBuckets is designed to have random access to the contents of a bucket, but
 * for efficiency's sake, only has sequential access to the buckets themselves. Also, SpansInBuckets
 * uses subclassing instead of GroupIdentity objects to determine what goes in a bucket. This makes
 * it easier to optimize.
 *
 * Note that SpansInBuckets assumes all hits in a bucket are from a single document.
 */
public interface SpansInBuckets {
	/** When to reallocate arraylists to avoid holding on to too much memory */
	static final int ARRAYLIST_REALLOC_THRESHOLD = 1000;

	final static int NO_MORE_BUCKETS = Spans.NO_MORE_POSITIONS;

	public static interface BucketSpanComparator {
		abstract public int compare(int start1, int end1, int start2, int end2);
	}

	/**
	 * Document id of current bucket
	 *
	 * @return Document id of current bucket
	 */
	int docID();

	int bucketSize();

	int startPosition(int index);

	int endPosition(int index);

	Hit getHit(int index);

	Collection<byte[]> getPayload(int indexInBucket);

	boolean isPayloadAvailable(int indexInBucket);

	/**
	 * Go to the next document.
	 *
	 * You still have to call nextBucket() to get to the first valid bucket.
	 *
	 * @return docID if we're at the next valid doc, NO_MORE_DOCS if we're done
	 * @throws IOException
	 */
	int nextDoc() throws IOException;

	/**
	 * Go to the next bucket in this doc.
	 *
	 * @return docID if we're at the next valid bucket, NO_MORE_BUCKETS if we're done
	 * @throws IOException
	 */
	int nextBucket() throws IOException;

	/**
	 * Skip to specified document id.
	 *
	 * If we're already at the target id, go to the next document (just like Spans).
	 *
	 * @param target
	 *            document id to skip to
	 * @return docID if we're at a valid document, NO_MORE_DOCS if we're done
	 * @throws IOException
	 */
	int advance(int target) throws IOException;

	/**
	 * Pass the hit query context to the underlying BLSpans.
	 *
	 * @param context the hit query context
	 */
	void setHitQueryContext(HitQueryContext context);

	/**
	 * Get the captured groups information for the current hit.
	 *
	 * @param indexInBucket what hit in the current bucket to get the information for
	 * @param capturedGroups where to add the captured group information
	 */
	void getCapturedGroups(int indexInBucket, Span[] capturedGroups);

}
