package org.wso2.carbon.humantask.core.dao.jpa.openjpa;

import org.wso2.carbon.humantask.core.dao.Constants;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnectionFactoryJDBC;
import org.wso2.carbon.humantask.core.dao.jpa.JPAVendorAdapter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA Implementation
 */
public class HumanTaskDAOConnectionFactoryImpl implements HumanTaskDAOConnectionFactoryJDBC {

    private EntityManagerFactory entityManagerFactory;

    private DataSource dataSource;

    private TransactionManager tnxManager;

    private Map<String, Object> jpaPropertiesMap;

    private static ThreadLocal<HumanTaskDAOConnectionImpl> connections = new ThreadLocal<HumanTaskDAOConnectionImpl>();

//    private HumanTaskDAOConnectionImpl connection;

    public HumanTaskDAOConnectionFactoryImpl() {

    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void setTransactionManager(TransactionManager tnxManager) {
        this.tnxManager = tnxManager;

    }

    @Override
    public void setDAOConnectionFactoryProperties(Map<String, Object> propertiesMap) {
        this.jpaPropertiesMap = propertiesMap;
    }

    @Override
    public HumanTaskDAOConnection getConnection() {
        //TODO : After we have external tnx manager injection, need to handle the synchronisations at this level.
        // introduce sync and thread local behaviours.

        try {
            tnxManager.getTransaction().registerSynchronization(new Synchronization() {
                // OpenJPA allows cross-transaction entity managers, which we don't want
                public void afterCompletion(int i) {
                    if (connections.get() != null) {
                        connections.get().getEntityManager().close();
                    }
                    connections.set(null);
                }
                public void beforeCompletion() { }
            });
        } catch (RollbackException e) {
            throw new RuntimeException("Coulnd't register synchronizer!", e);
        } catch (SystemException e) {
            throw new RuntimeException("Coulnd't register synchronizer!", e);
        }

        if (connections.get() != null) {
            return connections.get();
        } else {
            HashMap propMap = new HashMap();
            propMap.put("openjpa.TransactionMode", "managed");
            EntityManager em = entityManagerFactory.createEntityManager(propMap);
            HumanTaskDAOConnectionImpl conn = createHumanTaskDAOConnection(em);
            connections.set(conn);
            return conn;
        }
    }

    protected HumanTaskDAOConnectionImpl createHumanTaskDAOConnection(EntityManager entityManager) {
        return new HumanTaskDAOConnectionImpl(entityManager);
    }

    @Override
    public void init() {

        JPAVendorAdapter vendorAdapter = getJPAVendorAdapter();
        this.entityManagerFactory = Persistence.createEntityManagerFactory("HT-PU",
                vendorAdapter.getJpaPropertyMap(tnxManager));

    }


    /**
     * Returns the JPA Vendor adapter based on user preference
     * <p/>
     * Note: Currently we only support one JPA vendor(OpenJPA), so I have omitted vendor selection
     * logic.
     *
     * @return JPAVendorAdapter implementation
     */
    private JPAVendorAdapter getJPAVendorAdapter() {
        JPAVendorAdapter vendorAdapter = new OpenJPAVendorAdapter();

        vendorAdapter.setDataSource(dataSource);

        // TODO: Investigate whether this could be moved to upper layer. Directly put bool into prop map.
        Object generateDDL = jpaPropertiesMap.get(Constants.PROP_ENABLE_DDL_GENERATION);
        Object showSQL = jpaPropertiesMap.get(Constants.PROP_ENABLE_SQL_TRACING);

        if (generateDDL == null) {
            generateDDL = Boolean.FALSE.toString();
        }

        if (showSQL == null) {
            showSQL = Boolean.FALSE.toString();
        }

        vendorAdapter.setGenerateDdl((Boolean) generateDDL);
        vendorAdapter.setShowSql((Boolean) showSQL);

        return vendorAdapter;
    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public void shutdown() {
        this.entityManagerFactory.close();
    }
}
