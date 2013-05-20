package fr.inria.jessy.consistency;

import org.apache.log4j.Logger;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.protocol.NMSI_DV_GC;
import fr.inria.jessy.protocol.NMSI_GMUVector_GC;
import fr.inria.jessy.protocol.PSI_VV_GC;
import fr.inria.jessy.protocol.PStore;
import fr.inria.jessy.protocol.Serrano;
import fr.inria.jessy.protocol.US_DV_GC;
import fr.inria.jessy.protocol.US_GMUVector_GC;
import fr.inria.jessy.store.DataStore;
import fr.inria.jessy.utils.Configuration;

public class ConsistencyFactory {

	private static Logger logger = Logger.getLogger(ConsistencyFactory.class);

	private static Consistency _instance;

	private static String consistencyTypeName;

	static {
		consistencyTypeName = Configuration
				.readConfig(ConstantPool.CONSISTENCY_TYPE);
		logger.warn("Consistency is " + consistencyTypeName);
	}

	public static Consistency initConsistency(JessyGroupManager m, DataStore dataStore) {
		if (_instance != null)
			return _instance;

		if (consistencyTypeName.equals("nmsi")) {
			_instance = new NMSI_DV_GC(m, dataStore);
		} else if (consistencyTypeName.equals("nmsi2")) {
			_instance = new NMSI_GMUVector_GC(m,
					dataStore);
		} else if (consistencyTypeName.equals("si")) {
			//TODO
			System.err.println("si with ab-cast is not yet implemented. Use si2 instead");
//			_instance = new SnapshotIsolationWithBroadcast(dataStore);
		} else if (consistencyTypeName.equals("si2")) {
			_instance = new Serrano(m, dataStore);
		} else if (consistencyTypeName.equals("ser")) {
			_instance = new PStore(m, dataStore);
		} else if (consistencyTypeName.equals("rc")) {
			_instance = new RC(m, dataStore);
		} else if (consistencyTypeName.equals("psi")) {
			_instance = new PSI_VV_GC(m, dataStore);
		} else if (consistencyTypeName.equals("us")) {
			_instance = new US_DV_GC(m, dataStore);
		} else if (consistencyTypeName.equals("us2")) {
			_instance = new US_GMUVector_GC(m, dataStore);
		} else if (consistencyTypeName.equals("us3")) {
			_instance = new US_DV_GC(m, dataStore);
		} else if (consistencyTypeName.equals("nmsi3")) {
			_instance = new NMSI_DV_GC(m, dataStore);
		}
		return _instance;
	}

	public static Consistency getConsistencyInstance() {
		return _instance;
	}

	public static String getConsistencyTypeName() {
		return consistencyTypeName;
	}
	
	
}
