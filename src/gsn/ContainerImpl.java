package gsn;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.notifications.GSNNotification;
import gsn.notifications.NotificationRequest;
import gsn.storage.StorageManager;
import gsn.utils.CaseInsensitiveComparator;
import gsn.vsensor.AbstractVirtualSensor;
import gsn.vsensor.http.AddressingReqHandler;
import gsn.vsensor.http.ContainerInfoHandler;
import gsn.vsensor.http.OneShotQueryHandler;
import gsn.vsensor.http.OneShotQueryWithAddressingHandler;
import gsn.vsensor.http.OutputStructureHandler;
import gsn.vsensor.http.RequestHandler;
import gsn.wrappers.RemoteDS;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * @author Ali Salehi (AliS, ali.salehi-at-epfl.ch)<br>
 * @web.servlet name="gsn" load-on-startup="1"
 * @web.servlet-mapping url-pattern="/gsn"
 */
public class ContainerImpl extends HttpServlet implements Container {
   
   private static transient Logger                                      logger                             = Logger.getLogger( ContainerImpl.class );
   
   /**
    * The <code> waitingVirtualSensors</code> contains the virtual sensors that
    * recently produced data. This variable is useful for batch processing TIMED
    * couple virtual sensor produce data.
    */
   /*
    * In the <code>registeredQueries</code> the key is the local virtual
    * sensor name.
    */
   private static TreeMap < String , ArrayList < NotificationRequest >> notificationRequests               = null;
   
   private static final Class < ContainerImpl >                         notificationRequestsLock           = ContainerImpl.class;
   
   private static final HashMap < Integer , RemoteDS >                   notificationCodeToRemoteDataSource = new HashMap < Integer , RemoteDS >( );
   
   private static final Object                                          psLock                             = new Object( );
   
   public ContainerImpl ( ) {
      notificationRequests = new TreeMap < String , ArrayList < NotificationRequest >>( new CaseInsensitiveComparator( ) );
   }
   
   public void publishData ( AbstractVirtualSensor sensor ) {
      StreamElement data = sensor.getData( );
      String name = sensor.getVirtualSensorConfiguration( ).getVirtualSensorName( );
      StorageManager storageMan = StorageManager.getInstance( );
      synchronized ( psLock ) {
         storageMan.insertDataNoDupError( name , data );
      }
      // SimulationResult.addJustBeforeStartingToEvaluateQueries ();
      ArrayList < NotificationRequest > registered;
      synchronized ( notificationRequestsLock ) {
         registered = notificationRequests.get( name );
      }
      if ( registered == null ) {
         if ( logger.isDebugEnabled( ) ) logger.debug( new StringBuilder( ).append( "No Query registered for >" ).append( name ).append( "<" ).toString( ) );
         // SimulationResult.addJustQueryEvaluationFinished ( 0 );
         return;
      }
      if ( logger.isDebugEnabled( ) )
         logger.debug( new StringBuilder( ).append( "There are queries " ).append( registered.size( ) ).append( " registered for >" ).append( name ).append( "<" ).toString( ) );
      ArrayList < NotificationRequest > notificationCandidates = new ArrayList < NotificationRequest >( );
      synchronized ( registered ) {
         Iterator < NotificationRequest > registeredIterator = registered.iterator( );
         while ( registeredIterator.hasNext( ) ) {
            NotificationRequest interestedClient = registeredIterator.next( );
            StringBuilder query = interestedClient.getQuery( );
            boolean result = interestedClient.send(  storageMan.executeQuery( query , false ) );
            //FIXME : The Asynchronous notification System.
         }
      }
   }
   
   public void doGet ( HttpServletRequest request , HttpServletResponse response ) throws ServletException , IOException {
      response.setContentType( "text/xml" );
      // to be sure it isn't cached
      response.setHeader( "Expires" , "Sat, 6 May 1995 12:00:00 GMT" );
      response.setHeader( "Cache-Control" , "no-store, no-cache, must-revalidate" );
      response.addHeader( "Cache-Control" , "post-check=0, pre-check=0" );
      response.setHeader( "Pragma" , "no-cache" );
      
      String rawRequest = request.getParameter( Container.REQUEST );
      int requestType = -1;
      if ( rawRequest == null || rawRequest.trim( ).length( ) == 0 ) {
         requestType = Container.REQUEST_LIST_VIRTUAL_SENSORS;
      } else
         try {
            requestType = Integer.parseInt( ( String ) rawRequest );
         } catch ( Exception e ) {
            logger.debug( e.getMessage( ) , e );
            requestType = -1;
         }
      StringBuilder sb = new StringBuilder( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" );
      response.getWriter( ).write( sb.toString( ) );
      RequestHandler handler;
      if ( logger.isDebugEnabled( ) ) logger.debug( "Received a request with code : " + requestType );
      
      switch ( requestType ) {
         case Container.REQUEST_ONE_SHOT_QUERY :
            handler = new OneShotQueryHandler( );
            if ( handler.isValid( request , response ) ) handler.handle( request , response );
            break;
         case Container.REQUEST_ONE_SHOT_QUERY_WITH_ADDRESSING :
            handler = new OneShotQueryWithAddressingHandler( );
            if ( handler.isValid( request , response ) ) handler.handle( request , response );
            break;
         case Container.REQUEST_LIST_VIRTUAL_SENSORS :
            handler = new ContainerInfoHandler( );
            if ( handler.isValid( request , response ) ) handler.handle( request , response );
            break;
         case Container.REQUEST_OUTPUT_FORMAT :
            handler = new OutputStructureHandler( );
            if ( handler.isValid( request , response ) ) handler.handle( request , response );
            break;
         case Container.REQUEST_ADDRESSING :
            handler = new AddressingReqHandler( );
            if ( handler.isValid( request , response ) ) handler.handle( request , response );
            break;
         default :
            response.sendError( Container.UNSUPPORTED_REQUEST_ERROR , "The requested operation is not supported." );
            break;
      }
   }
   
   
   public synchronized void addNotificationRequest ( String localVirtualSensorName , NotificationRequest notificationRequest ) {
      localVirtualSensorName = localVirtualSensorName.toUpperCase( );
      ArrayList < NotificationRequest > contents;
      if ( notificationRequests.get( localVirtualSensorName ) == null ) {
         contents = new ArrayList < NotificationRequest >( );
         notificationRequests.put( localVirtualSensorName , contents );
      } else
         contents = notificationRequests.get( localVirtualSensorName );
      if ( logger.isDebugEnabled( ) ) {
         logger.debug( "Notification request added to " + localVirtualSensorName );
      }
      synchronized ( contents ) {
         contents.add( notificationRequest );
      }
   }
   
   public synchronized void removeNotificationRequest ( String localVirtualSensorName , NotificationRequest notificationRequest ) {
      localVirtualSensorName = localVirtualSensorName.toUpperCase( );
      ArrayList < NotificationRequest > contents = notificationRequests.get( localVirtualSensorName );
      if ( contents == null ) {// when an invalid remove request recevied for
         // a
         // virtual sensor which doesn't have any query
         // registered to it.
         return;
      }
      synchronized ( contents ) {
         boolean changed = contents.remove( notificationRequest );
      }
   }
   
   public synchronized void removeNotificationRequest ( NotificationRequest notificationRequest ) {
      Iterator < String > virtualSensorNames = notificationRequests.keySet( ).iterator( );
      while ( virtualSensorNames.hasNext( ) ) {
         String virtualSensorName = virtualSensorNames.next( );
         ArrayList < NotificationRequest > contents = notificationRequests.get( virtualSensorName );
         if ( contents == null || contents.size( ) == 0 ) {// when an
            // invalid
            // remove request
            // recevied for a
            // virtual sensor which
            // doesn't have any
            // query registered to
            // it.
            return;
         }
         synchronized ( contents ) {
            boolean changed = contents.remove( notificationRequest );
         }
      }
   }
   
   public synchronized NotificationRequest [ ] getAllNotificationRequests ( ) {
      Vector < NotificationRequest > results = new Vector < NotificationRequest >( );
      for ( ArrayList < NotificationRequest > notifications : notificationRequests.values( ) )
         results.addAll( notifications );
      return results.toArray( new NotificationRequest [ ] {} );
   }
   
   public void addRemoteStreamSource ( int notificationCode , RemoteDS remoteDS ) {
      notificationCodeToRemoteDataSource.put( notificationCode , remoteDS );
      if ( logger.isDebugEnabled( ) )
         logger.debug( new StringBuilder( ).append( "Remote DataSource DBALIAS *" ).append( remoteDS.getDBAlias( ) ).append( "* with the code : *" ).append( notificationCode ).append( "* added." )
               .toString( ) );
   }
   
   public void removeAllResourcesAssociatedWithVSName ( String vsensorName ) {
      ArrayList < NotificationRequest > effected = notificationRequests.remove( vsensorName );
      // FIXME : The used prepare statements should be released from the
      // stroagemanager using a timeout mechanism.
      // PreparedStatement ps;
      // synchronized (psLock) {
      // ps = preparedStatements.remove(vsensorName);
      // }
      // StorageManager.getInstance().returnPrepaedStatement(ps);
   }
   
   public void removeRemoteStreamSource ( int notificationCode ) {
      notificationCodeToRemoteDataSource.remove( notificationCode );
   }
   public RemoteDS getRemoteDSForANotificationCode(int code) {
      return notificationCodeToRemoteDataSource.get( code );
   }
   
}
