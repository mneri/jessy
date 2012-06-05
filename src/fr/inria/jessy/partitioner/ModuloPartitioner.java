package fr.inria.jessy.partitioner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.fractal.membership.Group;
import net.sourceforge.fractal.membership.Membership;

import org.apache.log4j.Logger;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequest;

/**
 * This class implements a simple uniform partitioner as follows:
 * Desitionation=Key % No_Of_Partitions
 * <p>
 * Attention: This class implementation may not be safe regarding some keys.
 * Attention: This class does not support dynamic groups.
 * 
 * @author Masoud Saeida Ardekani
 * 
 */
// TODO InComplete!!!
public class ModuloPartitioner extends Partitioner {

	private static Logger logger = Logger.getLogger(ModuloPartitioner.class);

	// TODO should be defined by the user
	private String nonNumerical = "user";

	private List<Group> allGroups;

	public ModuloPartitioner(Membership m) {
		super(m);

		allGroups = new ArrayList<Group>();

		for (Group g : membership.allGroups(ConstantPool.JESSY_SERVER_GROUP)) {
			allGroups.add(g);
		}

		Collections.sort(allGroups);
	}

	@Override
	public <E extends JessyEntity> Set<Group> resolve(ReadRequest<E> readRequest) {
		Set<Group> ret = new HashSet<Group>();

		if (readRequest.isOneKeyRequest()) {
			ret.add(resolve(readRequest.getOneKey().getKeyValue().toString()));
		} else {
			return null;
		}

		return ret;
	}

	@Override
	public boolean isLocal(String k) {
		return membership.myGroups().contains(resolve(k));
	}

	@Override
	public Set<String> resolveNames(Set<String> keys) {
		Set<String> result = new HashSet<String>();
	
		/*
		 * return if there is no key!
		 */
		if (keys.size() == 0)
			return result;

		for (String key : keys) {
			result.add(resolve(key).name());
		}
		logger.debug("keys " + keys + " are resolved to" + result);
		return result;
	}

	/**
	 * This methods returns the group of a key.
	 * 
	 * @param k
	 *            a key
	 * @return the replica group of <i>k</i>.
	 */
	private Group resolve(String key) {
		int numericKey = Integer.valueOf(key.replaceAll(nonNumerical, ""));
		return allGroups.get(numericKey % allGroups.size());
	}

}
