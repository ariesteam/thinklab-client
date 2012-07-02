package org.integratedmodelling.thinklab.client.owl;

import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.semanticweb.owlapi.model.OWLOntology;

public interface OWLOntologyNotifier {

	public abstract void notifyImportedOWLOntology(INamespace namespace, OWLOntology o);

	public abstract void notifyOWLOntology(INamespace namespace, OWLOntology o);

}
