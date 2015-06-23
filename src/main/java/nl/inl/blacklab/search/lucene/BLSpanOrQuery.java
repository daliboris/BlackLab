package nl.inl.blacklab.search.lucene;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.inl.blacklab.search.Span;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.PriorityQueue;
import org.apache.lucene.util.ToStringUtils;

/** Matches the union of its clauses.
 *
 * BL-specific version (search for "// BL") that produces a BLSpans.
 */
@SuppressWarnings({"javadoc"}) // BL: suppress some innocent warnings
public class BLSpanOrQuery extends SpanQuery {
	List<SpanQuery> clauses; // BL: changed from private because accessed by inner class
	private String field;

	/** Construct a BLSpanOrQuery merging the provided clauses. */
	public BLSpanOrQuery(SpanQuery... clauses) {

		// copy clauses array into an ArrayList
		this.clauses = new ArrayList<SpanQuery>(clauses.length);
		for (int i = 0; i < clauses.length; i++) {
			addClause(clauses[i]);
		}
	}

	/** Adds a clause to this query */
	public final void addClause(SpanQuery clause) {
		if (field == null) {
			field = clause.getField();
		} else if (!clause.getField().equals(field)) {
			throw new IllegalArgumentException("Clauses must have same field.");
		}
		this.clauses.add(clause);
	}

	/** Return the clauses whose spans are matched. */
	public SpanQuery[] getClauses() {
		return clauses.toArray(new SpanQuery[clauses.size()]);
	}

	@Override
	public String getField() {
		return field;
	}

	@Override
	public void extractTerms(Set<Term> terms) {
		for (final SpanQuery clause : clauses) {
			clause.extractTerms(terms);
		}
	}

	@Override
	public BLSpanOrQuery clone() {
		int sz = clauses.size();
		SpanQuery[] newClauses = new SpanQuery[sz];

		for (int i = 0; i < sz; i++) {
			newClauses[i] = (SpanQuery) clauses.get(i).clone();
		}
		BLSpanOrQuery soq = new BLSpanOrQuery(newClauses);
		soq.setBoost(getBoost());
		return soq;
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		BLSpanOrQuery clone = null;
		for (int i = 0; i < clauses.size(); i++) {
			SpanQuery c = clauses.get(i);
			SpanQuery query = (SpanQuery) c.rewrite(reader);
			if (query != c) { // clause rewrote: must clone
				if (clone == null)
					clone = this.clone();
				clone.clauses.set(i, query);
			}
		}
		if (clone != null) {
			return clone; // some clauses rewrote
		}
		return this; // no clauses rewrote
	}

	@Override
	public String toString(String field) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("spanOr([");
		Iterator<SpanQuery> i = clauses.iterator();
		while (i.hasNext()) {
			SpanQuery clause = i.next();
			buffer.append(clause.toString(field));
			if (i.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append("])");
		buffer.append(ToStringUtils.boost(getBoost()));
		return buffer.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final BLSpanOrQuery that = (BLSpanOrQuery) o;

		if (!clauses.equals(that.clauses))
			return false;
		if (!clauses.isEmpty() && !field.equals(that.field))
			return false;

		return getBoost() == that.getBoost();
	}

	@Override
	public int hashCode() {
		int h = clauses.hashCode();
		h ^= (h << 10) | (h >>> 23);
		h ^= Float.floatToRawIntBits(getBoost());
		return h;
	}

	private class SpanQueue extends PriorityQueue<BLSpans> {
		public SpanQueue(int size) {
			super(size);
		}

		@Override
		protected final boolean lessThan(BLSpans spans1, BLSpans spans2) {
			if (spans1.doc() == spans2.doc()) {
				if (spans1.start() == spans2.start()) {
					return spans1.end() < spans2.end();
				}
				return spans1.start() < spans2.start();
			}
			return spans1.doc() < spans2.doc();
		}
	}

	@Override
	public Spans getSpans(final AtomicReaderContext context,
			final Bits acceptDocs, final Map<Term, TermContext> termContexts)
			throws IOException {
		if (clauses.size() == 1) // optimize 1-clause case
			return BLSpansWrapper.optWrap((clauses.get(0)).getSpans(context, acceptDocs, termContexts)); // BL: wrapped in BLSpans

		// Create a clauseList and compute clauseLength and clausesAllSameLength for the
		// anonymous BLSpan class
		boolean clausesAllSameLengthSetter = true;
		int clauseLengthSetter = -1;
		final ArrayList<BLSpans> clauseList = new ArrayList<BLSpans>(clauses.size());
		for(SpanQuery spanQuery : clauses) {
			BLSpans spans = BLSpansWrapper.optWrap(spanQuery.getSpans(context, acceptDocs,
					termContexts));
			if (spans.hitsAllSameLength() &&
					(clauseLengthSetter == -1 || clauseLengthSetter == spans.hitsLength())) {
				// This clause doesn't violate the all-same-length requirements
				clauseLengthSetter = spans.hitsLength();
			} else {
				// This clause does violate the all-same-length requirements
				clausesAllSameLengthSetter = false;
			}
			clauseList.add(spans);
		}
		final boolean clausesAllSameLength = clausesAllSameLengthSetter;
		final int clauseLength;
		if (clausesAllSameLength) {
			clauseLength = clauseLengthSetter;
		} else {
			clauseLength = -1;
		}

		return new BLSpans() { // BL: was Spans

			private SpanQueue queue = null;

			private boolean initSpanQueue(int target) throws IOException {
				queue = new SpanQueue(clauseList.size());
				for (BLSpans spans: clauseList) {

					if (((target == -1) && spans.next())
							|| ((target != -1) && spans.skipTo(target))) {
						queue.add(spans);
					}
				}

				return queue.size() != 0;
			}

			@Override
			public boolean next() throws IOException {
				if (queue == null) {
					return initSpanQueue(-1);
				}

				if (queue.size() == 0) { // all done
					return false;
				}

				if (top().next()) { // move to next
					queue.updateTop();
					return true;
				}

				queue.pop(); // exhausted a clause
				return queue.size() != 0;
			}

			private BLSpans top() {
				return queue.top();
			}

			@Override
			public boolean skipTo(int target) throws IOException {
				if (queue == null) {
					return initSpanQueue(target);
				}

				boolean skipCalled = false;
				while (queue.size() != 0 && top().doc() < target) {
					if (top().skipTo(target)) {
						queue.updateTop();
					} else {
						queue.pop();
					}
					skipCalled = true;
				}

				if (skipCalled) {
					return queue.size() != 0;
				}
				return next();
			}

			@Override
			public int doc() {
				return top().doc();
			}

			@Override
			public int start() {
				return top().start();
			}

			@Override
			public int end() {
				return top().end();
			}

			@Override
			public Collection<byte[]> getPayload() throws IOException {
				ArrayList<byte[]> result = null;
				Spans theTop = top();
				if (theTop != null && theTop.isPayloadAvailable()) {
					result = new ArrayList<byte[]>(theTop.getPayload());
				}
				return result;
			}

			@Override
			public boolean isPayloadAvailable() throws IOException {
				Spans top = top();
				return top != null && top.isPayloadAvailable();
			}

			@Override
			public String toString() {
				return "spans("
						+ BLSpanOrQuery.this
						+ ")@"
						+ ((queue == null) ? "START"
								: (queue.size() > 0 ? (doc() + ":" + start()
										+ "-" + end()) : "END"));
			}

			// BL: added guarantee-methods

			@Override
			public boolean hitsEndPointSorted() {
				return false; // cannot guarantee because we're merging from different sources
			}

			@Override
			public boolean hitsStartPointSorted() {
				return true;  // our way of merging guarantees this, as it should for almost all BLSpans
			}

			@Override
			public boolean hitsAllSameLength() {
				return clausesAllSameLength;
			}

			@Override
			public int hitsLength() {
				return clauseLength;
			}

			@Override
			public boolean hitsHaveUniqueStart() {
				return false; // cannot guarantee because we're merging from different sources
			}

			@Override
			public boolean hitsHaveUniqueEnd() {
				return false; // cannot guarantee because we're merging from different sources
			}

			@Override
			public boolean hitsAreUnique() {
				return false; // cannot guarantee because we're merging from different sources
			}

			@Override
			public void passHitQueryContextToClauses(HitQueryContext context) {
				for (BLSpans spans: clauseList) {
					spans.setHitQueryContext(context);
				}
			}

			@Override
			public void getCapturedGroups(Span[] capturedGroups) {
				if (!childClausesCaptureGroups)
					return;
				top().getCapturedGroups(capturedGroups);
			}

		};
	}

	/**
	 * Explicitly set the field for this query. Required because some queries
	 * rewrite to 0-clause or queries, and we need to be able to call getField()
	 * later.
	 *
	 * @param field the field for this query
	 */
	public void setField(String field) {
		this.field = field;
	}

}
