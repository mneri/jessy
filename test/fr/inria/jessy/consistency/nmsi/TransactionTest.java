/**
 * 
 */
package fr.inria.jessy.consistency.nmsi;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.jessy.LocalJessy;
import fr.inria.jessy.store.Sample2EntityClass;
import fr.inria.jessy.store.SampleEntityClass;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.ExecutionHistory.TransactionState;

/**
 * @author msaeida
 * 
 */
public class TransactionTest extends TestCase {

	LocalJessy jessy;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
			
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");	
		jessy = LocalJessy.getInstance();

		// First, we have to define the entities read or written inside the
		// transaction
		jessy.addEntity(SampleEntityClass.class);
		jessy.addEntity(Sample2EntityClass.class);

	}

	/**
	 * Test method for
	 * {@link fr.inria.jessy.transaction.Transaction#Transaction(fr.inria.jessy.Jessy, fr.inria.jessy.transaction.TransactionHandler)}
	 * .
	 * @throws Exception 
	 */
	@Test
	public void testTransaction() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(3);

		Future<ExecutionHistory> future;
		future = pool.submit(new SampleInitTransaction(jessy));
		
		Future<ExecutionHistory> future1;
		future1 = pool.submit(new SampleTransaction1(jessy));
		
		Future<ExecutionHistory> future2;
		future2 = pool.submit(new SampleTransaction2(jessy));

		ExecutionHistory result=future.get();
		assertEquals("Result", TransactionState.COMMITTED, result.getTransactionState());
		
		ExecutionHistory result1=future1.get();
		assertEquals("Result", TransactionState.COMMITTED, result1.getTransactionState());
		
		ExecutionHistory result2=future2.get();
		assertEquals("Result", TransactionState.ABORTED_BY_CERTIFICATION, result2.getTransactionState());

	}
	

}