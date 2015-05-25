/*
 * Copyright (c) 2014-2015, Qiao
 *
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
 * limitations under the LICENSE
 */

package info.breezes.orm;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import junit.framework.Assert;
import junit.framework.TestResult;

import java.io.File;
import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class OrmTest extends AndroidTestCase {

    String TAG = "orm.test";

    SimpleOrmSQLiteHelper helper;
    boolean over;

    static {
        OrmConfig.Debug = true;
    }

    @Override
    public void run(TestResult result) {
        long st = System.currentTimeMillis();
        super.run(result);
        Log.d(TAG, getName() + ":" + (System.currentTimeMillis() - st));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Log.d(TAG, "setUp");
        OrmConfig.Debug = false;
        helper = new SimpleOrmSQLiteHelper(getContext(), "test.db", 1, Employee.class);
    }

    @SmallTest
    public void testACreateTable() throws Exception {
        SQLiteDatabase database = helper.getCurrentDatabase(true);
        assertNotNull(database);
    }

    @SmallTest
    public void testBInsert500() throws Exception {
        for (int i = 0; i < 500; i++) {
            Employee employee = new Employee();
            helper.insert(employee);
        }
    }

    @SmallTest
    public void testCInsertAll500() throws Exception {
        ArrayList<Employee> employees = new ArrayList<Employee>();
        for (int i = 0; i < 500; i++) {
            Employee employee = new Employee();
            employees.add(employee);
        }
        helper.insertAll(employees.toArray());
    }

    @SmallTest
    public void testDCount() throws Exception {
        int count = helper.query(Employee.class).execute().size();
        Log.d(TAG, "Count:" + count);
    }


    @SmallTest
    public void testESelect500ToList() throws Exception {
        ArrayList<Employee> employees = helper.query(Employee.class).limit(0, 500).execute().toList();
        if (employees.size() < 1) {
            fail("query to list error.");
        } else {
            Log.d(TAG, "ListSize:" + employees.size());
        }
    }


    @SmallTest
    public void testFDeleteAll() throws Exception {
        helper.clear(Employee.class);
        over = true;
    }

    @SmallTest
    public void testGUpgrade() throws Exception {
        SimpleOrmSQLiteHelper helper = new SimpleOrmSQLiteHelper(getContext(), "test2.db", 1, Employee.class);
        Log.d(TAG, helper.getCurrentDatabase(true).getVersion() + "-->1");
        Assert.assertEquals(1, helper.getCurrentDatabase(false).getVersion());
        ArrayList<Employee> employees = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Employee employee = new Employee();
            employees.add(employee);
        }
        helper.insertAll(employees.toArray());
        helper.close();
        helper = new SimpleOrmSQLiteHelper(getContext(), "test2.db", 2, Employee2.class);
        Log.d(TAG, helper.getCurrentDatabase(true).getVersion() + "-->2");
        Assert.assertEquals(2, helper.getCurrentDatabase(false).getVersion());
        int count = helper.query(Employee2.class).execute().size();
        Log.d(TAG, "c:" + count);
        Assert.assertEquals(500, count);
        helper.close();
        File file = new File(helper.getCurrentDatabase(false).getPath());
        helper.getCurrentDatabase(false).close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            SQLiteDatabase.deleteDatabase(file);
        } else {
            file.delete();
        }
    }

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG, "tearDown");
        if (over) {
            helper.close();
            Log.d(TAG, "Clean.");
            File file = new File(helper.getCurrentDatabase(false).getPath());
            helper.getCurrentDatabase(false).close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                SQLiteDatabase.deleteDatabase(file);
            } else {
                file.delete();
            }
        }
        super.tearDown();
    }
}