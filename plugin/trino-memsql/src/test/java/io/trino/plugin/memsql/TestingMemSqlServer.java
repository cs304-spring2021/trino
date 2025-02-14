/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.memsql;

import com.google.common.collect.ImmutableSet;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class TestingMemSqlServer
        extends JdbcDatabaseContainer<TestingMemSqlServer>
{
    private static final String MEM_SQL_LICENSE = requireNonNull(System.getProperty("memsql.license"), "memsql.license is not set");

    public static final String DEFAULT_TAG = "memsql/cluster-in-a-box:centos-7.1.13-11ddea2a3a-3.0.0-1.9.0";

    public static final Integer MEMSQL_PORT = 3306;

    public TestingMemSqlServer()
    {
        this(DEFAULT_TAG);
    }

    public TestingMemSqlServer(String dockerImageName)
    {
        super(DockerImageName.parse(dockerImageName));
        start();
    }

    @Override
    public Set<Integer> getLivenessCheckPortNumbers()
    {
        return ImmutableSet.of(getMappedPort(MEMSQL_PORT));
    }

    @Override
    protected void configure()
    {
        addExposedPort(MEMSQL_PORT);
        addEnv("LICENSE_KEY", MEM_SQL_LICENSE);
        addEnv("START_AFTER_INIT", "true");
        setStartupAttempts(3);
    }

    @Override
    public String getDriverClassName()
    {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    public String getUsername()
    {
        return "root";
    }

    @Override
    public String getPassword()
    {
        return "";
    }

    @Override
    public String getJdbcUrl()
    {
        return "jdbc:mariadb://" + getContainerIpAddress() + ":" + getMappedPort(MEMSQL_PORT);
    }

    @Override
    public String getTestQueryString()
    {
        return "SELECT 1";
    }

    public void execute(String sql)
    {
        execute(sql, getUsername(), getPassword());
    }

    public void execute(String sql, String user, String password)
    {
        try (Connection connection = DriverManager.getConnection(getJdbcUrl(), user, password);
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
