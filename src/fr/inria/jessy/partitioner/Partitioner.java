package fr.inria.jessy.partitioner;

import java.util.Set;

import net.sourceforge.fractal.membership.Group;
import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequest;

public abstract class Partitioner {

	JessyGroupManager manager;
	
	public Partitioner(JessyGroupManager m) {
		manager = m;
	}

	public abstract <E extends JessyEntity> Set<Group> resolve(
			ReadRequest<E> readRequest);

	public abstract Group resolve(String key);
	
	public abstract boolean isLocal(String k);

	public abstract Set<String> resolveNames(Set<String> keys);
	
	public abstract Set<String> generateKeysInAllGroups();
}
