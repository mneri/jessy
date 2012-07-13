package fr.inria.jessy.consistency;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.fractal.Learner;
import net.sourceforge.fractal.membership.Group;
import net.sourceforge.fractal.utils.CollectionUtils;

import org.apache.log4j.Logger;

import fr.inria.jessy.communication.GenuineTerminationCommunication;
import fr.inria.jessy.communication.TerminationCommunication;
import fr.inria.jessy.store.DataStore;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequest;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.ExecutionHistory.TransactionType;
import fr.inria.jessy.vector.Vector;
import fr.inria.jessy.vector.VectorFactory;

public class UpdateSerializability extends Consistency {

	private static Logger logger = Logger
			.getLogger(UpdateSerializability.class);

	public UpdateSerializability(DataStore dateStore) {
		super(dateStore);
	}

	@Override
	public boolean certify(ExecutionHistory executionHistory) {
		TransactionType transactionType = executionHistory.getTransactionType();

		logger.debug(executionHistory.getTransactionHandler() + " >> "
				+ transactionType.toString());
		logger.debug("ReadSet Vector"
				+ executionHistory.getReadSet().getCompactVector().toString());
		logger.debug("CreateSet Vectors"
				+ executionHistory.getCreateSet().getCompactVector().toString());
		logger.debug("WriteSet Vectors"
				+ executionHistory.getWriteSet().getCompactVector().toString());

		/*
		 * if the transaction is a read-only transaction, it commits right away.
		 */
		if (transactionType == TransactionType.READONLY_TRANSACTION) {
			logger.debug(executionHistory.getTransactionHandler() + " >> "
					+ transactionType.toString() + " >> COMMITTED");
			return true;
		}

		/*
		 * if the transaction is an initalization transaction, it first
		 * increaments the vectors and then commits.
		 */
		if (transactionType == TransactionType.INIT_TRANSACTION) {
			for (JessyEntity tmp : executionHistory.getCreateSet()
					.getEntities()) {
				/*
				 * set the selfkey of the created vector and put it back in the
				 * entity
				 */
				tmp.getLocalVector().increament();
			}

			logger.debug(executionHistory.getTransactionHandler() + " >> "
					+ transactionType.toString()
					+ " >> INIT_TRANSACTION COMMITTED");
			return true;
		}

		/*
		 * If the transaction is not init, we consider the create operations as
		 * update operations. Thus, we move them to the writeSet List.
		 */
		executionHistory.getWriteSet().addEntity(
				executionHistory.getCreateSet());

		JessyEntity lastComittedEntity;

		/*
		 * Firstly, the writeSet is checked.
		 */
		for (JessyEntity tmp : executionHistory.getWriteSet().getEntities()) {
			try {
				lastComittedEntity = store
						.get(new ReadRequest<JessyEntity>(
								(Class<JessyEntity>) tmp.getClass(),
								"secondaryKey", tmp.getKey(), null))
						.getEntity().iterator().next();

				if (lastComittedEntity.getLocalVector().isCompatible(
						tmp.getLocalVector()) != Vector.CompatibleResult.COMPATIBLE) {
					logger.warn("Certification fails for transaction "
							+ executionHistory.getTransactionHandler().getId()
							+ " because it has written " + tmp.getKey()
							+ " with version " + tmp.getLocalVector()
							+ " but the last committed version is : "
							+ lastComittedEntity.getLocalVector());
					return false;
				}

			} catch (NullPointerException e) {
				// nothing to do.
				// the key is simply not there.
			}

		}

		/*
		 * Secondly, the readSet is checked.
		 */
		for (JessyEntity tmp : executionHistory.getReadSet().getEntities()) {
			try {
				lastComittedEntity = store
						.get(new ReadRequest<JessyEntity>(
								(Class<JessyEntity>) tmp.getClass(),
								"secondaryKey", tmp.getKey(), null))
						.getEntity().iterator().next();

				if (lastComittedEntity.getLocalVector().isCompatible(
						tmp.getLocalVector()) != Vector.CompatibleResult.COMPATIBLE) {

					logger.warn("Certification fails for transaction "
							+ executionHistory.getTransactionHandler().getId()
							+ " because it has written " + tmp.getKey()
							+ " with version " + tmp.getLocalVector()
							+ " but the last committed version is : "
							+ lastComittedEntity.getLocalVector());

					return false;
				}

			} catch (NullPointerException e) {
				// nothing to do.
				// the key is simply not there.
			}

		}

		logger.debug(executionHistory.getTransactionHandler() + " >> "
				+ transactionType.toString() + " >> COMMITTED");
		return true;
	}

	@Override
	public boolean certificationCommute(ExecutionHistory history1,
			ExecutionHistory history2) {
		if (!checkCommutativity)
			return false;

		return !CollectionUtils.isIntersectingWith(history1.getWriteSet()
				.getKeys(), history2.getReadSet().getKeys())
				&& !CollectionUtils.isIntersectingWith(history2.getWriteSet()
						.getKeys(), history1.getReadSet().getKeys());

	}

	@Override
	public void prepareToCommit(ExecutionHistory executionHistory) {
		// updatedVector is a new vector. It will be used as a new
		// vector for all modified vectors.
		Vector<String> updatedVector = VectorFactory.getVector("");
		updatedVector.update(executionHistory.getReadSet().getCompactVector(),
				executionHistory.getWriteSet().getCompactVector());

		for (JessyEntity entity : executionHistory.getWriteSet().getEntities()) {
			updatedVector.setSelfKey(entity.getLocalVector().getSelfKey());
			entity.setLocalVector(updatedVector.clone());
		}
		
	}

	@Override
	public Set<String> getConcerningKeys(ExecutionHistory executionHistory) {
		Set<String> keys = new HashSet<String>();
		keys.addAll(executionHistory.getReadSet().getKeys());
		keys.addAll(executionHistory.getWriteSet().getKeys());
		keys.addAll(executionHistory.getCreateSet().getKeys());
		return keys;
	}

	@Override
	public TerminationCommunication getOrCreateTerminationCommunication(
			Group group, Learner learner) {
		if (terminationCommunication == null)
			/*
			 * Do not return {@code TrivialTerminationCommunication} instance
			 * because it may lead to <i>deadlock</i>.
			 */
			terminationCommunication = new GenuineTerminationCommunication(
					group, learner);
		return terminationCommunication;
	}

}