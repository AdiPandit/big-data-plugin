/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.big.data.kettle.plugins.hive;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseType;

/**
 * User: Dzmitry Stsiapanau Date: 8/28/2015 Time: 10:23
 */
public class ImpalaSimbaDatabaseDialect extends Hive2SimbaDatabaseDialect {

  public ImpalaSimbaDatabaseDialect() {
    super();
  }

  /**
   * UID for serialization
   */
  private static final long serialVersionUID = -8456961348836455937L;

  protected static final int DEFAULT_PORT = 21050;

  protected static final String JDBC_URL_TEMPLATE = "jdbc:impala://%s:%s/%s;AuthMech=%d%s";

  private static final IDatabaseType DBTYPE =
    new DatabaseType( "Cloudera Impala", "IMPALASIMBA",
      DatabaseAccessType.getList( DatabaseAccessType.NATIVE,
        DatabaseAccessType.JNDI, DatabaseAccessType.ODBC ), DEFAULT_PORT,
      "http://go.cloudera.com/odbc-driver-hive-impala.html" );

  public IDatabaseType getDatabaseType() {
    return DBTYPE;
  }

  @Override
  public String getNativeDriver() {
    return "org.apache.hive.jdbc.ImpalaSimbaDriver";
  }

  @Override
  public String getNativeJdbcPre() {
    return "jdbc:impala://";
  }

  @Override
  public int getDefaultDatabasePort() {
    return DEFAULT_PORT;
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "ImpalaJDBC41.jar" };
  }

  @Override
  protected String getJdbcUrlTemplate() {
    return JDBC_URL_TEMPLATE;
  }

  @Override public boolean initialize( String classname ) {
    return true;
  }
}
