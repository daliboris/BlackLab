package nl.inl.blacklab.server.search;

import nl.inl.blacklab.perdocument.DocProperty;

public class DocGroupSettings {

	DocProperty groupBy;

	public DocGroupSettings(DocProperty groupBy) {
		super();
		this.groupBy = groupBy;
	}

	public DocProperty groupBy() {
		return groupBy;
	}

	@Override
	public String toString() {
		return "DocGroupSettings [groupBy=" + groupBy + "]";
	}

}
