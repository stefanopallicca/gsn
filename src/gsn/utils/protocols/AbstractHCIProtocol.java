
package gsn.utils.protocols;



import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class provides a common framework to easily
 * implement a Host Controller Interface Protocol
 * in GSN.
 * Such protocols are used to communicate between a host
 * (pc, server...) and a controller (embedded system,
 * microcontroller, mote...).
 * The host can send queries and the controller sends
 * answers. Often, the controller also sends data spontaneously.
 * Support for this is in development.
 * There are two types of queries: some require an answer
 * from the controller, and some don't.
 * An implementation of AbstractHCIProtocol should be
 * used with the ProtocolManager class. The ProtocolManager
 * class deals with state issues: for example, after sending
 * a query the protocol might require you not to send any
 * query before the end of a timer or the reception of an
 * answer from the mote, whichever comes first. 
 * @author Jérôme Rousselot <jerome.rousselot@csem.ch>
 * @see ProtocolManager
 * @see AbstractHCIQuery
 */
public abstract class AbstractHCIProtocol {

	private String protocolName;
	private HashMap<String, AbstractHCIQuery> queries;
	   
	public AbstractHCIProtocol(String name) {
		protocolName = name;
		queries = new HashMap<String, AbstractHCIQuery>();
	}
	
	protected void addQuery(AbstractHCIQuery query) {
		queries.put(query.getName(), query);
	}
	
	/*
	 * Returns the complete list of all queries known 
	 * by this protocol.
	 */
	
	public Collection<AbstractHCIQuery> getQueries() {
		return queries.values();
	}
	/*
	 * Returns the name of the protocol represented
	 * by this class.
	 * 
	 */
	public String getName() {
		return protocolName;
	}
	
	public AbstractHCIQuery getQuery(String queryName) {
		return queries.get(queryName);
	}
   
	/*
	 * Returns null if the query does not exists, and the raw bytes
	 * to send to the wrapper if the query has been found.
	 */
	public byte[] buildRawQuery(String queryName, Vector<Object> params) {
		AbstractHCIQuery query = queries.get(queryName);
		if (query == null)
			return null;
		else {
			return query.buildRawQuery( params );
		}
	}

}
