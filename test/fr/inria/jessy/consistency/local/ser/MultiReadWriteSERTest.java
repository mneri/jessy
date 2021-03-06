/**
 * 
 */
package fr.inria.jessy.consistency.local.ser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.JessyFactory;
import fr.inria.jessy.consistency.local.nmsi.transaction.SampleEntityInitTransaction;
import fr.inria.jessy.consistency.local.nmsi.transaction.SampleTransactionMultiObj1;
import fr.inria.jessy.consistency.local.nmsi.transaction.SampleTransactionMultiObj2;
import fr.inria.jessy.consistency.local.nmsi.transaction.SampleTransactionMultiObj3;
import fr.inria.jessy.consistency.local.nmsi.transaction.SampleTransactionMultiObj4;
import fr.inria.jessy.entity.Sample2EntityClass;
import fr.inria.jessy.entity.SampleEntityClass;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;
import fr.inria.jessy.transaction.TransactionState;

/**
 * @author Masoud Saeida Ardekani This a test class for checking NMSI in a
 *         multi-object scenario.
 *         <p>
 *         The scenario is as follows: {@link SampleEntityInitTransaction} and
 *         {@link Sample2EntityInitTransaction} initialize two obejcts.
 *         {@link SampleTransactionMultiObj1} reads and writes on the first
 *         object. {@link SampleTransactionMultiObj3} reads and writes on the
 *         second object. {@link SampleTransactionMultiObj3} reads the initial
 *         values of two objects and writes after all transaction has been
 *         committed. Therefore, {@link SampleTransactionMultiObj3} should abort
 *         by the certification test, and the other should commit. There is also
 *         one read only transaction {@link SampleTransactionMultiObj4} that
 *         first reads the initial value for {@link SampleEntityClass} and when
 *         all update transaction have committed, reads
 *         {@link Sample2EntityClass}
 */
public class MultiReadWriteSERTest extends TestCase {

	Jessy jessy;

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
		jessy = JessyFactory.getLocalJessy();

		// First, we have to define the entities read or written inside the
		// transaction
		jessy.addEntity(SampleEntityClass.class);
		jessy.addEntity(Sample2EntityClass.class);
		
		
	}

	/**
	 * Test method for
	 * {@link fr.inria.jessy.transaction.Transaction#Transaction(fr.inria.jessy.Jessy, fr.inria.jessy.transaction.TransactionHandler)}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTransaction() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(4);

		Future<ExecutionHistory> futureInit1;
		futureInit1 = pool.submit(new SampleEntityInitTransaction(jessy));
		ExecutionHistory resultInit1 = futureInit1.get();
		assertEquals("Result", TransactionState.COMMITTED,
				resultInit1.getTransactionState());

		Future<ExecutionHistory> future1 = pool.submit(new update1(jessy));

		Future<ExecutionHistory> future2 = pool.submit(new update2(jessy));

		Future<ExecutionHistory> future3 = pool.submit(new read1(jessy));

		ExecutionHistory result1 = future1.get();
		assertEquals("Result", TransactionState.COMMITTED,
				result1.getTransactionState());

		ExecutionHistory result2 = future2.get();
		assertEquals("Result", TransactionState.COMMITTED,
				result2.getTransactionState());

		ExecutionHistory result3 = future3.get();
		assertEquals("Result", TransactionState.COMMITTED,
				result3.getTransactionState());

	}
	
	private class update1 extends Transaction{

		public update1(Jessy jessy) throws Exception {
			super(jessy);
		}

		@Override
		public ExecutionHistory execute() {
			SampleEntityClass se=new SampleEntityClass("1", "first_write");
			write(se);
			
			return commitTransaction();
		}
		
	}
	
	private class update2 extends Transaction{

		public update2(Jessy jessy) throws Exception {
			super(jessy);
		}

		@Override
		public ExecutionHistory execute() {
			try {
				
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			SampleEntityClass se=new SampleEntityClass("1", "second_write");
			write(se);
			
			return commitTransaction();
		}
		
	}

	private class read1 extends Transaction{

		public read1(Jessy jessy) throws Exception {
			super(jessy);
		}

		@Override
		public ExecutionHistory execute() {

			try {
				Thread.sleep(3000);
				SampleEntityClass se=read(SampleEntityClass.class, "1");
				System.out.println(se.getData());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return commitTransaction();
		}
		
	}

}
