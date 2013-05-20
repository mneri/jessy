package fr.inria.jessy.consistency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.fractal.Learner;
import net.sourceforge.fractal.membership.Group;
import net.sourceforge.fractal.utils.CollectionUtils;
import net.sourceforge.fractal.utils.ExecutorPool;

import org.apache.log4j.Logger;

import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.communication.MessagePropagation;
import fr.inria.jessy.protocol.ParallelSnapshotIsolationApplyPiggyback;
import fr.inria.jessy.protocol.ParallelSnapshotIsolationPiggyback;
import fr.inria.jessy.store.DataStore;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.TransactionTouchedKeys;

/**
 * PSI implementation according to [Serrano2011] paper with one exception. 
 * I.e., Uses group communication instead of two phase commit. 
 * 
 * CONS: PSI
 * Vector: VersionVector
 * Atomic Commitment: GroupCommunication
 * 
 * @author Masoud Saeida Ardekani
 * 
 */
public abstract class PSI extends Consistency implements Learner {

	private ExecutorPool pool = ExecutorPool.getInstance();

	private static Logger logger = Logger
			.getLogger(PSI.class);

	static {
		votePiggybackRequired = true;
		READ_KEYS_REQUIRED_FOR_COMMUTATIVITY_TEST=false;
	}

	private MessagePropagation propagation;
	
	private HashMap<String,ParallelSnapshotIsolationApplyPiggyback> applyPiggyback;

	private ConcurrentHashMap<UUID, ParallelSnapshotIsolationPiggyback> receivedPiggybacks;

	public PSI(JessyGroupManager m, DataStore store) {
		super(m, store);
		receivedPiggybacks = new ConcurrentHashMap<UUID, ParallelSnapshotIsolationPiggyback>();
		propagation = new MessagePropagation(this,m);
		
		applyPiggyback=new HashMap<String, ParallelSnapshotIsolationApplyPiggyback>();
		
		for(Group group:manager.getReplicaGroups()){
			ParallelSnapshotIsolationApplyPiggyback task=new ParallelSnapshotIsolationApplyPiggyback();
			pool.submit(task);
			applyPiggyback.put(group.name(),task);
		}
	}

	/**
	 * @inheritDoc
	 * 
	 *             According to the implementation of VersionVector, commute in
	 *             certification is not allowed.
	 *             <p>
	 *             Assume there are two servers s1 and s2 that replicate objects
	 *             x and y. Also assume there are two transaction t1 and t2 that
	 *             writes a new value on x and y accordingly. Since
	 *             commutativity may lead to execution of t1 and t2 in different
	 *             orders on s1 and s2, and version vector cannot distinguish
	 *             this re-ordering, it can lead to some strange behavior.
	 *             (i.e., reading inconsistent snapshots!)
	 *             
	 *             <p>
	 *             Note: if the group size is greater than 1, transactions cannot commute under any condition.
	 *             Because, sequenceNumber is assigned to a transaction in {@link Consistency#createCertificationVote(ExecutionHistory)}.
	 *             Now if two transactions T1 and T2 that does not have any conflict run in P1 and P2 (in group g1) concurrently,
	 *             then they can end up having different sequence numbers in different jessy instances.
	 *             For example, T1 and T2 can have sequenceNumbers 1 and 2 respectively in P1, and 
	 *             they can have sequenceNumbers 2 and 1 in P2 respectively. 
	 */
	@Override
	public boolean certificationCommute(ExecutionHistory history1,
			ExecutionHistory history2) {

			return !CollectionUtils.isIntersectingWith(history1.getWriteSet()
					.getKeys(), history2.getWriteSet().getKeys());

	}
	
	@Override
	public boolean certificationCommute(TransactionTouchedKeys tk1,
			TransactionTouchedKeys tk2) {
		return !CollectionUtils.isIntersectingWith(tk1.writeKeys, tk2.writeKeys);
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public Set<String> getConcerningKeys(ExecutionHistory executionHistory,
			ConcernedKeysTarget target) {
		Set<String> keys = new HashSet<String>();
		keys.addAll(executionHistory.getWriteSet().getKeys());
		keys.addAll(executionHistory.getCreateSet().getKeys());
		return keys;
	}

}
