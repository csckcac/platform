/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer;

import org.apache.axiom.om.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.builders.*;
import org.wso2.carbon.bam.analyzer.engine.*;
import org.wso2.carbon.bam.analyzer.task.BAMTaskInfo;
import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.Granularity;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.configurations.IndexType;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraIndexConfiguration;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    private static DataAccessService dataAccessService;
    private static RegistryService registryService;
    private static AnalyzerEngine engine;
    private static TenantRegistryLoader tenantRegistryLoader;
    private static TaskService taskService;
    private static OMFactory factory = OMAbstractFactory.getOMFactory();
    private static String NULL_NAMESPACE = "";
    private static OMNamespace NULL_OMNS = factory.createOMNamespace(NULL_NAMESPACE, "");

    public static AnalyzerEngine getEngine() {
        return engine;
    }

    public static void setEngine(AnalyzerEngine engine) {
        Utils.engine = engine;
    }

    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public static TaskService getTaskService() {
        return Utils.taskService;
    }

    public static void setDataAccessService(DataAccessService dataAccessService) {
        Utils.dataAccessService = dataAccessService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        Utils.registryService = registryService;
    }

/*    public static List<AnalyzerSequence> getAnalyzerSequences(OMElement documentEl)
            throws AnalyzerException {
        List<AnalyzerSequence> analyzerSequences = new ArrayList<AnalyzerSequence>();
        Iterator sequenceIterator = documentEl.getChildrenWithLocalName(AnalyzerConfigConstants.ANALYZER_SEQUENCE);
        while (sequenceIterator.hasNext()) {
            Object seqElementObject = sequenceIterator.next();
            if (!(seqElementObject instanceof OMElement)) {
                continue;
            }
            OMElement analyzerSeqEl = (OMElement) seqElementObject;
            AnalyzerSequence sequence = getAnalyzerSequence(analyzerSeqEl);
            analyzerSequences.add(sequence);
        }
        return analyzerSequences;
    }*/

//    private static AnalyzerSequence getAnalyzerSequence(TaskDTO taskDTO) {
//        AnalyzerSequence analyzerSequence = new AnalyzerSequence();
//
//        assert taskDTO.getSequenceName() != null;
//        analyzerSequence.setName(taskDTO.getSequenceName());
//
//        if (taskDTO.getCronTrigger() != null) {
//            analyzerSequence.setCron(taskDTO.getCronTrigger());
//        }
//
//        AnalyzerDTO[] analyzers = taskDTO.getAnalyzers();
//        assert analyzers != null;
//        for (int i = 0; i < analyzers.length; i++) {
//            AnalyzerDTO analyzerDTO = analyzers[i];
//            getAnalyzerObject(analyzerDTO);
//        }
//
//    }

/*    public static List<CFConfigBean> getIndexConfigurations(OMElement configEl)
            throws AnalyzerException {

        Iterator cfIterator = configEl.getChildrenWithName(AnalyzerConfigConstants.CF_ELEMENT);

        List<CFConfigBean> cfList = new ArrayList<CFConfigBean>();
        while (cfIterator.hasNext()) {
            Object cfElementObject = cfIterator.next();
            if (!(cfElementObject instanceof OMElement)) {
                continue;
            }

            OMElement cfElement = (OMElement) cfElementObject;
            OMAttribute cfName = cfElement.getAttribute(AnalyzerConfigConstants.name);

            if (cfName == null || cfName.getAttributeValue() == null ||
                cfName.getAttributeValue().trim().equals("")) {
                throw new AnalyzerException("Required attribute 'name' missing..");
            }

            String cfNameStr = cfName.getAttributeValue();

            OMAttribute defaultCF = cfElement.getAttribute(AnalyzerConfigConstants.DEFAULT_CF_QNAME);

            boolean isDefaultCf = false;
            if (defaultCF != null) {
                isDefaultCf = Boolean.getBoolean(defaultCF.getAttributeValue());
            }

            OMElement granularity = cfElement.getFirstChildWithName(
                    AnalyzerConfigConstants.granularity);

            String granularityStr = null;
            if (granularity != null) {
                granularityStr = granularity.getText();
            }

            OMElement indexRowKey = cfElement.getFirstChildWithName(
                    AnalyzerConfigConstants.INDEX_ROW_KEY_QNAME);

            String indexRowKeyStr = null;
            if (indexRowKey != null) {
                indexRowKeyStr = granularity.getText();
            }

            OMElement rowKey = cfElement.getFirstChildWithName(AnalyzerConfigConstants.ROWKEY_QNAME);

            List<KeyPart> keyParts = new ArrayList<KeyPart>();
            if (rowKey != null) {
                Iterator<OMElement> partIterator = rowKey.getChildrenWithName(AnalyzerConfigConstants.PART_QNAME);

                while (partIterator.hasNext()) {
                    OMElement partElement = partIterator.next();

                    OMAttribute partName = partElement.getAttribute(AnalyzerConfigConstants.name);

                    if (partName == null || partName.getAttributeValue() == null ||
                        partName.getAttributeValue().trim().equals("")) {
                        throw new AnalyzerException("Required attribute 'name' missing..");
                    }

                    String partNameStr = partName.getAttributeValue();

                    OMAttribute storeIndex = partElement.getAttribute(
                            AnalyzerConfigConstants.STORE_INDEX);

                    boolean isStoreIndex = false;
                    if (storeIndex != null) {
                        isStoreIndex = Boolean.getBoolean(storeIndex.getAttributeValue());
                    }

                    KeyPart part = new KeyPart(partNameStr, null, isStoreIndex);
                    keyParts.add(part);
                }
            }

            CFConfigBean cfConfig = new CFConfigBean();
            cfConfig.setCfName(cfNameStr);
            cfConfig.setDefaultCF(isDefaultCf);
            cfConfig.setGranularity(granularityStr);
            cfConfig.setIndexRowKey(indexRowKeyStr);
            cfConfig.setRowKeyParts(keyParts);

            cfList.add(cfConfig);

        }

        return cfList;
    }*/

    public static AnalyzerSequence getAnalyzerSequence(int tenantId, OMElement analyzerSeqEl)
            throws AnalyzerException {
        OMAttribute seqName = analyzerSeqEl.getAttribute(AnalyzerConfigConstants.name);
        if (seqName == null) {
            String message = "Analyzer Sequence name cannot be null";
            AnalyzerException analyzerException = new AnalyzerException(message);
            log.error(message, analyzerException);
            throw analyzerException;
        }
        OMElement triggerEl = analyzerSeqEl.getFirstChildWithName(AnalyzerConfigConstants.TRIGGER_ELEMENT);
        AnalyzerSequence sequence = new AnalyzerSequence();
        sequence.setTenantId(tenantId);
        OMAttribute countEl = triggerEl.getAttribute(AnalyzerConfigConstants.COUNT_ATTRIBUTE);
        if (countEl != null) {
            sequence.setCount(Integer.parseInt(countEl.getAttributeValue()));
        }
        OMAttribute intervalEl = triggerEl.getAttribute(AnalyzerConfigConstants.INTERVAL_ATTRIBUTE);
        if (intervalEl != null) {
            sequence.setInterval(Integer.parseInt(intervalEl.getAttributeValue()));
        }
        OMAttribute cronEl = triggerEl.getAttribute(AnalyzerConfigConstants.CRON_ATTRIBUTE);
        if (cronEl != null) {
            sequence.setCron(cronEl.getAttributeValue());
        }
        sequence.setName(seqName.getAttributeValue());

        OMElement analyzersEl = analyzerSeqEl.getFirstChildWithName(
                AnalyzerConfigConstants.ANALYZERS_ELEMENT);
        Iterator analyzersIterator = analyzersEl.getChildElements();
        while (analyzersIterator.hasNext()) {
            Object analyzerElementObject = analyzersIterator.next();
            if (!(analyzerElementObject instanceof OMElement)) {
                continue;
            }
            OMElement analyzerEl = (OMElement) analyzerElementObject;

            Object analyzerObject = getAnalyzerObject(analyzerEl);
            ((Analyzer) analyzerObject).setAnalyzerSeqeunceName(sequence.getName());
            ((Analyzer) analyzerObject).setAnalyzerSequence(sequence);
            sequence.getAnalyzers().add((Analyzer) analyzerObject);

/*                String analyzerClassName = analyzerEl.getAttribute(AnalyzerConfigConstants.className).getAttributeValue();
            try {
                Class analyzerClass = this.getClass().getClassLoader().loadClass(analyzerClassName);
                Object analyzerObject = analyzerClass.newInstance();
                if (!(analyzerObject instanceof Analyzer)) {
                    throw new AnalyzerException(analyzerClassName + " does not implement the Analyzer interface");
                }
                sequence.getAnalyzers().add((Analyzer) analyzerObject);
            } catch (ClassNotFoundException e) {
                String msg = analyzerClassName + "Class not available in the class path";
                throw new AnalyzerException(msg, e);
            } catch (InstantiationException e) {
                String msg = analyzerClassName + "Class cannot be instantiated or has no nullary constructor";
                throw new AnalyzerException(msg, e);
            } catch (IllegalAccessException e) {
                String msg = analyzerClassName + "Class has a private constructor";
                throw new AnalyzerException(msg, e);
            }*/
        }
        return sequence;
    }

/*    public static Object processDefaultAnalyzer(OMElement analyzerEl) throws AnalyzerException {
        String analyzerClassName = analyzerEl.getAttribute(AnalyzerConfigConstants.className).getAttributeValue();
        try {
            Class analyzerClass = Utils.class.getClass().getClassLoader().loadClass(analyzerClassName);
            Object analyzerObject = analyzerClass.newInstance();
            if (!(analyzerObject instanceof Analyzer)) {
                throw new AnalyzerException(analyzerClassName + " does not implement the Analyzer interface");
            }

            return analyzerObject;

        } catch (ClassNotFoundException e) {
            String msg = analyzerClassName + "Class not available in the class path";
            throw new AnalyzerException(msg, e);
        } catch (InstantiationException e) {
            String msg = analyzerClassName + "Class cannot be instantiated or has no nullary constructor";
            throw new AnalyzerException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = analyzerClassName + "Class has a private constructor";
            throw new AnalyzerException(msg, e);
        }
    }*/

    private static Map<String, AnalyzerBuilder> builderMap = null;

    public static Map<String, AnalyzerBuilder> getAnalyzerBuilderMap() {
        if (Utils.builderMap == null) {
            Map<String, AnalyzerBuilder> builderMap = new HashMap<String, AnalyzerBuilder>();
            builderMap.put(AnalyzerConfigConstants.GET_ANALYZER, new GetAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.AGGREGATE_ANALYZER, new AggregateAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.LOOKUP_ANALYZER, new LookupAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.PUT_ANALYZER, new PutAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.LOG_ANALYZER, new LogAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.DROP_ANALYZER, new DropAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.CORRELATE_ANALYZER, new CorrelateAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.INDEX_ANALYZER, new IndexingAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.GROUPBY_ANALYZER, new GroupByAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.ORDERBY_ANALYZER, new OrderByAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.EXTRACT_ANALYZER, new ExtractAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.DETECT_FAULTS_ANALYZER, new DetectFaultAnalyzerBuilder());
            builderMap.put(AnalyzerConfigConstants.JMX_ANALYZER, new JMXAnalyzerBuilder());

            builderMap.put(AnalyzerConfigConstants.ALERT_TRIGGER, new AlertBuilder());
            builderMap.put(AnalyzerConfigConstants.CLASS_ANALYZER, new ClassAnalyzerBuilder());
            Utils.builderMap = builderMap;
        }
        return Utils.builderMap;
    }

    public static Analyzer getAnalyzerObject(OMElement analyzerEl) throws AnalyzerException {
        String name = analyzerEl.getLocalName();

        Analyzer analyzer;

        if (getAnalyzerBuilderMap().containsKey(name)) {
            analyzer = getAnalyzerBuilderMap().get(name).buildAnalyzer(analyzerEl);
        } else {
            throw new AnalyzerException("Analyzer with name " + name + " not present..");
        }

        return analyzer;
    }


    private static ConfigurationContextService configurationContextService;

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        Utils.configurationContextService = configurationContextService;
    }

    public static void setTaskService(TaskService taskService) {
        Utils.taskService = taskService;
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return tenantRegistryLoader;
    }

    public static void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        Utils.tenantRegistryLoader = tenantRegistryLoader;
    }

/**
     * Serializes the index analyser configuration to be passed into task runtime.
     *
     * @param indexConfig Index Configuration
     * @return OMElement containing the index configuration
     */
    public static OMElement serializeIndexAnalyser(IndexConfiguration indexConfig) {
        OMElement rootEl = factory.createOMElement(new QName("index"));
        rootEl.addAttribute("name", indexConfig.getIndexName(), NULL_OMNS);
        rootEl.addAttribute("isAutoGenerated", Boolean.toString(indexConfig.isAutoGenerated()),
                NULL_OMNS);
        rootEl.addAttribute("isManuallyIndexed", Boolean.toString(indexConfig.isManuallyIndexed()),
                NULL_OMNS);

        if (indexConfig.getDataSourceType() != null) {
            OMElement dsTypeEl = factory.createOMElement(new QName("dataSourceType"));
            DataSourceType dsType = indexConfig.getDataSourceType();
            if (dsType.getName() != null) {
                dsTypeEl.addAttribute("name", dsType.getName(), NULL_OMNS);
                dsTypeEl.addAttribute("manuallyIndexed",
                        Boolean.toString(dsType.isManuallyIndexed()), NULL_OMNS);
            }
            rootEl.addChild(dsTypeEl);
        }

        if (indexConfig.getIndexedTable() != null) {
            OMElement idxTableEl = factory.createOMElement(new QName("indexTable"));
            idxTableEl.setText(indexConfig.getIndexedTable());
            rootEl.addChild(idxTableEl);
        }

        if (indexConfig.getIndexedColumns() != null) {
            OMElement idxColumnsEl = factory.createOMElement(new QName("indexColumns"));
            for (String idxColumn : indexConfig.getIndexedColumns()) {
                if (idxColumn != null) {
                    OMElement idxColumnEl = factory.createOMElement(new QName("indexColumn"));
                    idxColumnEl.setText(idxColumn);
                    idxColumnsEl.addChild(idxColumnEl);
                }
            }
            rootEl.addChild(idxColumnsEl);
        }

        if (indexConfig.getIndexType() != null) {
            IndexType idxType = indexConfig.getIndexType();
            OMElement idxTypeEl = factory.createOMElement(new QName("indexType"));
            if (idxType.getName() != null) {
                idxTypeEl.addAttribute("name", idxType.getName(), NULL_OMNS);
            }
            rootEl.addChild(idxTypeEl);
        }

        if (indexConfig.getGranularity() != null) {
            Granularity granularity = indexConfig.getGranularity();
            OMElement granularityEl = factory.createOMElement(new QName("granularity"));
            if (granularity.getName() != null) {
                granularityEl.addAttribute("name", granularity.getName(), NULL_OMNS);
            }
            rootEl.addChild(granularityEl);
        }

        if (indexConfig instanceof CassandraIndexConfiguration) {
            CassandraIndexConfiguration cassandraIdxConfig =
                    (CassandraIndexConfiguration) indexConfig;

            OMElement indexingCfEl = factory.createOMElement(new QName("indexingColumnFamily"));
            if (cassandraIdxConfig.getIndexingColumnFamily() != null) {
                indexingCfEl.setText(cassandraIdxConfig.getIndexingColumnFamily());
            }
            rootEl.addChild(indexingCfEl);

            OMElement cronEl = factory.createOMElement(new QName("cron"));
            if (cassandraIdxConfig.getCron() != null) {
                cronEl.setText(cassandraIdxConfig.getCron());
            }
            rootEl.addChild(cronEl);
        }

        return rootEl;
    }


     /**
     * Populates a TaskInfo object with the details provided.
     *
     * @param taskInfo BAM task configuration data
     * @return taskInfo
     * @throws java.io.IOException IOException
     * @throws AnalyzerException AnalyzerException
     */
    public static TaskInfo getTaskInfo(BAMTaskInfo taskInfo) throws
             IOException, AnalyzerException {

        AnalyzerSequence sequence = taskInfo.getAnalyzerSequence();

        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
        triggerInfo.setRepeatCount(sequence.getCount());
        triggerInfo.setIntervalMillis(sequence.getInterval());
        triggerInfo.setCronExpression(sequence.getCron());

        TaskInfo info = new TaskInfo();
        info.setName(sequence.getName());
        info.setTriggerInfo(triggerInfo);
        info.setTaskClass(AnalyzerConfigConstants.BAM_DEFAULT_TASK_CLASS);
        info.setProperties(Utils.getProperties(taskInfo));

        return info;
    }

    /**
     * Populates the additional properties to be passed into the task configuration.
     *
     * @param taskInfo BAM task configuration data
     * @return properties object populated with the additional information to be passed into the
     *                    task configuration
     * @throws IOException IOException
     * @throws AnalyzerException AnalyzerException
     */
    public static Map<String, String> getProperties(BAMTaskInfo taskInfo) throws IOException,
            AnalyzerException {
        AnalyzerSequence sequence = taskInfo.getAnalyzerSequence();
        OMElement analyzerSeqXML = taskInfo.getAnalyzerSeqXML();
        Map<String, String> credentials = taskInfo.getCredentials();

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AnalyzerConfigConstants.ANALYZER_SEQUENCE_NAME, sequence.getName());
        properties.put(AnalyzerConfigConstants.ANALYZER_SEQUENCE, analyzerSeqXML.toString());
        properties.put(AnalyzerConfigConstants.USERNAME,
                credentials.get(AnalyzerConfigConstants.USERNAME));
        properties.put(AnalyzerConfigConstants.PASSWORD,
                credentials.get(AnalyzerConfigConstants.PASSWORD));
        properties.put(AnalyzerConfigConstants.TENANT_ID, String.valueOf(sequence.getTenantId()));

        return properties;
    }

}
