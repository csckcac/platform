package org.apache.hadoop.hive.cassandra;

import org.apache.cassandra.thrift.AuthenticationException;
import org.apache.cassandra.thrift.AuthenticationRequest;
import org.apache.cassandra.thrift.AuthorizationException;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CassandraClientHolder
{
    private static final Logger log = LoggerFactory.getLogger(CassandraClientHolder.class);

    private Cassandra.Client client;
    private final TTransport transport;
    private String keyspace;

    public CassandraClientHolder(TTransport transport, String keyspace) throws CassandraException
    {
        this(transport, null,null);
    }

    public CassandraClientHolder(TTransport transport, String keyspace, Map<String, String> credentials) throws CassandraException

    {
        this.transport = transport;
        this.keyspace = keyspace;
        initClient(credentials);
    }

    public boolean isOpen()
    {
        return client != null && transport != null && transport.isOpen();
    }

    public String getKeyspace()
    {
        return keyspace;
    }

    private void initClient(Map<String, String> credentials) throws CassandraException
    {
        try
        {
            transport.open();
        } catch (TTransportException e)
        {
            throw new CassandraException("unable to connect to server", e);
        }

        // If username is provided authenticate with keyspace         
        client = new Cassandra.Client(new TBinaryProtocol(transport));

        if (credentials != null) {
          try {
            client.login(new AuthenticationRequest(credentials));
          } catch (AuthenticationException e) {
            throw new CassandraException("Unable to authenticate to keyspace " + this.keyspace + "\n" +
              e.getMessage());
          } catch (AuthorizationException e) {
            throw new CassandraException("Unable to authrorize to keyspace " + this.keyspace + "\n" +
              e.getMessage());
          } catch (TException e) {
            throw new CassandraException("Unable to connect to keyspace " + this.keyspace + "\n" +
              e.getMessage());
          }
        }

        // connect to last known keyspace
        setKeyspace(keyspace);
    }

    /**
     * Set the client with the (potentially) new keyspace. Safe to call this
     * repeatedly with the same keyspace.
     * @param keyspace
     * @return
     * @throws CassandraException
     */
    public void setKeyspace(String ks) throws CassandraException
    {
        if ( ks == null )
        {
            return;
        }

        if (keyspace == null || !StringUtils.equals(keyspace, ks))
        {
            try
            {
                this.keyspace = ks;
                client.set_keyspace(keyspace);
            } catch (InvalidRequestException e)
            {
                throw new CassandraException(e);
            } catch (TException e)
            {
                throw new CassandraException(e);
            }
        }
    }

    public Cassandra.Client getClient()
    {
        return client;
    }



    public void close()
    {
        if ( transport == null || !transport.isOpen() )
        {
            return;
        }
        try
        {
            transport.flush();
        } catch (Exception e)
        {
            log.error("Could not flush transport for client holder: "+ toString(), e);
        } finally
        {
            try
            {
                if (transport.isOpen())
                {
                    transport.close();
                }
            } catch (Exception e)
            {
                log.error("Error on transport close for client: " + toString(),e);
            }
        }
    }

}
