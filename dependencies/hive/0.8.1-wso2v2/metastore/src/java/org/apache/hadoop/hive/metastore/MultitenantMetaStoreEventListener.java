/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.apache.hadoop.hive.metastore;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.events.multitenancy.AddPartitionEvent;
import org.apache.hadoop.hive.metastore.events.multitenancy.AlterPartitionEvent;
import org.apache.hadoop.hive.metastore.events.multitenancy.AlterTableEvent;
import org.apache.hadoop.hive.metastore.events.multitenancy.CreateDatabaseEvent;
import org.apache.hadoop.hive.metastore.events.multitenancy.CreateTableEvent;
import org.apache.hadoop.hive.metastore.events.multitenancy.DropDatabaseEvent;
import org.apache.hadoop.hive.metastore.events.multitenancy.DropPartitionEvent;
import org.apache.hadoop.hive.metastore.events.multitenancy.DropTableEvent;
import org.apache.hadoop.hive.metastore.events.multitenancy.LoadPartitionDoneEvent;

/**
 * This abstract class needs to be extended to  provide implementation of actions that needs
 * to be performed when a particular event occurs on a metastore. These methods
 * are called whenever an event occurs on metastore. Status of the event whether
 * it was successful or not is contained in container event object.
 */

public abstract class MultitenantMetaStoreEventListener implements Configurable {

  private Configuration conf;

  public MultitenantMetaStoreEventListener (Configuration config){
    this.conf = config;
  }

  /**
   * @param create table event.
   * @throws org.apache.hadoop.hive.metastore.api.MetaException
   */
  public abstract void onCreateTable (CreateTableEvent tableEvent) throws MetaException;

  /**
   * @param drop table event.
   * @throws MetaException
   */
  public abstract void onDropTable (DropTableEvent tableEvent)  throws MetaException;

  /**
   * @param add partition event
   * @throws MetaException
   */

  /**
   * @param tableEvent alter table event
   * @throws MetaException
   */
  public abstract void onAlterTable (AlterTableEvent tableEvent) throws MetaException;


  public abstract void onAddPartition (AddPartitionEvent partitionEvent)  throws MetaException;

  /**
   * @param drop partition event
   * @throws MetaException
   */
  public abstract void onDropPartition (DropPartitionEvent partitionEvent)  throws MetaException;

  /**
   * @param alter partition event
   * @throws MetaException
   */
  public abstract void onAlterPartition (AlterPartitionEvent partitionEvent)  throws MetaException;

  /**
   * @param create database event
   * @throws MetaException
   */
  public abstract void onCreateDatabase (CreateDatabaseEvent dbEvent) throws MetaException;

  /**
   * @param drop database event
   * @throws MetaException
   */
  public abstract void onDropDatabase (DropDatabaseEvent dbEvent) throws MetaException;

  /**
   * @param partSetDoneEvent
   * @throws MetaException
   */
  public abstract void onLoadPartitionDone(LoadPartitionDoneEvent partSetDoneEvent) throws MetaException;

  @Override
  public Configuration getConf() {
    return this.conf;
  }

  @Override
  public void setConf(Configuration config) {
    this.conf = config;
  }

}

