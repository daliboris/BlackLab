package nl.inl.blacklab.server.search;

import nl.inl.blacklab.perdocument.DocGroupProperty;

public class DocGroupSortSettings {

	private DocGroupProperty sortBy;

	private boolean reverse;

	public DocGroupSortSettings(DocGroupProperty sortBy, boolean reverse) {
		super();
		this.sortBy = sortBy;
		this.reverse = reverse;
	}

	public DocGroupProperty sortBy() {
		return sortBy;
	}

	public boolean reverse() {
		return reverse;
	}

	@Override
	public String toString() {
		return "DocGroupSortSettings [sortBy=" + sortBy + ", reverse=" + reverse + "]";
	}

}
